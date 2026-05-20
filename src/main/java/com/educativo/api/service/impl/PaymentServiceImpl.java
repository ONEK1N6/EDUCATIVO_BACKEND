package com.educativo.api.service.impl;

import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.MonthlyFeeResponse;
import com.educativo.api.dto.PaymentSummaryResponse;
import com.educativo.api.entity.FeeConfig;
import com.educativo.api.entity.MonthlyFee;
import com.educativo.api.entity.PaymentStatus;
import com.educativo.api.entity.Student;
import com.educativo.api.entity.FeeType;
import com.educativo.api.repository.FeeConfigRepository;
import com.educativo.api.repository.MonthlyFeeRepository;
import com.educativo.api.repository.StudentRepository;
import com.educativo.api.service.PaymentService;
import com.educativo.api.dto.ManualMonthlyFeeRequest;
import com.educativo.api.dto.GroupPaymentRequest;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    MonthlyFeeRepository monthlyFeeRepository;

    @Autowired
    FeeConfigRepository feeConfigRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    com.educativo.api.service.EmailService emailService;

    @Override
    @Transactional
    public void generateFeesForStudent(Student student) {
        FeeConfig config = feeConfigRepository.findByLevel(student.getLevel())
                .orElseThrow(() -> new RuntimeException("Error: Fee configuration not found for level " + student.getLevel()));

        int year = LocalDate.now().getYear();
        List<MonthlyFee> initialFees = new ArrayList<>();

        // 1. Enrollment Fee (MATRICULA)
        initialFees.add(MonthlyFee.builder()
                .month("MATRICULA")
                .year(year)
                .amount(config.getRegistrationAmount())
                .status(PaymentStatus.PENDING)
                .type(FeeType.MATRICULA)
                .dueDate(LocalDate.of(year, 3, 5)) // Enrollment usually due early March
                .student(student)
                .build());

        // 2. First Pension (MARZO) - Since classes start March 9
        initialFees.add(MonthlyFee.builder()
                .month("MARZO")
                .year(year)
                .amount(config.getMonthlyAmount())
                .status(PaymentStatus.PENDING)
                .type(FeeType.PENSION)
                .dueDate(LocalDate.of(year, 3, config.getDueDay()))
                .student(student)
                .build());

        monthlyFeeRepository.saveAll(initialFees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyFeeResponse> getFeesByStudent(Long studentId) {
        return monthlyFeeRepository.findByStudentIdOrderByYearAscDueDateAsc(studentId).stream()
                .map(fee -> MonthlyFeeResponse.builder()
                        .id(fee.getId())
                        .month(fee.getMonth())
                        .year(fee.getYear())
                        .amount(fee.getAmount())
                        .status(fee.getStatus().name())
                        .dueDate(fee.getDueDate().toString())
                        .paymentDate(fee.getPaymentDate() != null ? fee.getPaymentDate().toString() : null)
                        .description(fee.getDescription())
                        .feeType(fee.getType().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary() {
        List<MonthlyFee> allFees = monthlyFeeRepository.findAll();
        
        double totalCollected = allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.PAID)
                .mapToDouble(MonthlyFee::getAmount)
                .sum();

        double totalPending = allFees.stream()
                .filter(f -> f.getStatus() != PaymentStatus.PAID)
                .mapToDouble(MonthlyFee::getAmount)
                .sum();

        long overdueCount = allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.OVERDUE || 
                           (f.getStatus() == PaymentStatus.PENDING && f.getDueDate().isBefore(LocalDate.now())))
                .count();

        return PaymentSummaryResponse.builder()
                .totalCollected(totalCollected)
                .totalPending(totalPending)
                .overduePaymentsCount(overdueCount)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse recordPayment(Long feeId) {
        MonthlyFee fee = monthlyFeeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Error: Fee not found."));

        if (fee.getStatus() == PaymentStatus.PAID) {
            return new MessageResponse("Error: Esta pensión ya ha sido pagada.");
        }

        fee.setStatus(PaymentStatus.PAID);
        fee.setPaymentDate(LocalDateTime.now());
        monthlyFeeRepository.save(fee);

        emailService.sendPaymentSuccessEmail(fee.getStudent().getParent(), fee);

        return new MessageResponse("Pago registrado exitosamente para " + fee.getMonth());
    }

    @Override
    @Transactional
    public MessageResponse createManualMonthlyFee(ManualMonthlyFeeRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Error: Student not found with ID: " + request.getStudentId()));

        MonthlyFee monthlyFee = MonthlyFee.builder()
                .student(student)
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .description(request.getDescription())
                .type(request.getFeeType())
                .status(PaymentStatus.PENDING)
                .month(request.getDueDate().getMonth().toString()) // Derive month from dueDate
                .year(request.getDueDate().getYear()) // Derive year from dueDate
                .build();

        monthlyFeeRepository.save(monthlyFee);
        return new MessageResponse("Monthly fee created successfully for student " + student.getId());
    }

    @Override
    @Transactional
    public MessageResponse createGroupPayment(GroupPaymentRequest request) {
        List<Student> students = studentRepository.findByLevelAndGradeAndSection(
                request.getLevel(), request.getGrade(), request.getSection());

        if (students.isEmpty()) {
            return new MessageResponse("No se encontraron estudiantes para los criterios seleccionados.");
        }

        List<MonthlyFee> feesToSave = new ArrayList<>();

        for (Student student : students) {
            feesToSave.add(MonthlyFee.builder()
                    .student(student)
                    .amount(request.getAmount())
                    .dueDate(request.getDueDate())
                    .description(request.getDescription())
                    .type(request.getFeeType())
                    .status(PaymentStatus.PENDING)
                    .month(request.getDueDate().getMonth().toString())
                    .year(request.getDueDate().getYear())
                    .build());
        }

        monthlyFeeRepository.saveAll(feesToSave);
        return new MessageResponse("Se generaron " + feesToSave.size() + " pagos de " + request.getFeeType() + " para " + students.size() + " estudiantes.");
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePaymentHistoryPdf(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        List<MonthlyFee> fees = monthlyFeeRepository.findByStudentIdOrderByYearAscDueDateAsc(studentId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLUE);
            Paragraph title = new Paragraph("Lumina Academic", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph subTitle = new Paragraph("HISTORIAL DE PAGOS", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Fecha de Reporte: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("Estudiante: " + student.getFirstName() + " " + student.getLastName()));
            document.add(new Paragraph("DNI: " + student.getDni()));
            document.add(new Paragraph("Nivel/Grado: " + student.getLevel() + " - " + student.getGrade()));
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{3f, 2f, 2f, 2f, 3f});

            // Headers
            String[] headers = {"Descripción", "Monto", "Vencimiento", "Estado", "Fecha Pago"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            for (MonthlyFee fee : fees) {
                table.addCell(fee.getDescription() != null ? fee.getDescription() : fee.getMonth() + " " + fee.getYear());
                table.addCell("S/ " + String.format("%.2f", fee.getAmount()));
                table.addCell(fee.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                PdfPCell statusCell = new PdfPCell(new Phrase(fee.getStatus().name()));
                if (fee.getStatus() == PaymentStatus.PAID) {
                    statusCell.setPhrase(new Phrase("PAGADO", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GREEN.darker())));
                } else if (fee.getDueDate().isBefore(LocalDate.now())) {
                    statusCell.setPhrase(new Phrase("VENCIDO", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.RED)));
                }
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(statusCell);

                table.addCell(fee.getPaymentDate() != null ? 
                        fee.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-");
            }

            document.add(table);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Payment History PDF", e);
        }
    }
}

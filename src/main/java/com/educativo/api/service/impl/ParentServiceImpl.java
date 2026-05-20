package com.educativo.api.service.impl;

import com.educativo.api.dto.CardInfoResponse;
import com.educativo.api.dto.CardPaymentRequest;
import com.educativo.api.dto.CreditHistoryResponse;
import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.MonthlyFeeResponse;
import com.educativo.api.dto.ParentResponse;
import com.educativo.api.dto.StudentResponse;
import com.educativo.api.entity.CreditCard;
import com.educativo.api.entity.MonthlyFee;
import com.educativo.api.entity.PaymentStatus;
import com.educativo.api.entity.Student;
import com.educativo.api.entity.User;
import com.educativo.api.repository.CreditCardRepository;
import com.educativo.api.repository.MonthlyFeeRepository;
import com.educativo.api.repository.StudentRepository;
import com.educativo.api.repository.UserRepository;
import com.educativo.api.service.ParentService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParentServiceImpl implements ParentService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    MonthlyFeeRepository monthlyFeeRepository;

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    com.educativo.api.service.EmailService emailService;

    @Autowired
    com.educativo.api.service.PaymentService paymentService;

    @Override
    public ParentResponse getMyData(String username) {
// ... existing code
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        return ParentResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dni(user.getDni())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .childrenNames(user.getChildren().stream().map(Student::getFirstName).collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<StudentResponse> getMyChildren(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        return user.getChildren().stream().map(s -> StudentResponse.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .dni(s.getDni())
                .section(s.getSection())
                .grade(s.getGrade())
                .level(s.getLevel())
                .parentName(user.getFirstName() + " " + user.getLastName())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<MonthlyFeeResponse> getStudentFees(String username, Long studentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        // Security check: ensure the student belongs to the requesting parent
        if (!student.getParent().getId().equals(user.getId())) {
            throw new RuntimeException("Error: Student does not belong to this parent.");
        }

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
    public CardInfoResponse getMyCardInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        CreditCard card = creditCardRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Error: Credit card not found for this user."));

        return CardInfoResponse.builder()
                .cardNumber(card.getCardNumber())
                .cardHolderName(card.getCardHolderName())
                .expirationDate(card.getExpirationDate())
                .cvv(card.getCvv())
                .balance(card.getBalance())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse processCardPayment(String username, CardPaymentRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        CreditCard card = creditCardRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Error: Credit card not found."));

        // 1. Security & Validation
        if (!card.getCardNumber().equals(request.getCardNumber())) {
            return new MessageResponse("Error: Número de tarjeta incorrecto.");
        }
        if (!card.getCvv().equals(request.getCvv())) {
            return new MessageResponse("Error: CVV incorrecto.");
        }
        if (!card.getExpirationDate().equals(request.getExpirationDate())) {
            return new MessageResponse("Error: Fecha de expiración incorrecta.");
        }

        MonthlyFee fee = monthlyFeeRepository.findById(request.getFeeId())
                .orElseThrow(() -> new RuntimeException("Error: Pago no encontrado."));

        if (fee.getStatus() == PaymentStatus.PAID) {
            return new MessageResponse("Error: Este pago ya ha sido realizado.");
        }

        // Security check: ensure the fee belongs to one of the parent's children
        boolean belongsToChild = user.getChildren().stream()
                .anyMatch(s -> s.getId().equals(fee.getStudent().getId()));
        
        if (!belongsToChild) {
            return new MessageResponse("Error: No tiene autorización para pagar esta cuota.");
        }

        // 2. Balance Check
        if (card.getBalance() < fee.getAmount()) {
            emailService.sendPaymentFailureEmail(user, fee, "Saldo insuficiente en la tarjeta");
            return new MessageResponse("Error: Saldo insuficiente en la tarjeta.");
        }

        // 3. Process Payment
        card.setBalance(card.getBalance() - fee.getAmount());
        creditCardRepository.save(card);

        fee.setStatus(PaymentStatus.PAID);
        fee.setPaymentDate(LocalDateTime.now());
        monthlyFeeRepository.save(fee);

        emailService.sendPaymentSuccessEmail(user, fee);

        return new MessageResponse("¡Pago realizado con éxito! Se han descontado S/ " + fee.getAmount() + " de su tarjeta.");
    }

    @Override
    public byte[] generateReceipt(String username, Long feeId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        MonthlyFee fee = monthlyFeeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Error: Pago no encontrado."));

        // Security check
        boolean belongsToChild = user.getChildren().stream()
                .anyMatch(s -> s.getId().equals(fee.getStudent().getId()));
        
        if (!belongsToChild || fee.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Error: No tiene autorización para este comprobante o el pago no ha sido realizado.");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLUE);
            Paragraph title = new Paragraph("Lumina Academic", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Spacer

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph subTitle = new Paragraph("COMPROBANTE DE PAGO", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Fecha de Emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("Número de Operación: #PAY-" + fee.getId() + "-" + System.currentTimeMillis() % 10000));
            document.add(new Paragraph(" "));

            // Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            PdfPCell cell = new PdfPCell(new Phrase("DETALLES DEL ESTUDIANTE", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setColspan(2);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);

            table.addCell("Estudiante:");
            table.addCell(fee.getStudent().getFirstName() + " " + fee.getStudent().getLastName());
            table.addCell("DNI Estudiante:");
            table.addCell(fee.getStudent().getDni());
            table.addCell("Nivel / Grado:");
            table.addCell(fee.getStudent().getLevel() + " - " + fee.getStudent().getGrade());

            PdfPCell cell2 = new PdfPCell(new Phrase("DETALLES DEL PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell2.setColspan(2);
            cell2.setBackgroundColor(Color.LIGHT_GRAY);
            cell2.setPadding(5);
            table.addCell(cell2);

            table.addCell("Concepto:");
            table.addCell(fee.getDescription() != null ? fee.getDescription() : "Pensión " + fee.getMonth() + " " + fee.getYear());
            table.addCell("Fecha de Pago:");
            table.addCell(fee.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            table.addCell("Método de Pago:");
            table.addCell("Tarjeta de Crédito (Simulada)");
            
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL PAGADO:"));
            totalLabel.setPadding(10);
            table.addCell(totalLabel);

            PdfPCell totalAmount = new PdfPCell(new Phrase("S/ " + String.format("%.2f", fee.getAmount()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE)));
            totalAmount.setPadding(10);
            totalAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalAmount);

            document.add(table);

            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("¡Gracias por su puntualidad!", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }

    @Override
    public CreditHistoryResponse getCreditHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        List<MonthlyFee> allFees = user.getChildren().stream()
                .flatMap(student -> monthlyFeeRepository.findByStudentIdOrderByYearAscDueDateAsc(student.getId()).stream())
                .collect(Collectors.toList());

        int totalPaid = 0;
        int onTime = 0;
        int late = 0;
        int pending = 0;

        for (MonthlyFee fee : allFees) {
            if (fee.getStatus() == PaymentStatus.PAID) {
                totalPaid++;
                if (fee.getPaymentDate() != null && !fee.getPaymentDate().toLocalDate().isAfter(fee.getDueDate())) {
                    onTime++;
                } else {
                    late++;
                }
            } else {
                pending++;
            }
        }

        double percentage = totalPaid == 0 ? 0 : (double) onTime / totalPaid * 100;
        
        String level;
        String color;
        if (totalPaid == 0) {
            level = "Sin historial";
            color = "#94a3b8"; // slate-400
        } else if (percentage >= 90) {
            level = "Excelente";
            color = "#22c55e"; // green-500
        } else if (percentage >= 70) {
            level = "Bueno";
            color = "#3b82f6"; // blue-500
        } else if (percentage >= 50) {
            level = "Regular";
            color = "#f59e0b"; // amber-500
        } else {
            level = "En Riesgo";
            color = "#ef4444"; // red-500
        }

        return CreditHistoryResponse.builder()
                .totalPaid(totalPaid)
                .onTimePayments(onTime)
                .latePayments(late)
                .pendingPayments(pending)
                .punctualityPercentage(percentage)
                .creditLevel(level)
                .color(color)
                .build();
    }

    @Override
    public byte[] exportStudentHistory(String username, Long studentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        // Security check
        if (!student.getParent().getId().equals(user.getId())) {
            throw new RuntimeException("Error: Student does not belong to this parent.");
        }

        return paymentService.generatePaymentHistoryPdf(studentId);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

package com.educativo.api.config;

import com.educativo.api.entity.*;
import com.educativo.api.repository.FeeConfigRepository;
import com.educativo.api.repository.MonthlyFeeRepository;
import com.educativo.api.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Configuration
@EnableScheduling
public class FeeGenerationScheduler {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    MonthlyFeeRepository monthlyFeeRepository;

    @Autowired
    FeeConfigRepository feeConfigRepository;

    @Autowired
    com.educativo.api.service.EmailService emailService;

    // Run at 00:00 on the 1st day of every month from April to December
    @Scheduled(cron = "0 0 0 1 4-12 ?")
    @Transactional
    public void generateMonthlyPensions() {
        List<Student> students = studentRepository.findAll();
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        
        for (Student student : students) {
            FeeConfig config = feeConfigRepository.findByLevel(student.getLevel()).orElse(null);
            if (config == null) continue;

            MonthlyFee pension = MonthlyFee.builder()
                    .month(translateMonth(now.getMonthValue()))
                    .year(year)
                    .amount(config.getMonthlyAmount())
                    .status(PaymentStatus.PENDING)
                    .type(FeeType.PENSION)
                    .dueDate(LocalDate.of(year, now.getMonthValue(), config.getDueDay()))
                    .student(student)
                    .build();

            monthlyFeeRepository.save(pension);
        }
        System.out.println("Automated Monthly Pensions generated for " + now.getMonth());
    }

    // Run daily at 08:00 to check for upcoming payments
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void sendPaymentReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(3);
        List<MonthlyFee> upcomingFees = monthlyFeeRepository.findAll().stream()
                .filter(f -> f.getStatus() == PaymentStatus.PENDING && f.getDueDate().equals(reminderDate))
                .toList();

        for (MonthlyFee fee : upcomingFees) {
            emailService.sendPaymentReminderEmail(fee.getStudent().getParent(), fee);
        }
        
        if (!upcomingFees.isEmpty()) {
            System.out.println("Sent " + upcomingFees.size() + " payment reminders for " + reminderDate);
        }
    }

    private String translateMonth(int value) {
        String[] months = {"ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SETIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"};
        return months[value - 1];
    }
}

package com.educativo.api.service.impl;

import com.educativo.api.entity.MonthlyFee;
import com.educativo.api.entity.User;
import com.educativo.api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "no-reply@lumina-academic.com";

    @Override
    public void sendPaymentSuccessEmail(User parent, MonthlyFee fee) {
        if (parent.getEmail() == null || parent.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(parent.getEmail());
        message.setSubject("Confirmación de Pago Exitoso - Lumina Academic");
        
        String body = String.format(
            "Estimado(a) %s %s,\n\n" +
            "Le informamos que el pago de la %s del estudiante %s %s ha sido procesado exitosamente.\n\n" +
            "Detalles del pago:\n" +
            "- Concepto: %s\n" +
            "- Monto: S/ %.2f\n" +
            "- Fecha de pago: %s\n\n" +
            "Gracias por su puntualidad.\n\n" +
            "Atentamente,\n" +
            "Lumina Academic",
            parent.getFirstName(), parent.getLastName(),
            fee.getType(), fee.getStudent().getFirstName(), fee.getStudent().getLastName(),
            fee.getMonth() + " " + fee.getYear(),
            fee.getAmount(),
            fee.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );

        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendPaymentFailureEmail(User parent, MonthlyFee fee, String reason) {
        if (parent.getEmail() == null || parent.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(parent.getEmail());
        message.setSubject("Aviso: Intento de Pago Fallido - Lumina Academic");

        String body = String.format(
            "Estimado(a) %s %s,\n\n" +
            "Le informamos que el intento de pago para la %s del estudiante %s %s no pudo ser procesado.\n\n" +
            "Detalles:\n" +
            "- Concepto: %s\n" +
            "- Monto: S/ %.2f\n" +
            "- Motivo del fallo: %s\n\n" +
            "Por favor, verifique sus datos de pago o intente con otra tarjeta.\n\n" +
            "Atentamente,\n" +
            "Lumina Academic",
            parent.getFirstName(), parent.getLastName(),
            fee.getType(), fee.getStudent().getFirstName(), fee.getStudent().getLastName(),
            fee.getMonth() + " " + fee.getYear(),
            fee.getAmount(),
            reason
        );

        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendPaymentReminderEmail(User parent, MonthlyFee fee) {
        if (parent.getEmail() == null || parent.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(parent.getEmail());
        message.setSubject("Recordatorio de Pago Próximo a Vencer - Lumina Academic");

        String body = String.format(
            "Estimado(a) %s %s,\n\n" +
            "Le recordamos que tiene un pago pendiente próximo a vencer para el estudiante %s %s.\n\n" +
            "Detalles del pago:\n" +
            "- Concepto: %s\n" +
            "- Monto: S/ %.2f\n" +
            "- Fecha de vencimiento: %s\n\n" +
            "Evite recargos realizando su pago a tiempo a través de nuestra plataforma.\n\n" +
            "Atentamente,\n" +
            "Lumina Academic",
            parent.getFirstName(), parent.getLastName(),
            fee.getStudent().getFirstName(), fee.getStudent().getLastName(),
            fee.getMonth() + " " + fee.getYear(),
            fee.getAmount(),
            fee.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        message.setText(body);
        mailSender.send(message);
    }
}

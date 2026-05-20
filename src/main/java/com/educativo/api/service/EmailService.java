package com.educativo.api.service;

import com.educativo.api.entity.MonthlyFee;
import com.educativo.api.entity.User;

public interface EmailService {
    void sendPaymentSuccessEmail(User parent, MonthlyFee fee);
    void sendPaymentFailureEmail(User parent, MonthlyFee fee, String reason);
    void sendPaymentReminderEmail(User parent, MonthlyFee fee);
}

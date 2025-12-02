package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.SmsNotificationModel;

public interface ISmsNotificationPort {
    void sendSms(SmsNotificationModel smsNotification);
}


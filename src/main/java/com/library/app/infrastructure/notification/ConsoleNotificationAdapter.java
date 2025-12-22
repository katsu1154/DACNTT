package com.library.app.infrastructure.notification;

import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationAdapter implements NotificationAdapter {

    @Override
    public void sendNotification(String message, String recipient) {
        // In ra Console giả lập việc đang gửi tin nhắn
        System.out.println("\n================ [NOTIFICATION SYSTEM] ================");
        System.out.println("📨 Đang gửi tới: " + recipient);
        System.out.println("📝 Nội dung: " + message);
        System.out.println("✅ Trạng thái: Đã gửi thành công!");
        System.out.println("=======================================================\n");
    }
}
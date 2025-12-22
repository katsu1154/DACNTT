package com.library.app.infrastructure.notification;

public interface NotificationAdapter {
    /**
     * Hàm gửi thông báo chuẩn của hệ thống.
     * @param message Nội dung thông báo
     * @param recipient Người nhận (Email hoặc SĐT)
     */
    void sendNotification(String message, String recipient);
}
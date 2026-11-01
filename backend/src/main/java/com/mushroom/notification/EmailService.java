package com.mushroom.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        log.info("Gửi email xác minh tài khoản tới: {} với token: {}", toEmail, token);
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("[Nấm Thông Minh] Xác minh địa chỉ email");
                message.setText(String.format(
                        "Xin chào %s,\n\n" +
                                "Cảm ơn bạn đã đăng ký tài khoản. Vui lòng sử dụng mã/token xác minh sau:\n\n" +
                                "%s\n\n" +
                                "Token có hiệu lực trong vòng 15 phút.\n" +
                                "Trân trọng,\nHệ thống Quản lý Nuôi trồng Nấm Thông minh",
                        fullName, token
                ));
                mailSender.send(message);
            }
        } catch (Exception e) {
            log.error("Không thể gửi email xác minh tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("Gửi email đặt lại mật khẩu tới: {} với token: {}", toEmail, token);
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("[Nấm Thông Minh] Yêu cầu đặt lại mật khẩu");
                message.setText(String.format(
                        "Xin chào,\n\n" +
                                "Hệ thống đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n" +
                                "Mã xác thực đặt lại mật khẩu của bạn là:\n\n" +
                                "%s\n\n" +
                                "Token có hiệu lực trong vòng 15 phút.\n" +
                                "Nếu bạn không yêu cầu điều này, xin vui lòng bỏ qua email.\n" +
                                "Trân trọng,\nHệ thống Quản lý Nuôi trồng Nấm Thông minh",
                        token
                ));
                mailSender.send(message);
            }
        } catch (Exception e) {
            log.error("Không thể gửi email đặt lại mật khẩu tới {}: {}", toEmail, e.getMessage());
        }
    }
}

package com.fitpulse.app.common.mail;

import com.fitpulse.app.common.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailProperties mailProperties;

    public void sendMail(String to, String subject, String content) {
        if (!mailProperties.isConfigured()) {
            log.info("[MailService] 邮件未配置，跳过发送。to={}, subject={}, content={}", to, subject, content);
            return;
        }

        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(mailProperties.getSmtpHost());
            sender.setPort(mailProperties.getSmtpPort());
            sender.setUsername(mailProperties.getUsername());
            sender.setPassword(mailProperties.getPassword());
            sender.setDefaultEncoding(mailProperties.getEncoding());

            Properties props = sender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", mailProperties.getSmtpHost());
            props.put("mail.smtp.timeout", String.valueOf(mailProperties.getTimeout()));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            sender.send(message);
            log.info("[MailService] 邮件发送成功。to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("[MailService] 邮件发送失败。to={}, subject={}, error={}", to, subject, e.getMessage(), e);
        }
    }
}

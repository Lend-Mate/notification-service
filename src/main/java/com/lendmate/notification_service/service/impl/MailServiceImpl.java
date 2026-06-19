package com.lendmate.notification_service.service.impl;

import com.lendmate.notification_service.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendPlainText(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true means this is HTML
            mailSender.send(message);
        } catch (MessagingException ex){
            log.debug("exception: " + ex);
        }
    }

    @Override
    public void sendOrderConfirmation(String to, Long orderId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("orderId", orderId);

            String html = templateEngine.process("order-confirmation", context);
            helper.setTo(to);
            helper.setSubject("Siparişiniz Alındı!");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Mail gönderildi: {}", to);
        } catch (MessagingException e) {
            log.error("Mail gönderilemedi: {}", e.getMessage());
        }
    }
}

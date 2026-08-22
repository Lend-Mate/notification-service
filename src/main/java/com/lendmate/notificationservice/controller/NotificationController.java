package com.lendmate.notificationservice.controller;

import com.lendmate.notificationservice.dto.request.MailRequest;
import com.lendmate.notificationservice.dto.request.NotificationRequest;
import com.lendmate.notificationservice.dto.response.NotificationResponse;
import com.lendmate.notificationservice.service.MailService;
import com.lendmate.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final MailService mailService;

    @GetMapping("/health")
    public String healthCheck() {
        return "notification service is up and working...";
    }

    @PostMapping
    public ResponseEntity<Void> saveNotification(
            @Valid @RequestBody NotificationRequest request) {

        notificationService.saveNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {

        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id) {

        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sendPlainText")
    public void sendPlainText(@RequestBody MailRequest request) {
        mailService.sendPlainText(request.to(), request.subject(), request.body());
    }

    @PostMapping("/sendHtml")
    public void sendHtml(@RequestBody MailRequest request) {
        mailService.sendHtml(request.to(), request.subject(), request.body());
    }
}
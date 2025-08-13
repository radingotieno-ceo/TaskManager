package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTaskAssignmentEmail(Task task, User assignedUser) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(assignedUser.getEmail());
            message.setSubject("New Task Assigned: " + task.getTitle());
            message.setText(buildTaskAssignmentEmailContent(task, assignedUser));
            
            mailSender.send(message);
            log.info("Task assignment email sent successfully to: {}", assignedUser.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send task assignment email to: {}", assignedUser.getEmail(), e);
            throw e;
        }
    }
    
    private String buildTaskAssignmentEmailContent(Task task, User assignedUser) {
        return String.format(
            "Hello %s,\n\n" +
            "You have been assigned a new task:\n\n" +
            "Task: %s\n" +
            "Description: %s\n" +
            "Due Date: %s\n" +
            "Project: %s\n\n" +
            "Please log into the Task Management System to view more details.\n\n" +
            "Best regards,\n" +
            "Task Management System",
            assignedUser.getName(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate().toString(),
            task.getProject().getName()
        );
    }
}


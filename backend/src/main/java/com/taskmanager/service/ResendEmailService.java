package com.taskmanager.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;

import java.time.format.DateTimeFormatter;

@Service
public class ResendEmailService {
    
    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    
    @Value("${resend.api-key}")
    private String apiKey;
    
    @Value("${resend.from-email}")
    private String fromEmail;
    
    private Resend resend;
    
    public ResendEmailService(@Value("${resend.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.resend = new Resend(apiKey);
    }
    
    /**
     * Send task assignment notification email
     */
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTaskAssignmentEmail(Task task, User assignedUser) {
        try {
            String subject = "New Task Assigned: " + task.getTitle();
            String htmlContent = buildTaskAssignmentHtmlEmail(task, assignedUser);
            
            SendEmailRequest emailOptions = SendEmailRequest.builder()
                .from(fromEmail)
                .to(assignedUser.getEmail())
                .subject(subject)
                .html(htmlContent)
                .build();
            
            SendEmailResponse response = resend.emails().send(emailOptions);
            log.info("Task assignment email sent successfully to: {} with ID: {}", 
                    assignedUser.getEmail(), response.getId());
            
        } catch (ResendException e) {
            log.error("Failed to send task assignment email to: {}", assignedUser.getEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send task update notification email
     */
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTaskUpdateEmail(Task task, User user, String updateType) {
        try {
            String subject = "Task Updated: " + task.getTitle();
            String htmlContent = buildTaskUpdateHtmlEmail(task, user, updateType);
            
            SendEmailRequest emailOptions = SendEmailRequest.builder()
                .from(fromEmail)
                .to(user.getEmail())
                .subject(subject)
                .html(htmlContent)
                .build();
            
            SendEmailResponse response = resend.emails().send(emailOptions);
            log.info("Task update email sent successfully to: {} with ID: {}", 
                    user.getEmail(), response.getId());
            
        } catch (ResendException e) {
            log.error("Failed to send task update email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send task due reminder email
     */
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendTaskDueReminderEmail(Task task, User user) {
        try {
            String subject = "Task Due Soon: " + task.getTitle();
            String htmlContent = buildTaskDueReminderHtmlEmail(task, user);
            
            SendEmailRequest emailOptions = SendEmailRequest.builder()
                .from(fromEmail)
                .to(user.getEmail())
                .subject(subject)
                .html(htmlContent)
                .build();
            
            SendEmailResponse response = resend.emails().send(emailOptions);
            log.info("Task due reminder email sent successfully to: {} with ID: {}", 
                    user.getEmail(), response.getId());
            
        } catch (ResendException e) {
            log.error("Failed to send task due reminder email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send custom notification email
     */
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendCustomEmail(String toEmail, String subject, String htmlContent) {
        try {
            SendEmailRequest emailOptions = SendEmailRequest.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(htmlContent)
                .build();
            
            SendEmailResponse response = resend.emails().send(emailOptions);
            log.info("Custom email sent successfully to: {} with ID: {}", toEmail, response.getId());
            
        } catch (ResendException e) {
            log.error("Failed to send custom email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Build HTML email template for task assignment
     */
    private String buildTaskAssignmentHtmlEmail(Task task, User assignedUser) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>New Task Assigned</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #10b981, #3b82f6); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .task-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .task-title { color: #1f2937; font-size: 18px; font-weight: bold; margin-bottom: 10px; }
                    .task-detail { margin: 8px 0; }
                    .task-label { font-weight: bold; color: #6b7280; }
                    .task-value { color: #1f2937; }
                    .cta-button { display: inline-block; background: linear-gradient(135deg, #10b981, #3b82f6); color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎯 New Task Assigned</h1>
                        <p>You have been assigned a new task</p>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>A new task has been assigned to you. Here are the details:</p>
                        
                        <div class="task-card">
                            <div class="task-title">%s</div>
                            <div class="task-detail">
                                <span class="task-label">Description:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Project:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Status:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Priority:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Due Date:</span>
                                <span class="task-value">%s</span>
                            </div>
                        </div>
                        
                        <p>Please log into your dashboard to view and update this task.</p>
                        
                        <a href="http://localhost:4200/dashboard" class="cta-button">View Task</a>
                        
                        <div class="footer">
                            <p>Best regards,<br>KAROOTH Task Manager Team</p>
                            <p>This is an automated notification. Please do not reply to this email.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                assignedUser.getName(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getName(),
                task.getStatus().toString(),
                task.getPriority().toString(),
                task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
    }
    
    /**
     * Build HTML email template for task update
     */
    private String buildTaskUpdateHtmlEmail(Task task, User user, String updateType) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Task Updated</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f59e0b, #ef4444); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .task-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .task-title { color: #1f2937; font-size: 18px; font-weight: bold; margin-bottom: 10px; }
                    .task-detail { margin: 8px 0; }
                    .task-label { font-weight: bold; color: #6b7280; }
                    .task-value { color: #1f2937; }
                    .update-type { background: #fef3c7; color: #92400e; padding: 8px 12px; border-radius: 6px; display: inline-block; margin: 10px 0; }
                    .cta-button { display: inline-block; background: linear-gradient(135deg, #f59e0b, #ef4444); color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📝 Task Updated</h1>
                        <p>A task has been updated</p>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>A task has been updated. Here are the details:</p>
                        
                        <div class="update-type">Update Type: %s</div>
                        
                        <div class="task-card">
                            <div class="task-title">%s</div>
                            <div class="task-detail">
                                <span class="task-label">Project:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Current Status:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Priority:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Due Date:</span>
                                <span class="task-value">%s</span>
                            </div>
                        </div>
                        
                        <p>Please log into your dashboard to view the updated task.</p>
                        
                        <a href="http://localhost:4200/dashboard" class="cta-button">View Task</a>
                        
                        <div class="footer">
                            <p>Best regards,<br>KAROOTH Task Manager Team</p>
                            <p>This is an automated notification. Please do not reply to this email.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                updateType,
                task.getTitle(),
                task.getProject().getName(),
                task.getStatus().toString(),
                task.getPriority().toString(),
                task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
    }
    
    /**
     * Build HTML email template for task due reminder
     */
    private String buildTaskDueReminderHtmlEmail(Task task, User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Task Due Soon</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .task-card { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .task-title { color: #1f2937; font-size: 18px; font-weight: bold; margin-bottom: 10px; }
                    .task-detail { margin: 8px 0; }
                    .task-label { font-weight: bold; color: #6b7280; }
                    .task-value { color: #1f2937; }
                    .urgent { background: #fee2e2; color: #991b1b; padding: 8px 12px; border-radius: 6px; display: inline-block; margin: 10px 0; font-weight: bold; }
                    .cta-button { display: inline-block; background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ Task Due Soon</h1>
                        <p>This is a reminder for an upcoming deadline</p>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>This is a reminder that you have a task due soon:</p>
                        
                        <div class="urgent">⚠️ URGENT: Task Due Soon</div>
                        
                        <div class="task-card">
                            <div class="task-title">%s</div>
                            <div class="task-detail">
                                <span class="task-label">Description:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Project:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Current Status:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Priority:</span>
                                <span class="task-value">%s</span>
                            </div>
                            <div class="task-detail">
                                <span class="task-label">Due Date:</span>
                                <span class="task-value">%s</span>
                            </div>
                        </div>
                        
                        <p>Please update the task status or request an extension if needed.</p>
                        
                        <a href="http://localhost:4200/dashboard" class="cta-button">Update Task</a>
                        
                        <div class="footer">
                            <p>Best regards,<br>KAROOTH Task Manager Team</p>
                            <p>This is an automated notification. Please do not reply to this email.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getName(),
                task.getStatus().toString(),
                task.getPriority().toString(),
                task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
    }
    
    /**
     * Recovery method for failed email attempts
     */
    @Recover
    public void recoverEmailFailure(Exception e, String toEmail, String subject, String htmlContent) {
        log.error("Failed to send email to {} after retries: {}", toEmail, e.getMessage());
        // In production, you might want to:
        // 1. Store failed emails in a database for retry
        // 2. Send to a fallback email service
        // 3. Create a notification in the system
    }
}

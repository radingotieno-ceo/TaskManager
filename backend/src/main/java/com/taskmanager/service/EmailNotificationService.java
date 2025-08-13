package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from:noreply@karooth.com}")
    private String fromEmail;

    @Value("${app.email.task-assignment.subject:New Task Assigned: {taskTitle}}")
    private String taskAssignmentSubject;

    @Value("${app.email.task-update.subject:Task Updated: {taskTitle}}")
    private String taskUpdateSubject;

    @Value("${app.email.task-due-reminder.subject:Task Due Soon: {taskTitle}}")
    private String taskDueReminderSubject;

    /**
     * Send email notification when a task is assigned to a user
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendTaskAssignmentNotification(Task task, User assignedUser) {
        String subject = taskAssignmentSubject.replace("{taskTitle}", task.getTitle());
        String content = buildTaskAssignmentEmail(task, assignedUser);
        
        sendEmail(assignedUser.getEmail(), subject, content);
    }

    /**
     * Send email notification when a task status is updated
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendTaskUpdateNotification(Task task, User user, String updateType) {
        String subject = taskUpdateSubject.replace("{taskTitle}", task.getTitle());
        String content = buildTaskUpdateEmail(task, user, updateType);
        
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send email reminder for tasks due soon
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendTaskDueReminder(Task task, User user) {
        String subject = taskDueReminderSubject.replace("{taskTitle}", task.getTitle());
        String content = buildTaskDueReminderEmail(task, user);
        
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send custom email notification
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendCustomNotification(String toEmail, String subject, String content) {
        sendEmail(toEmail, subject, content);
    }

    /**
     * Core email sending method
     */
    private void sendEmail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
    }

    /**
     * Build task assignment email content
     */
    private String buildTaskAssignmentEmail(Task task, User assignedUser) {
        StringBuilder content = new StringBuilder();
        content.append("Hello ").append(assignedUser.getName()).append(",\n\n");
        content.append("You have been assigned a new task:\n\n");
        content.append("Task: ").append(task.getTitle()).append("\n");
        content.append("Description: ").append(task.getDescription()).append("\n");
        content.append("Project: ").append(task.getProject().getName()).append("\n");
        content.append("Status: ").append(task.getStatus()).append("\n");
        content.append("Due Date: ").append(task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");
        content.append("Please log into your dashboard to view and update this task.\n\n");
        content.append("Best regards,\nKAROOTH Task Manager Team");
        
        return content.toString();
    }

    /**
     * Build task update email content
     */
    private String buildTaskUpdateEmail(Task task, User user, String updateType) {
        StringBuilder content = new StringBuilder();
        content.append("Hello ").append(user.getName()).append(",\n\n");
        content.append("A task has been updated:\n\n");
        content.append("Task: ").append(task.getTitle()).append("\n");
        content.append("Update Type: ").append(updateType).append("\n");
        content.append("New Status: ").append(task.getStatus()).append("\n");
        content.append("Project: ").append(task.getProject().getName()).append("\n");
        content.append("Due Date: ").append(task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");
        content.append("Please log into your dashboard to view the updated task.\n\n");
        content.append("Best regards,\nKAROOTH Task Manager Team");
        
        return content.toString();
    }

    /**
     * Build task due reminder email content
     */
    private String buildTaskDueReminderEmail(Task task, User user) {
        StringBuilder content = new StringBuilder();
        content.append("Hello ").append(user.getName()).append(",\n\n");
        content.append("This is a reminder that you have a task due soon:\n\n");
        content.append("Task: ").append(task.getTitle()).append("\n");
        content.append("Description: ").append(task.getDescription()).append("\n");
        content.append("Project: ").append(task.getProject().getName()).append("\n");
        content.append("Current Status: ").append(task.getStatus()).append("\n");
        content.append("Due Date: ").append(task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");
        content.append("Please update the task status or request an extension if needed.\n\n");
        content.append("Best regards,\nKAROOTH Task Manager Team");
        
        return content.toString();
    }

    /**
     * Recovery method for failed email attempts
     */
    @Recover
    public void recoverEmailFailure(Exception e, String toEmail, String subject, String content) {
        // Log the failure for manual follow-up
        System.err.println("Failed to send email to " + toEmail + " after retries: " + e.getMessage());
        // In production, you might want to:
        // 1. Store failed emails in a database for retry
        // 2. Send to a fallback email service
        // 3. Create a notification in the system
    }
}

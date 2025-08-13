# Email Notification Setup Guide

## Overview
This guide explains how to set up email notifications for the KAROOTH Task Manager application using different email service providers.

## Email Service Options

### 1. Gmail SMTP (Recommended for Development)
**Pros:** Free, Easy setup, Good for testing
**Cons:** Limited to 500 emails/day on free tier

#### Setup Steps:
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password:**
   - Go to Google Account Settings
   - Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
3. **Configure Environment Variables:**
   ```bash
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-16-digit-app-password
   MAIL_FROM=noreply@karooth.com
   ```

### 2. SendGrid (Recommended for Production)
**Pros:** Excellent deliverability, Good free tier, Analytics
**Cons:** Paid plans required for high volume

#### Setup Steps:
1. **Create SendGrid Account:**
   - Sign up at [sendgrid.com](https://sendgrid.com)
   - Verify your email address
2. **Create API Key:**
   - Settings → API Keys → Create API Key
   - Choose "Full Access" or "Restricted Access" (Mail Send)
3. **Configure Environment Variables:**
   ```bash
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=your-sendgrid-api-key
   MAIL_FROM=verified-sender@yourdomain.com
   ```
4. **Update application.yml:**
   ```yaml
   spring:
     mail:
       host: smtp.sendgrid.net
       port: 587
   ```

### 3. Mailgun
**Pros:** Good free tier, Simple API
**Cons:** Limited features on free tier

#### Setup Steps:
1. **Create Mailgun Account:**
   - Sign up at [mailgun.com](https://mailgun.com)
   - Add your domain or use sandbox domain
2. **Get API Key:**
   - Sending → API Keys → Private API Key
3. **Configure Environment Variables:**
   ```bash
   MAIL_USERNAME=postmaster@yourdomain.mailgun.org
   MAIL_PASSWORD=your-mailgun-api-key
   MAIL_FROM=noreply@yourdomain.com
   ```

### 4. Amazon SES (AWS)
**Pros:** Very cost-effective, High deliverability
**Cons:** Requires AWS account, More complex setup

#### Setup Steps:
1. **AWS Account Setup:**
   - Create AWS account
   - Navigate to Amazon SES
2. **Verify Email Address:**
   - Add and verify your sender email
3. **Create SMTP Credentials:**
   - SMTP Settings → Create SMTP Credentials
4. **Configure Environment Variables:**
   ```bash
   MAIL_USERNAME=your-smtp-username
   MAIL_PASSWORD=your-smtp-password
   MAIL_FROM=verified-email@yourdomain.com
   ```
5. **Update application.yml:**
   ```yaml
   spring:
     mail:
       host: email-smtp.us-east-1.amazonaws.com
       port: 587
   ```

## Environment Configuration

### Development (Local)
Create a `.env` file in your project root:
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@karooth.com
```

### Production (Docker)
Update your `docker-compose.yml`:
```yaml
services:
  backend:
    environment:
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
      - MAIL_FROM=${MAIL_FROM}
```

## Testing Email Configuration

### 1. Test Endpoint
Add this to your `TaskController` for testing:
```java
@PostMapping("/test-email")
public ResponseEntity<String> testEmail(@RequestParam String email) {
    try {
        emailNotificationService.sendCustomNotification(
            email, 
            "Test Email from KAROOTH", 
            "This is a test email from KAROOTH Task Manager."
        );
        return ResponseEntity.ok("Test email sent successfully!");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
    }
}
```

### 2. Manual Testing
```bash
# Test with curl
curl -X POST "http://localhost:8080/api/tasks/test-email?email=test@example.com"
```

## Email Templates

The application supports three types of email notifications:

1. **Task Assignment:** Sent when a task is assigned to a user
2. **Task Update:** Sent when a task status is updated
3. **Task Due Reminder:** Sent for tasks due soon

### Customizing Email Content
Edit the email templates in `EmailNotificationService.java`:
- `buildTaskAssignmentEmail()`
- `buildTaskUpdateEmail()`
- `buildTaskDueReminderEmail()`

## Troubleshooting

### Common Issues:

1. **Authentication Failed:**
   - Check username/password
   - Ensure 2FA is enabled (Gmail)
   - Verify API key (SendGrid/Mailgun)

2. **Connection Timeout:**
   - Check firewall settings
   - Verify SMTP host and port
   - Check network connectivity

3. **Email Not Delivered:**
   - Check spam folder
   - Verify sender email is authenticated
   - Check email service logs

### Debug Mode:
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    org.springframework.mail: DEBUG
    com.taskmanager.service.EmailNotificationService: DEBUG
```

## Security Best Practices

1. **Never commit credentials to version control**
2. **Use environment variables for sensitive data**
3. **Enable SSL/TLS for email connections**
4. **Regularly rotate API keys and passwords**
5. **Monitor email sending logs for suspicious activity**

## Cost Considerations

| Service | Free Tier | Paid Plans |
|---------|-----------|------------|
| Gmail SMTP | 500 emails/day | N/A |
| SendGrid | 100 emails/day | $15/month for 50k |
| Mailgun | 5k emails/month (3 months) | $35/month for 50k |
| Amazon SES | $0.10 per 1k emails | Pay per use |

Choose based on your expected email volume and budget requirements.

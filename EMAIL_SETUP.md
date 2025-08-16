# Email Notification Service Setup Guide

## Overview
This project uses Resend (https://resend.com) for sending email notifications to users when tasks are assigned, updated, or due soon.

## Configuration

### 1. Resend Account Setup
1. Sign up for a free account at [Resend](https://resend.com)
2. Verify your domain or use the provided sandbox domain
3. Get your API key from the Resend dashboard

### 2. Environment Configuration
Create a `.env` file in the backend directory with the following variables:

```env
# Resend Email Service Configuration
RESEND_API_KEY=your-actual-resend-api-key-here
RESEND_FROM_EMAIL=Felix@radingtechnologies.co.ke
```

### 3. Application Configuration
The email service is configured in `backend/src/main/resources/application.yml`:

```yaml
# Resend Email Configuration
resend:
  api-key: ${RESEND_API_KEY:your-resend-api-key}
  from-email: Felix@radingtechnologies.co.ke

# Email notification settings
app:
  email:
    from: ${RESEND_FROM_EMAIL:Felix@radingtechnologies.co.ke}
```

## Email Templates

### Task Assignment Email
- **Trigger**: When a task is assigned to a user
- **Template**: Beautiful HTML email with task details
- **Features**: 
  - Task title, description, project, status, priority, due date
  - Call-to-action button to view task
  - Professional styling with gradients

### Task Update Email
- **Trigger**: When a task status or details are updated
- **Template**: HTML email with update information
- **Features**:
  - Update type indicator
  - Current task status and details
  - Link to view updated task

### Task Due Reminder Email
- **Trigger**: When a task is due soon (can be scheduled)
- **Template**: Urgent reminder email
- **Features**:
  - Urgent warning indicator
  - Task details and due date
  - Call-to-action to update task

## Implementation Details

### Backend Services
- `ResendEmailService`: Main email service using Resend API
- `TaskService`: Integrates email notifications with task operations
- Async processing with retry mechanism
- Error handling and logging

### Email Features
- **HTML Templates**: Professional, responsive email designs
- **Async Processing**: Non-blocking email sending
- **Retry Mechanism**: Automatic retry on failure (3 attempts)
- **Error Recovery**: Graceful handling of email failures
- **Logging**: Comprehensive logging for debugging

### Security
- API key stored in environment variables
- No sensitive data in email templates
- Secure email delivery via Resend

## Testing

### 1. Test Email Sending
```bash
# Start the backend server
cd backend
./gradlew bootRun

# Create a task assignment through the API
curl -X POST http://localhost:8080/api/tasks/create-and-assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Test Task",
    "description": "Test task description",
    "dueDate": "2024-12-31",
    "projectId": 1,
    "userId": 2,
    "priority": "MEDIUM"
  }'
```

### 2. Check Logs
Monitor the application logs for email sending status:
```bash
# Look for email success/failure messages
tail -f backend/logs/application.log
```

## Troubleshooting

### Common Issues

1. **API Key Invalid**
   - Verify your Resend API key is correct
   - Check if the key has proper permissions

2. **Domain Not Verified**
   - Verify your sending domain in Resend dashboard
   - Use sandbox domain for testing

3. **Email Not Received**
   - Check spam folder
   - Verify recipient email address
   - Check Resend dashboard for delivery status

4. **Rate Limiting**
   - Resend has rate limits (check your plan)
   - Implement proper error handling

### Debug Mode
Enable debug logging by setting in `application.yml`:
```yaml
logging:
  level:
    com.taskmanager.service.ResendEmailService: DEBUG
    com.resend: DEBUG
```

## Production Deployment

### Environment Variables
Set these in your production environment:
```bash
export RESEND_API_KEY=your-production-api-key
export RESEND_FROM_EMAIL=Felix@radingtechnologies.co.ke
```

### Monitoring
- Monitor email delivery rates in Resend dashboard
- Set up alerts for email failures
- Review application logs regularly

## Support
For issues with:
- **Resend Service**: Contact Resend support
- **Application Integration**: Check application logs and configuration
- **Email Templates**: Modify HTML templates in `ResendEmailService.java`

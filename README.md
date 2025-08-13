# Task Management System

A full-stack task management application with role-based access control, built with Spring Boot and Angular.

## Features

- **User Authentication & Authorization**: JWT-based authentication with role-based access (ADMIN, MANAGER, USER)
- **Project Management**: Create, update, and delete projects (Admin/Manager only)
- **Task Management**: Create, assign, and update task status
- **Email Notifications**: Automatic email notifications when tasks are assigned
- **Modern UI**: Responsive Angular Material design
- **API Documentation**: Swagger/OpenAPI documentation
- **Docker Support**: Complete containerization with Docker Compose

## Tech Stack

### Backend
- **Spring Boot 3.2.0** - Main framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **MySQL 8.0** - Database
- **JWT** - Token-based authentication
- **Spring Mail** - Email notifications
- **Swagger/OpenAPI** - API documentation

### Frontend
- **Angular 17** - Frontend framework
- **Angular Material** - UI components
- **TypeScript** - Programming language
- **RxJS** - Reactive programming

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Nginx** - Web server and reverse proxy

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- Node.js 18+ (for local development)

### Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd task-management-system
   ```

2. **Set up email configuration (optional)**
   ```bash
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```

3. **Start the application**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - Database: localhost:3306

### Local Development

#### Backend Setup
1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Build the application**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

#### Frontend Setup
1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm start
   ```

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Projects (Admin/Manager only)
- `GET /api/projects` - Get all projects
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Tasks
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/assigned` - Get assigned tasks
- `GET /api/tasks/project/{projectId}` - Get tasks by project
- `POST /api/tasks` - Create task (Admin/Manager only)
- `PUT /api/tasks/{id}/assign` - Assign task (Manager only)
- `PUT /api/tasks/{id}/status` - Update task status

## User Roles

### Admin
- Full access to all features
- Can create, update, and delete projects
- Can create and assign tasks
- Can manage all users

### Manager
- Can create, update, and delete projects
- Can create and assign tasks
- Can view all tasks and projects

### User
- Can view assigned tasks only
- Can update status of assigned tasks
- Limited access to project information

## Database Schema

### Users Table
- `id` - Primary key
- `name` - User's full name
- `email` - Unique email address
- `password` - Encrypted password
- `role` - User role (ADMIN, MANAGER, USER)
- `created_at` - Account creation timestamp

### Projects Table
- `id` - Primary key
- `name` - Project name
- `description` - Project description
- `created_at` - Project creation timestamp

### Tasks Table
- `id` - Primary key
- `title` - Task title
- `description` - Task description
- `status` - Task status (TODO, IN_PROGRESS, DONE)
- `due_date` - Task due date
- `project_id` - Foreign key to projects
- `assigned_user_id` - Foreign key to users
- `created_at` - Task creation timestamp
- `updated_at` - Task last update timestamp

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-based Authorization**: Fine-grained access control
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Cross-origin resource sharing setup
- **Input Validation**: Comprehensive request validation
- **SQL Injection Protection**: JPA/Hibernate protection

## Email Configuration

The application sends email notifications when tasks are assigned. Configure your email settings in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

For Gmail, you'll need to:
1. Enable 2-factor authentication
2. Generate an app password
3. Use the app password in the configuration

## Development Guidelines

### Code Style
- Follow Java naming conventions for backend
- Use TypeScript best practices for frontend
- Implement proper error handling
- Add comprehensive logging

### Testing
- Write unit tests for services
- Add integration tests for controllers
- Test frontend components

### Security
- Never commit sensitive data
- Use environment variables for configuration
- Implement proper input validation
- Follow OWASP security guidelines

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Ensure MySQL is running
   - Check database credentials
   - Verify network connectivity

2. **JWT Token Issues**
   - Check token expiration
   - Verify secret key configuration
   - Ensure proper token format

3. **Email Notifications Not Working**
   - Verify email configuration
   - Check SMTP settings
   - Ensure app password is correct

4. **Frontend Build Issues**
   - Clear node_modules and reinstall
   - Check Angular version compatibility
   - Verify TypeScript configuration

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository or contact the development team.

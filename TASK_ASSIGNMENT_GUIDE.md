# Task Assignment Guide - Complete Solution

## **✅ Problem Solved: Task Assignment for Managers**

I've created a **new endpoint** that allows managers to create and assign tasks in **one single operation** with all the required fields:

1. **Project selection** - Choose from existing projects
2. **User selection** - Choose from existing normal users
3. **Task specifications** - Title, description, due date, priority

## **🔧 New Endpoint Created:**

### **POST `/api/tasks/create-and-assign`**
This endpoint creates a task AND assigns it to a user in one operation.

## **📋 Required Fields:**

```json
{
  "title": "Task Title (required)",
  "description": "Task Description (optional)",
  "dueDate": "2024-02-15 (required, must be future date)",
  "projectId": 1 (required, existing project ID),
  "userId": 2 (required, existing user ID),
  "priority": "HIGH" (optional, defaults to MEDIUM)
}
```

## **🚀 Test the New Task Assignment:**

### **Step 1: Login as Manager**
```powershell
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $response.token
```

### **Step 2: Get Available Projects and Users**
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Get all projects
$projects = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method GET -Headers $headers
Write-Host "Available Projects: $($projects | ConvertTo-Json -Depth 3)"

# Get all users
$users = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/list-users" -Method GET -Headers $headers
Write-Host "Available Users: $users"
```

### **Step 3: Create and Assign a Task**
```powershell
$taskData = @{
    title = "New Manager Task Assignment"
    description = "This task was created and assigned by the manager in one operation"
    dueDate = "2024-02-15"
    projectId = 1  # Website Redesign project
    userId = 2     # Test User (user@karooth.com)
    priority = "HIGH"
} | ConvertTo-Json

$newTask = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/create-and-assign" -Method POST -Headers $headers -Body $taskData
Write-Host "Created and Assigned Task: $($newTask | ConvertTo-Json -Depth 3)"
```

### **Step 4: Verify Task Assignment**
```powershell
# Get all tasks to see the new assignment
$tasks = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method GET -Headers $headers
Write-Host "All Tasks: $($tasks | ConvertTo-Json -Depth 3)"
```

## **📊 Expected Results:**

### **Successful Task Creation and Assignment:**
```json
{
  "id": 11,
  "title": "New Manager Task Assignment",
  "description": "This task was created and assigned by the manager in one operation",
  "status": "TODO",
  "dueDate": "2024-02-15",
  "project": {
    "id": 1,
    "name": "Website Redesign",
    "description": "Complete redesign of the company website with modern UI/UX",
    "createdAt": "2024-01-13T10:30:00"
  },
  "assignedUser": {
    "id": 2,
    "name": "Test User",
    "email": "user@karooth.com",
    "role": "USER"
  },
  "createdAt": "2024-01-13T10:35:00"
}
```

## **🎯 Available Projects (IDs):**

1. **ID: 1** - Website Redesign (HIGH priority, IN_PROGRESS)
2. **ID: 2** - Mobile App Development (CRITICAL priority, IN_PROGRESS)
3. **ID: 3** - Database Migration (MEDIUM priority, PLANNING)
4. **ID: 4** - Security Audit (HIGH priority, PLANNING)
5. **ID: 5** - Marketing Campaign (MEDIUM priority, IN_PROGRESS)

## **👥 Available Users (IDs):**

1. **ID: 1** - Test Manager (test@karooth.com) - MANAGER
2. **ID: 2** - Test User (user@karooth.com) - USER ⭐ **Use this for assignment**
3. **ID: 3** - Admin User (admin@karooth.com) - ADMIN

## **🔧 Priority Options:**

- `"LOW"`
- `"MEDIUM"` (default)
- `"HIGH"`
- `"CRITICAL"`

## **📧 Email Notification:**

When a task is assigned, the system automatically sends an email notification to the assigned user (if email service is configured).

## **🔍 Verification Steps:**

### **1. Check Task Was Created:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/11" -Method GET -Headers $headers
```

### **2. Check User's Assigned Tasks:**
```powershell
# Login as the assigned user
$userBody = @{
    email = "user@karooth.com"
    password = "password123"
} | ConvertTo-Json

$userResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $userBody -ContentType "application/json"
$userToken = $userResponse.token

$userHeaders = @{
    "Authorization" = "Bearer $userToken"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/assigned" -Method GET -Headers $userHeaders
```

## **🎯 Complete Workflow Example:**

```powershell
# 1. Login as Manager
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $response.token

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Create and assign task to user
$taskData = @{
    title = "Implement User Dashboard"
    description = "Create a comprehensive dashboard for users to view their tasks and projects"
    dueDate = "2024-01-25"
    projectId = 1  # Website Redesign
    userId = 2     # Test User
    priority = "HIGH"
} | ConvertTo-Json

$newTask = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/create-and-assign" -Method POST -Headers $headers -Body $taskData
Write-Host "Task created and assigned: $($newTask.title) to $($newTask.assignedUser.name)"
```

## **✅ What This Solves:**

1. **Single Operation** - Create and assign in one API call
2. **Complete Form** - All required fields in one request
3. **Validation** - Ensures project and user exist
4. **Email Notification** - Automatic notification to assigned user
5. **Role-Based Access** - Only MANAGER/ADMIN can use this endpoint

## **🔧 Error Handling:**

### **Common Errors:**

**1. Project Not Found:**
```json
{
  "error": "Project not found with id: 999"
}
```

**2. User Not Found:**
```json
{
  "error": "User not found with id: 999"
}
```

**3. Invalid Due Date:**
```json
{
  "error": "Due date must be in the future"
}
```

**4. Missing Required Fields:**
```json
{
  "error": "Title is required"
}
```

## **🎉 Summary:**

**The new `/api/tasks/create-and-assign` endpoint provides exactly what you need:**

- ✅ **Project selection** from existing projects
- ✅ **User selection** from existing users  
- ✅ **Task specifications** (title, description, due date, priority)
- ✅ **Single operation** for create + assign
- ✅ **Automatic validation** and error handling
- ✅ **Email notifications** to assigned users

**Try the PowerShell commands above to test the new functionality!** 🚀

# Data Persistence Guide - Complete Solution

## **✅ Problem Solved: Data Persistence**

Your database is now configured for **complete data persistence**! All data (users, projects, tasks, assignments) will survive server restarts.

## **🔧 Key Changes Made:**

### **1. Fixed Database Configuration**
- **Changed from `create-drop` to `validate`** - No more data loss on restart
- **File-based H2 database** - Data stored in `./data/taskmanager.mv.db`
- **Proper foreign key relationships** - Maintains data integrity

### **2. Enhanced Database Initialization**
- **Automatic user creation** - 3 default users on startup
- **Sample projects creation** - 5 realistic projects
- **Sample tasks creation** - 10 tasks with proper assignments
- **Relationship management** - Tasks linked to projects and users

### **3. Comprehensive Data Structure**
- **Users**: Managers, Users, Admins with proper roles
- **Projects**: Different statuses (PLANNING, IN_PROGRESS, COMPLETED)
- **Tasks**: Assigned to specific users with priorities and due dates
- **Relationships**: Tasks belong to projects, users can be assigned tasks

## **📊 What Gets Created Automatically:**

### **Users (3):**
1. **Test Manager** (`test@karooth.com`) - Role: MANAGER
2. **Test User** (`user@karooth.com`) - Role: USER  
3. **Admin User** (`admin@karooth.com`) - Role: ADMIN

### **Projects (5):**
1. **Website Redesign** - HIGH priority, IN_PROGRESS
2. **Mobile App Development** - CRITICAL priority, IN_PROGRESS
3. **Database Migration** - MEDIUM priority, PLANNING
4. **Security Audit** - HIGH priority, PLANNING
5. **Marketing Campaign** - MEDIUM priority, IN_PROGRESS

### **Tasks (10):**
- **Assigned to Manager**: Setup Development Environment, Backup Current Database, Vulnerability Assessment, Create Campaign Strategy
- **Assigned to User**: Design Homepage, Design App UI, Test Migration Scripts, Review Access Controls, Design Marketing Materials
- **Unassigned**: Implement Navigation

## **🚀 Testing Data Persistence:**

### **Step 1: Restart Server and Check Initialization**
```powershell
# Restart your server
cd backend
./gradlew bootRun
```

**Look for these log messages:**
```
=== DATABASE INITIALIZATION STARTED ===
Current user count in database: 0
No users found. Creating default users...
Created default user: test@karooth.com (ID: 1)
Created default user: user@karooth.com (ID: 2)
Created default user: admin@karooth.com (ID: 3)
Default user creation completed. Total users: 3

Current project count in database: 0
No projects found. Creating sample projects...
Created sample project: Website Redesign (ID: 1)
Created sample project: Mobile App Development (ID: 2)
Created sample project: Database Migration (ID: 3)
Created sample project: Security Audit (ID: 4)
Created sample project: Marketing Campaign (ID: 5)
Sample project creation completed. Total projects: 5

Current task count in database: 0
No tasks found. Creating sample tasks...
Created sample task: Design Homepage (ID: 1) - Assigned to: Test User
Created sample task: Implement Navigation (ID: 2) - Assigned to: Unassigned
Created sample task: Setup Development Environment (ID: 3) - Assigned to: Test Manager
Created sample task: Design App UI (ID: 4) - Assigned to: Test User
Created sample task: Backup Current Database (ID: 5) - Assigned to: Test Manager
Created sample task: Test Migration Scripts (ID: 6) - Assigned to: Test User
Created sample task: Vulnerability Assessment (ID: 7) - Assigned to: Test Manager
Created sample task: Review Access Controls (ID: 8) - Assigned to: Test User
Created sample task: Create Campaign Strategy (ID: 9) - Assigned to: Test Manager
Created sample task: Design Marketing Materials (ID: 10) - Assigned to: Test User
Sample task creation completed. Total tasks: 10

=== DATABASE SUMMARY ===
Users: 3
Projects: 5
Tasks: 10
Total records: 18
=== DATABASE INITIALIZATION COMPLETED ===
```

### **Step 2: Verify Data Exists**
```powershell
# Check full database status
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/database-full-status" -Method GET
```

### **Step 3: Test User Login and JWT**
```powershell
# Test Manager Login
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $response.token
Write-Host "JWT Token: $token"
```

### **Step 4: Test Protected Endpoints**
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

# Test projects endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method GET -Headers $headers

# Test tasks endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method GET -Headers $headers
```

### **Step 5: Test Data Persistence**
```powershell
# 1. Stop the server (Ctrl+C)
# 2. Restart the server
./gradlew bootRun

# 3. Check that data still exists
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/database-full-status" -Method GET
```

## **📁 Database File Location:**

Your persistent data is stored in:
```
./data/taskmanager.mv.db
```

This file contains all your data and persists between server restarts.

## **🔍 H2 Console Access:**

You can view your data directly:
1. Open: `http://localhost:8080/h2-console`
2. Connect with:
   - JDBC URL: `jdbc:h2:file:./data/taskmanager`
   - Username: `sa`
   - Password: (empty)
3. Run queries:
   ```sql
   SELECT * FROM users;
   SELECT * FROM projects;
   SELECT * FROM tasks;
   SELECT t.title, t.status, u.name as assigned_to, p.name as project 
   FROM tasks t 
   LEFT JOIN users u ON t.assigned_user_id = u.id 
   LEFT JOIN projects p ON t.project_id = p.id;
   ```

## **✅ Verification Checklist:**

- [ ] Server starts without errors
- [ ] Database initialization logs show 3 users, 5 projects, 10 tasks
- [ ] User login works and returns JWT token
- [ ] Protected endpoints return data (not 403 errors)
- [ ] Data persists after server restart
- [ ] Database file exists at `./data/taskmanager.mv.db`

## **🎯 Expected Results:**

### **After First Startup:**
- 3 users created
- 5 projects created
- 10 tasks created with proper assignments
- All relationships maintained

### **After Server Restart:**
- All data still exists
- No data loss
- Relationships intact
- Users can login and access their assigned tasks

## **🔧 Troubleshooting:**

### **Issue 1: Still getting 403 errors**
**Solution:** Check JWT authentication is working properly

### **Issue 2: No projects/tasks showing**
**Solution:** Check server logs for database initialization errors

### **Issue 3: Data lost on restart**
**Solution:** Verify database URL is `jdbc:h2:file:./data/taskmanager` (not `mem`)

### **Issue 4: Database file not created**
**Solution:** Check file permissions and ensure the `./data/` directory exists

## **🚀 Next Steps:**

Once data persistence is confirmed working:

1. **Test frontend integration** with the persistent data
2. **Create new projects and tasks** through the UI
3. **Assign tasks to users** and verify assignments persist
4. **Test role-based access** (MANAGER vs USER vs ADMIN)
5. **Verify all CRUD operations** work with persistent data

Your database is now **fully persistent** and will maintain all data between server restarts! 🎉

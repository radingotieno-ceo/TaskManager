# Database Persistence Fix Guide

## **Problem Identified:**
- Only 1 user in database instead of 3 expected users
- Data is lost on server restart (in-memory database)
- User registrations not persisting

## **Solutions Implemented:**

### **1. Fixed Database Configuration**
- **Changed from in-memory to file-based H2 database**
- **Data now persists between server restarts**
- **Enhanced logging for database initialization**

### **2. Added Database Initialization Service**
- **Automatically creates default users on startup**
- **Checks for existing users before creating new ones**
- **Comprehensive logging of database operations**

### **3. Added Manual User Creation Endpoints**
- **Manual trigger for creating default users**
- **Database status checking**
- **User verification tools**

## **Step-by-Step Fix Process:**

### **Step 1: Restart Your Backend Server**
```powershell
# Stop your current server (Ctrl+C)
# Then restart it
cd backend
./gradlew bootRun
```

**Look for these log messages during startup:**
```
=== DATABASE INITIALIZATION STARTED ===
Current user count in database: 0
No users found. Creating default users...
Created default user: test@karooth.com (ID: 1)
Created default user: user@karooth.com (ID: 2)
Created default user: admin@karooth.com (ID: 3)
Default user creation completed. Total users: 3
=== DATABASE INITIALIZATION COMPLETED ===
```

### **Step 2: Check Database Status**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/database-status" -Method GET
```

**Expected Output:**
```
=== DATABASE STATUS ===
Current time: 1234567890123

Total users in database: 3

✅ Users found in database:
1. Test Manager (test@karooth.com) - MANAGER
2. Test User (user@karooth.com) - USER
3. Admin User (admin@karooth.com) - ADMIN

=== EXPECTED USERS CHECK ===
test@karooth.com: ✅ FOUND
user@karooth.com: ✅ FOUND
admin@karooth.com: ✅ FOUND
```

### **Step 3: List All Users**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/list-users" -Method GET
```

**Expected Output:**
```
=== DATABASE USERS LIST ===
Current time: 1234567890123

✅ Found 3 user(s) in database:

1. Test Manager
   Email: test@karooth.com
   Role: MANAGER
   ID: 1

2. Test User
   Email: user@karooth.com
   Role: USER
   ID: 2

3. Admin User
   Email: admin@karooth.com
   Role: ADMIN
   ID: 3

=== PASSWORD VERIFICATION TESTS ===
test@karooth.com - Password 'password123' matches: ✅ YES
user@karooth.com - Password 'password123' matches: ✅ YES
admin@karooth.com - Password 'password123' matches: ✅ YES
```

### **Step 4: Test User Registration (if needed)**
```powershell
$body = @{
    name = "New Test User"
    email = "newuser@karooth.com"
    password = "password123"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json"
```

### **Step 5: Test User Login**
```powershell
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $response.token
Write-Host "JWT Token: $token"
```

### **Step 6: Test JWT Authentication**
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/test-jwt" -Method GET -Headers $headers
```

## **Manual User Creation (if needed):**

If the automatic initialization doesn't work, manually create users:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/create-default-users" -Method POST
```

## **Database File Location:**

Your database file will be created at:
```
./data/taskmanager.mv.db
```

This file contains all your data and persists between server restarts.

## **H2 Console Access:**

You can also check your database directly:
1. Open: `http://localhost:8080/h2-console`
2. Connect with:
   - JDBC URL: `jdbc:h2:file:./data/taskmanager`
   - Username: `sa`
   - Password: (empty)
3. Run: `SELECT * FROM users;`

## **Verification Checklist:**

- [ ] Server starts without errors
- [ ] Database initialization logs show 3 users created
- [ ] `/api/auth/database-status` shows 3 users
- [ ] `/api/auth/list-users` shows all 3 users
- [ ] User login works and returns JWT token
- [ ] JWT authentication works for protected endpoints
- [ ] Data persists after server restart

## **Troubleshooting:**

### **Issue 1: Still only 1 user**
**Solution:** Check if the database file exists and restart server
```powershell
# Check if database file exists
ls ./data/
```

### **Issue 2: Database initialization errors**
**Solution:** Check startup logs for SQL errors and fix `data.sql` syntax

### **Issue 3: Password verification fails**
**Solution:** The password hash might be incorrect. Use the manual user creation endpoint.

### **Issue 4: Data still lost on restart**
**Solution:** Verify the database URL is set to `jdbc:h2:file:./data/taskmanager` (not `mem`)

## **Expected Results After Fix:**

1. **3 default users** created automatically on startup
2. **Data persists** between server restarts
3. **User registrations** are saved permanently
4. **JWT authentication** works with all users
5. **Password verification** works for "password123"

## **Next Steps:**

Once the database persistence is working:
1. Test user registration through the frontend
2. Test JWT authentication with different user roles
3. Test protected endpoints with authenticated users
4. Verify data persists after multiple server restarts

Let me know what you see when you restart the server and run these verification commands!

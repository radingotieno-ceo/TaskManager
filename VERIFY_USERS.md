# Database Users Verification Guide

## **Quick Commands to Check Your Database Users**

### **For Windows PowerShell Users:**

#### **1. List All Users in Database**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/list-users" -Method GET
```

#### **2. Check Specific User**
```powershell
# Check the test manager
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/check-user/test@karooth.com" -Method GET

# Check the test user
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/check-user/user@karooth.com" -Method GET

# Check the admin user
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/check-user/admin@karooth.com" -Method GET
```

#### **3. Test User Login**
```powershell
# Test Manager Login
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"

# Test User Login
$body = @{
    email = "user@karooth.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"

# Admin Login
$body = @{
    email = "admin@karooth.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
```

### **For Unix/Linux/Mac Users (or Git Bash on Windows):**

#### **1. List All Users in Database**
```bash
curl -X GET http://localhost:8080/api/auth/list-users
```

#### **2. Check Specific User**
```bash
# Check the test manager
curl -X GET http://localhost:8080/api/auth/check-user/test@karooth.com

# Check the test user
curl -X GET http://localhost:8080/api/auth/check-user/user@karooth.com

# Check the admin user
curl -X GET http://localhost:8080/api/auth/check-user/admin@karooth.com
```

#### **3. Test User Login**
```bash
# Test Manager Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@karooth.com",
    "password": "password123"
  }'

# Test User Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@karooth.com",
    "password": "password123"
  }'

# Admin Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@karooth.com",
    "password": "password123"
  }'
```

## **Expected Users in Your Database**

Based on your `data.sql` file, you should have these users:

### **1. Test Manager**
- **Email:** `test@karooth.com`
- **Password:** `password123`
- **Role:** `MANAGER`
- **Name:** `Test Manager`

### **2. Test User**
- **Email:** `user@karooth.com`
- **Password:** `password123`
- **Role:** `USER`
- **Name:** `Test User`

### **3. Admin User**
- **Email:** `admin@karooth.com`
- **Password:** `password123`
- **Role:** `ADMIN`
- **Name:** `Admin User`

## **What to Look For**

### **✅ Successful Database Check:**
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

### **❌ Common Issues:**

#### **Issue 1: No Users Found**
```
❌ No users found in database
This might indicate:
- Database is empty
- Data initialization failed
- Wrong database connection
```

**Solution:** Check if your `data.sql` is being executed properly.

#### **Issue 2: Password Verification Fails**
```
test@karooth.com - Password 'password123' matches: ❌ NO
```

**Solution:** The password hash in `data.sql` might not match the current password encoder.

#### **Issue 3: User Not Found**
```
User exists: ❌ NO
```

**Solution:** The user might not have been created or the email is incorrect.

## **Troubleshooting Steps**

### **Step 1: Check Database Initialization**
1. Make sure your backend server is running
2. Check the startup logs for any database errors
3. Verify that `data.sql` is being executed

### **Step 2: Verify Data.sql Execution**
Look for these log messages during startup:
```
Executing SQL script from class path resource [data.sql]
```

### **Step 3: Check H2 Console (if using H2)**
1. Open `http://localhost:8080/h2-console`
2. Connect with:
   - JDBC URL: `jdbc:h2:mem:taskmanager`
   - Username: `sa`
   - Password: (empty)
3. Run: `SELECT * FROM users;`

### **Step 4: Create Test User if Needed**
If no users exist, create one using PowerShell:
```powershell
$body = @{
    name = "Test User"
    email = "test@karooth.com"
    password = "password123"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json"
```

## **Testing Authentication Flow**

### **1. Test Login (PowerShell)**
```powershell
$body = @{
    email = "test@karooth.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = $response.token
Write-Host "JWT Token: $token"
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Test Manager",
    "email": "test@karooth.com",
    "role": "MANAGER"
  }
}
```

### **2. Test JWT Authentication (PowerShell)**
```powershell
# Replace YOUR_JWT_TOKEN with the actual token from login
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/test-jwt" -Method GET -Headers $headers
```

**Expected Response:**
```
JWT authentication is working! Current time: 1234567890123
```

## **Next Steps**

Once you've verified your users exist and can authenticate:

1. **Test JWT Authentication** using the test script
2. **Test Protected Endpoints** with different user roles
3. **Test Frontend Integration** with the verified users
4. **Debug Any Remaining Issues** using the enhanced logging

Let me know what you find when you run these verification commands!

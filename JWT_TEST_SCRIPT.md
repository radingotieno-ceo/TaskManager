# JWT Authentication Test Script

## **Step 1: Start the Backend Server**
```bash
cd backend
./gradlew bootRun
```

## **Step 2: Test Backend Health**
```bash
curl -X GET http://localhost:8080/health
```

## **Step 3: Create a Test User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@karooth.com",
    "password": "password123",
    "role": "USER"
  }'
```

## **Step 4: Login and Get JWT Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@karooth.com",
    "password": "password123"
  }'
```

**Save the token from the response for the next steps.**

## **Step 5: Test JWT Authentication**
Replace `YOUR_JWT_TOKEN` with the actual token from step 4:

```bash
# Test basic JWT authentication
curl -X GET http://localhost:8080/api/auth/test-jwt \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test JWT authentication with details
curl -X GET http://localhost:8080/api/auth/test-jwt-details \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Debug JWT token
curl -X POST http://localhost:8080/api/auth/debug-jwt \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## **Step 6: Test Protected Endpoints**
```bash
# Test projects endpoint
curl -X GET http://localhost:8080/api/projects \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test tasks endpoint
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## **Step 7: Test Frontend Integration**

1. Start the frontend:
```bash
cd frontend
ng serve
```

2. Open browser to `http://localhost:4200`

3. Login with the test user credentials:
   - Email: `test@karooth.com`
   - Password: `password123`

4. Check browser console for any errors

5. Try accessing protected pages like Projects or Tasks

## **Expected Results**

### **Successful Authentication:**
- Login returns a JWT token
- `/api/auth/test-jwt` returns "JWT authentication is working!"
- `/api/auth/test-jwt-details` shows authentication details
- Protected endpoints return data instead of 403 errors

### **Common Issues and Solutions:**

#### **Issue 1: 403 Forbidden on all authenticated endpoints**
**Cause:** JWT token not being properly validated
**Solution:** Check the debug logs in the backend console

#### **Issue 2: CORS errors in browser**
**Cause:** CORS configuration issues
**Solution:** Verify the CORS configuration allows `http://localhost:4200`

#### **Issue 3: "No valid Authorization header found"**
**Cause:** Frontend not sending the Authorization header correctly
**Solution:** Check the AuthInterceptor in the frontend

#### **Issue 4: "JWT token validation failed"**
**Cause:** Token generation/validation mismatch
**Solution:** Check the JWT secret and algorithm consistency

## **Debugging Commands**

### **Check Backend Logs:**
Look for these log messages:
- `JWT Filter processing: GET /api/projects`
- `JWT token validation successful for user: test@karooth.com`
- `Authentication set in SecurityContext for user: test@karooth.com`

### **Check Frontend Network Tab:**
- Verify Authorization header is being sent
- Check for CORS preflight requests
- Look for 403 responses

### **Test Token Manually:**
```bash
# Decode JWT token (replace YOUR_JWT_TOKEN)
echo "YOUR_JWT_TOKEN" | cut -d. -f2 | base64 -d | jq .
```

## **Troubleshooting Checklist**

- [ ] Backend server is running on port 8080
- [ ] Frontend is running on port 4200
- [ ] JWT secret is properly configured
- [ ] CORS is configured correctly
- [ ] User exists in database
- [ ] JWT token is being sent in Authorization header
- [ ] Token is not expired
- [ ] User authorities are properly set

## **Next Steps**

If the authentication is working:
1. Test role-based access (ADMIN, MANAGER, USER)
2. Test different endpoints with different roles
3. Test token expiration
4. Test logout functionality

If authentication is still failing:
1. Check the detailed logs in the backend console
2. Use the debug endpoints to identify the specific issue
3. Verify the JWT token format and content
4. Check if the user authorities are properly mapped

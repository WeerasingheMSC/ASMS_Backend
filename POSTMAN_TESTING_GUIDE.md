# 🧪 ASMS Backend - Postman Testing Guide

## Prerequisites
1. **Postman installed** (Download from https://www.postman.com/downloads/)
2. **PostgreSQL running** with database `demo` created
3. **Application running** on http://localhost:8080

## 🚀 Starting the Application

```bash
cd /Users/nimeshmadhusanka/Desktop/ASMS_Backend
./mvnw spring-boot:run
```

Wait for the message: `Started AsmsBackendApplication in X seconds`

---

## 📝 Test Sequence

### Test 1: Public Endpoint (No Authentication Required)

**Purpose:** Verify the server is running and public endpoints are accessible

**Request:**
- **Method:** GET
- **URL:** `http://localhost:8080/api/public`
- **Headers:** None required

**Expected Response (200 OK):**
```json
{
  "message": "This is a public endpoint - no authentication required"
}
```

---

### Test 2: User Signup

**Purpose:** Register a new user

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/signup`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "name": "Admin User",
  "email": "admin@asms.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Admin User",
  "email": "admin@asms.com",
  "role": "ROLE_ADMIN",
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInN1YiI6ImFkbWluQGFzbXMuY29tIiwiaWF0IjoxNzMwMjU5NjAwLCJleHAiOjE3MzAzNDYwMDB9.xxxxx"
}
```

**💡 Important:** Copy the `token` value - you'll need it for protected endpoints!

---

### Test 3: Create More Users (Different Roles)

**Customer User:**
```json
{
  "name": "John Doe",
  "email": "john@customer.com",
  "password": "customer123",
  "role": "ROLE_CUSTOMER"
}
```

**Mechanic User:**
```json
{
  "name": "Mike Mechanic",
  "email": "mike@mechanic.com",
  "password": "mechanic123",
  "role": "ROLE_MECHANIC"
}
```

---

### Test 4: User Login

**Purpose:** Login with existing credentials

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "email": "admin@asms.com",
  "password": "admin123"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Admin User",
  "email": "admin@asms.com",
  "role": "ROLE_ADMIN",
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInN1YiI6ImFkbWluQGFzbXMuY29tIiwiaWF0IjoxNzMwMjU5NjAwLCJleHAiOjE3MzAzNDYwMDB9.xxxxx"
}
```

---

### Test 5: Access Protected Endpoint (With JWT Token)

**Purpose:** Verify JWT authentication is working

**Request:**
- **Method:** GET
- **URL:** `http://localhost:8080/api/test`
- **Headers:**
  - `Authorization: Bearer YOUR_TOKEN_HERE`
  
**⚠️ Replace `YOUR_TOKEN_HERE` with the token from signup/login response**

**Expected Response (200 OK):**
```json
{
  "message": "Protected endpoint accessed successfully!",
  "user": "admin@asms.com",
  "authorities": "[ROLE_ADMIN]"
}
```

---

### Test 6: Access Protected Endpoint Without Token

**Purpose:** Verify endpoints are actually protected

**Request:**
- **Method:** GET
- **URL:** `http://localhost:8080/api/test`
- **Headers:** None (no Authorization header)

**Expected Response (403 Forbidden):**
```json
{
  "timestamp": "2025-10-30T...",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/test"
}
```

---

## 🎯 Step-by-Step Postman Setup

### Method 1: Manual Setup

1. **Open Postman**

2. **Create a New Collection:**
   - Click "New" → "Collection"
   - Name: "ASMS Backend API"

3. **Add Test 1 - Public Endpoint:**
   - Click "Add Request" in your collection
   - Name: "Public Test"
   - Method: GET
   - URL: `http://localhost:8080/api/public`
   - Click "Send"

4. **Add Test 2 - Signup:**
   - Add new request: "User Signup"
   - Method: POST
   - URL: `http://localhost:8080/api/auth/signup`
   - Go to "Headers" tab:
     - Key: `Content-Type`, Value: `application/json`
   - Go to "Body" tab:
     - Select "raw" and "JSON"
     - Paste the signup JSON
   - Click "Send"
   - **Copy the token from response**

5. **Add Test 3 - Login:**
   - Add new request: "User Login"
   - Method: POST
   - URL: `http://localhost:8080/api/auth/login`
   - Headers: `Content-Type: application/json`
   - Body: Login JSON
   - Click "Send"

6. **Add Test 4 - Protected Endpoint:**
   - Add new request: "Test Protected Endpoint"
   - Method: GET
   - URL: `http://localhost:8080/api/test`
   - Go to "Headers" tab:
     - Key: `Authorization`
     - Value: `Bearer eyJhbGci...` (paste your token after "Bearer ")
   - Click "Send"

### Method 2: Using Environment Variables (Recommended)

1. **Create Environment:**
   - Click the eye icon (top right) → "Add"
   - Environment name: "ASMS Local"
   - Add variables:
     - `base_url`: `http://localhost:8080`
     - `token`: (leave empty initially)
   - Save

2. **Use Variables in Requests:**
   - URL: `{{base_url}}/api/auth/signup`
   - Authorization: `Bearer {{token}}`

3. **Auto-Save Token (Advanced):**
   - In signup/login request, go to "Tests" tab
   - Add script:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("token", jsonData.token);
   ```
   - Now token auto-saves after login!

---

## ❌ Common Errors & Solutions

### Error 1: Connection Refused
```
Error: connect ECONNREFUSED 127.0.0.1:8080
```
**Solution:** Application not running. Start with `./mvnw spring-boot:run`

### Error 2: 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized"
}
```
**Solution:** 
- Check if token is included in Authorization header
- Format: `Bearer YOUR_TOKEN_HERE` (note the space after Bearer)
- Token might be expired (24 hours), login again

### Error 3: 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden"
}
```
**Solution:** 
- CSRF token issue OR missing/invalid JWT token
- Make sure Authorization header format is correct

### Error 4: Email Already Registered
```json
{
  "message": "Email already registered"
}
```
**Solution:** Use a different email or login with existing credentials

### Error 5: 500 Internal Server Error - Database Connection
```json
{
  "status": 500,
  "error": "Internal Server Error"
}
```
**Solution:** 
- Check PostgreSQL is running: `psql -U postgres`
- Verify database `demo` exists
- Check credentials in `application.properties`

---

## 🔍 Verify Database

After signup, check if user is saved:

```bash
# Connect to PostgreSQL
psql -U postgres -d demo

# Query users table
SELECT * FROM users;

# Expected output:
 id |    name     |      email      |                          password                           |    role     
----+-------------+-----------------+-------------------------------------------------------------+-------------
  1 | Admin User  | admin@asms.com  | $2a$10$xxx...                                               | ROLE_ADMIN
```

---

## 📊 Quick Test Checklist

Run these tests in order:

- [ ] ✅ Public endpoint works (no auth)
- [ ] ✅ Signup creates user and returns token
- [ ] ✅ Same email signup fails with error
- [ ] ✅ Login with correct credentials returns token
- [ ] ✅ Login with wrong credentials fails
- [ ] ✅ Protected endpoint works with valid token
- [ ] ✅ Protected endpoint fails without token
- [ ] ✅ User data is saved in database

---

## 🎓 Next Steps

Once all tests pass:

1. ✅ Authentication system is working correctly
2. Create Vehicle entity and CRUD endpoints
3. Create Appointment entity and management
4. Add role-based authorization (@PreAuthorize)
5. Integrate with your React frontend

---

## 📞 Debugging Tips

**View application logs:**
```bash
# The terminal where you ran ./mvnw spring-boot:run
# Look for errors, SQL queries, etc.
```

**Test token validity:**
- Go to https://jwt.io/
- Paste your token
- Verify payload contains email and role

**Check application is running:**
```bash
curl http://localhost:8080/api/public
```

---

## 🔐 Security Notes

- JWT tokens expire after 24 hours (configurable in application.properties)
- Passwords are encrypted with BCrypt
- CSRF is disabled for API (JWT-based auth)
- CORS is enabled for frontend integration
- All endpoints except /api/auth/** and /api/public require authentication

---

**Your authentication system is now fully functional! 🎉**


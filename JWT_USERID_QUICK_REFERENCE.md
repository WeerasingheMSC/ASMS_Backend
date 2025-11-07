# Quick Reference: JWT with User ID

## 🎯 What Changed

### Backend Changes ✅
1. **JwtTokenProvider.java** - Now includes userId in JWT token
2. **LoginResponse.java** - Returns userId in login response
3. **AuthService.java** - Passes userId to token generation

### Login Response (NEW Format)
```json
{
  "token": "eyJhbGci...",
  "userId": 123,           ⭐ NEW - User ID
  "username": "john@example.com",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "profileImage": null,
  "message": "Login successful"
}
```

### JWT Token Payload (NEW)
```json
{
  "sub": "john@example.com",
  "userId": 123,           ⭐ NEW - User ID claim
  "iat": 1699270000,
  "exp": 1699356400
}
```

---

## 📱 Frontend Usage

### Store After Login
```javascript
const data = await loginAPI(credentials);

localStorage.setItem('token', data.token);
localStorage.setItem('userId', data.userId);      // ⭐ Store userId
localStorage.setItem('username', data.username);
localStorage.setItem('role', data.role);
```

### Use in API Calls
```javascript
const userId = localStorage.getItem('userId');    // ⭐ Get userId
const token = localStorage.getItem('token');

fetch(`http://localhost:8080/api/users/${userId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### Use in WebSocket
```javascript
const userId = localStorage.getItem('userId');
stompClient.subscribe(`/topic/notifications/user.${userId}`, callback);
```

---

## 🧪 Test It

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer@test.com", "password": "password123"}'
```

### 2. Expected Response
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,                          ⭐ Look for this
  "username": "customer@test.com",
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "profileImage": null,
  "message": "Login successful"
}
```

### 3. Decode Token at jwt.io
Paste your token at https://jwt.io and verify you see:
```json
{
  "sub": "customer@test.com",
  "userId": 1,                         ⭐ Verify this is present
  "iat": 1699270000,
  "exp": 1699356400
}
```

---

## ✅ Build Status
- **Compilation**: ✅ SUCCESS
- **No Errors**: ✅ All files compile correctly
- **Ready to Run**: ✅ Yes

## 🚀 Start Application
```bash
mvnw.cmd spring-boot:run
```

---

## 📚 Full Documentation
See `JWT_USERID_IMPLEMENTATION_GUIDE.md` for complete details and examples.


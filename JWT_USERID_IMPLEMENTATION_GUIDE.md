# JWT Token with User ID - Implementation Guide

## ✅ Changes Implemented

I've successfully added the User ID to the JWT token and login response. Here's what was changed:

### 1. Updated `JwtTokenProvider.java` ✅

**Location**: `src/main/java/com/example/demo/security/JwtTokenProvider.java`

**Changes**:
- Modified `generateToken()` method to accept `userId` parameter
- Added `userId` as a claim in the JWT token
- Added new `getUserIdFromToken()` method to extract userId from token

**Before**:
```java
public String generateToken(Authentication authentication) {
    return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
}
```

**After**:
```java
public String generateToken(Authentication authentication, Long userId) {
    return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", userId)  // ⭐ Added userId claim
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
}

// ⭐ New method to extract userId from token
public Long getUserIdFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

    return claims.get("userId", Long.class);
}
```

### 2. Updated `LoginResponse.java` ✅

**Location**: `src/main/java/com/example/demo/dto/LoginResponse.java`

**Changes**:
- Added `userId` field to the response

**Before**:
```java
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String role;
    private String profileImage;
    private String message;
}
```

**After**:
```java
public class LoginResponse {
    private String token;
    private Long userId;        // ⭐ Added userId field
    private String username;
    private String email;
    private String role;
    private String profileImage;
    private String message;
}
```

### 3. Updated `AuthService.java` ✅

**Location**: `src/main/java/com/example/demo/service/AuthService.java`

**Changes**:
- Pass `user.getId()` to `generateToken()` method
- Include `userId` in the `LoginResponse`

**Before**:
```java
String token = jwtTokenProvider.generateToken(authentication);

return LoginResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole().name())
        .profileImage(user.getProfileImage())
        .message("Login successful")
        .build();
```

**After**:
```java
String token = jwtTokenProvider.generateToken(authentication, user.getId()); // ⭐ Pass userId

return LoginResponse.builder()
        .token(token)
        .userId(user.getId())   // ⭐ Include userId in response
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole().name())
        .profileImage(user.getProfileImage())
        .message("Login successful")
        .build();
```

---

## 📋 How to Use in Frontend

### 1. Login Response Structure

When a user logs in, the response will now include the `userId`:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lckB0ZXN0LmNvbSIsInVzZXJJZCI6MTIzLCJpYXQiOjE2OTkyNzAwMDAsImV4cCI6MTY5OTM1NjQwMH0.signature",
  "userId": 123,
  "username": "customer@test.com",
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "profileImage": "https://example.com/profile.jpg",
  "message": "Login successful"
}
```

### 2. Store User Data in Frontend

#### Using LocalStorage (React/JavaScript)
```javascript
// After successful login
const loginUser = async (credentials) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    
    const data = await response.json();
    
    // Store token
    localStorage.setItem('token', data.token);
    
    // Store userId
    localStorage.setItem('userId', data.userId);
    
    // Store other user info
    localStorage.setItem('username', data.username);
    localStorage.setItem('email', data.email);
    localStorage.setItem('role', data.role);
    localStorage.setItem('profileImage', data.profileImage);
    
    console.log('User ID:', data.userId);  // ⭐ Use userId in your app
    
  } catch (error) {
    console.error('Login failed:', error);
  }
};
```

#### Using React Context/State
```javascript
// UserContext.js
import { createContext, useState, useContext } from 'react';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState({
    token: null,
    userId: null,
    username: null,
    email: null,
    role: null,
    profileImage: null
  });

  const login = async (credentials) => {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    
    const data = await response.json();
    
    setUser({
      token: data.token,
      userId: data.userId,      // ⭐ Store userId
      username: data.username,
      email: data.email,
      role: data.role,
      profileImage: data.profileImage
    });
    
    // Also persist to localStorage
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
  };

  const logout = () => {
    setUser({
      token: null,
      userId: null,
      username: null,
      email: null,
      role: null,
      profileImage: null
    });
    localStorage.clear();
  };

  return (
    <UserContext.Provider value={{ user, login, logout }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => useContext(UserContext);
```

#### Using the User Context
```javascript
// In your components
import { useUser } from './UserContext';

function MyComponent() {
  const { user } = useUser();
  
  return (
    <div>
      <h1>Welcome, {user.username}</h1>
      <p>Your User ID: {user.userId}</p>  {/* ⭐ Access userId */}
      <p>Email: {user.email}</p>
      <p>Role: {user.role}</p>
    </div>
  );
}
```

### 3. Use UserId in API Calls

#### Example: Fetch User-Specific Data
```javascript
// Get notifications for logged-in user
const fetchNotifications = async () => {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');  // ⭐ Get userId
  
  const response = await fetch('http://localhost:8080/api/notifications', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const notifications = await response.json();
  console.log(`Notifications for user ${userId}:`, notifications);
};
```

#### Example: WebSocket Connection with UserId
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const connectWebSocket = () => {
  const userId = localStorage.getItem('userId');  // ⭐ Get userId
  
  const socket = new SockJS('http://localhost:8080/ws');
  const stompClient = Stomp.over(socket);
  
  stompClient.connect({}, (frame) => {
    console.log('Connected:', frame);
    
    // Subscribe to user-specific notifications
    stompClient.subscribe(`/topic/notifications/user.${userId}`, (message) => {
      const notification = JSON.parse(message.body);
      console.log('New notification:', notification);
      
      // Update UI with notification
      showNotification(notification);
    });
  });
};
```

### 4. Extract UserId from JWT Token (Optional)

If you need to extract userId from the token itself (for validation or debugging):

```javascript
// Decode JWT token (client-side)
function decodeToken(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Failed to decode token:', error);
    return null;
  }
}

// Usage
const token = localStorage.getItem('token');
const payload = decodeToken(token);

if (payload) {
  console.log('Username:', payload.sub);      // Username from token
  console.log('User ID:', payload.userId);    // ⭐ userId from token
  console.log('Issued at:', new Date(payload.iat * 1000));
  console.log('Expires at:', new Date(payload.exp * 1000));
}
```

---

## 🧪 Testing

### Test Login Endpoint

```bash
# Login as customer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer@test.com",
    "password": "password123"
  }'
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,                          ⭐ User ID included
  "username": "customer@test.com",
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "profileImage": null,
  "message": "Login successful"
}
```

### Verify Token Contains UserId

You can decode the JWT token at [jwt.io](https://jwt.io) to verify the payload:

```json
{
  "sub": "customer@test.com",
  "userId": 1,                         ⭐ userId claim in token
  "iat": 1699270000,
  "exp": 1699356400
}
```

---

## 🎯 Use Cases

### 1. User Profile Page
```javascript
const ProfilePage = () => {
  const userId = localStorage.getItem('userId');
  
  useEffect(() => {
    // Fetch user details
    fetch(`http://localhost:8080/api/users/${userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
  }, [userId]);
};
```

### 2. Create Appointment with UserId
```javascript
const createAppointment = async (appointmentData) => {
  const userId = localStorage.getItem('userId');
  
  const response = await fetch('http://localhost:8080/api/customer/appointments', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      ...appointmentData,
      customerId: userId  // ⭐ Use userId
    })
  });
};
```

### 3. Filter Notifications
```javascript
const MyNotifications = () => {
  const [notifications, setNotifications] = useState([]);
  const userId = localStorage.getItem('userId');
  
  useEffect(() => {
    // Notifications are already filtered by userId on backend
    fetchNotifications();
  }, [userId]);
  
  const fetchNotifications = async () => {
    const response = await fetch('http://localhost:8080/api/notifications', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const data = await response.json();
    setNotifications(data.data);
  };
};
```

---

## 🔒 Backend Usage (Optional)

If you need to extract userId from token in backend controllers:

```java
// In any controller
@GetMapping("/profile")
public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7); // Remove "Bearer " prefix
    Long userId = jwtTokenProvider.getUserIdFromToken(token);
    
    User user = userService.getUserById(userId);
    return ResponseEntity.ok(user);
}
```

---

## ✅ Summary

**What's Included in JWT Token:**
- ✅ Username (subject)
- ✅ **User ID (custom claim)** ⭐ NEW
- ✅ Issued At timestamp
- ✅ Expiration timestamp

**What's Included in Login Response:**
- ✅ JWT Token (with userId inside)
- ✅ **User ID** ⭐ NEW
- ✅ Username
- ✅ Email
- ✅ Role
- ✅ Profile Image
- ✅ Success Message

**Benefits:**
- ✅ Frontend can easily access userId without decoding token
- ✅ UserId is also embedded in the token for validation
- ✅ Can be used for user-specific operations
- ✅ Supports WebSocket subscriptions with userId
- ✅ No breaking changes to existing code

---

## 🚀 Ready to Use!

Your application now includes the User ID in:
1. ✅ JWT token payload (as a claim)
2. ✅ Login response (as a field)

Start your application and test the login endpoint to see the userId in action!

```bash
mvnw.cmd spring-boot:run
```


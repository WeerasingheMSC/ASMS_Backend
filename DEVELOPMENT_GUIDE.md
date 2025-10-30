# 🚗 Automobile System Management System (ASMS) - Backend Development Guide

## ✅ Current Implementation Status

### Completed Features:
1. **JWT Authentication System** ✅
   - JWT token generation and validation
   - Token-based authentication filter
   - Secure password encryption with BCrypt
   
2. **User Management** ✅
   - User registration (signup)
   - User login with JWT token
   - Role-based access control (CUSTOMER, ADMIN, MECHANIC)
   
3. **Security Configuration** ✅
   - CORS enabled for frontend integration
   - Stateless JWT authentication
   - Protected and public endpoints
   
4. **Database Setup** ✅
   - PostgreSQL integration
   - JPA/Hibernate configuration
   - User entity with roles

---

## 📋 API Endpoints

### Authentication Endpoints (Public)

#### 1. **Signup**
```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ROLE_CUSTOMER"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "ROLE_CUSTOMER",
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 2. **Login**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "ROLE_CUSTOMER",
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Protected Endpoints (Requires JWT Token)

#### 3. **Test Protected Endpoint**
```http
GET http://localhost:8080/api/test
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
  "message": "Protected endpoint accessed successfully!",
  "user": "john@example.com",
  "authorities": "[ROLE_CUSTOMER]"
}
```

### Public Endpoints

#### 4. **Public Test Endpoint**
```http
GET http://localhost:8080/api/public
```

---

## 🚀 Next Steps for Development

### Phase 1: Core Automobile Management Features

#### 1. **Vehicle Management**
Create entities and endpoints for:
- Vehicle model (id, make, model, year, VIN, licensePlate, ownerId)
- Add vehicle
- Update vehicle
- Delete vehicle
- List user's vehicles

**Create:**
```java
// src/main/java/com/example/demo/model/Vehicle.java
@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private String vin;
    private String licensePlate;
    
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
```

#### 2. **Service/Appointment Management**
Create entities for:
- Service appointments
- Service types (oil change, tire rotation, inspection, etc.)
- Appointment status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)

#### 3. **Mechanic Management**
- Assign mechanics to services
- Mechanic availability
- Mechanic specializations

### Phase 2: Business Logic

#### 4. **Service Records**
- Track service history for each vehicle
- Generate service reports
- Maintenance reminders

#### 5. **Billing/Invoicing**
- Generate invoices
- Payment tracking
- Service pricing

### Phase 3: Advanced Features

#### 6. **Notifications**
- Email notifications for appointments
- SMS reminders
- Status updates

#### 7. **Admin Dashboard**
- User management
- Analytics and reports
- System configuration

---

## 🛠️ How to Test the Current System

### 1. **Start the Application**
```bash
cd /Users/nimeshmadhusanka/Desktop/ASMS_Backend
./mvnw spring-boot:run
```

### 2. **Test with cURL**

**Signup:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@asms.com",
    "password": "admin123",
    "role": "ROLE_ADMIN"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@asms.com",
    "password": "admin123"
  }'
```

**Test Protected Endpoint (replace TOKEN with your JWT):**
```bash
curl -X GET http://localhost:8080/api/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 3. **Test with Postman**
1. Import requests to Postman
2. Create a collection for ASMS API
3. Test each endpoint
4. Use Environment variables for token management

---

## 📁 Project Structure Explained

```
src/main/java/com/example/demo/
├── config/           # Security and application configurations
│   ├── CorsConfig.java          # CORS settings for frontend
│   └── SecurityConfig.java      # JWT security configuration
├── controller/       # REST API endpoints
│   ├── AuthController.java      # Login/Signup endpoints
│   └── TestController.java      # Test endpoints
├── dto/             # Data Transfer Objects
│   ├── AuthResponse.java        # Response for auth operations
│   ├── LoginRequest.java        # Login request body
│   └── SignupRequest.java       # Signup request body
├── filter/          # Request filters
│   └── JwtAuthenticationFilter.java  # JWT token validation
├── model/           # JPA Entities
│   └── User.java                # User entity
├── repository/      # Database repositories
│   └── UserRepository.java      # User data access
├── service/         # Business logic
│   ├── AuthService.java         # Authentication logic
│   └── CustomUserDetailsService.java  # User details for Spring Security
└── util/            # Utility classes
    └── JwtUtil.java             # JWT token operations
```

---

## 🔐 Frontend Integration Guide

### Store JWT Token (React Example)

```javascript
// After login/signup
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const data = await response.json();
// Store token in localStorage or state management
localStorage.setItem('token', data.token);
localStorage.setItem('user', JSON.stringify(data));
```

### Make Authenticated Requests

```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:8080/api/test', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### Logout

```javascript
localStorage.removeItem('token');
localStorage.removeItem('user');
// Redirect to login page
```

---

## 🎯 User Roles in ASMS

1. **ROLE_CUSTOMER**
   - Register/manage their vehicles
   - Book service appointments
   - View service history
   - Make payments

2. **ROLE_MECHANIC**
   - View assigned appointments
   - Update service status
   - Add service notes
   - Mark services complete

3. **ROLE_ADMIN**
   - Manage all users
   - Manage mechanics
   - View all appointments
   - Generate reports
   - System configuration

---

## 📦 Database Schema (Current)

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

---

## 🐛 Troubleshooting

### Issue: "Cannot connect to PostgreSQL"
**Solution:** Ensure PostgreSQL is running and credentials in `application.properties` are correct

### Issue: "Token expired"
**Solution:** Login again to get a new token. Default expiry is 24 hours.

### Issue: "401 Unauthorized"
**Solution:** Check if Bearer token is included in Authorization header

### Issue: "CORS error from frontend"
**Solution:** Add your frontend URL to CORS configuration in `CorsConfig.java`

---

## 📚 Additional Resources

- **Spring Security Documentation:** https://spring.io/projects/spring-security
- **JWT.io:** https://jwt.io/ (to decode and verify tokens)
- **Spring Boot Reference:** https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## ✅ Checklist for Next Development Session

- [ ] Create Vehicle entity and repository
- [ ] Create VehicleController with CRUD operations
- [ ] Create VehicleService for business logic
- [ ] Create Appointment entity
- [ ] Create AppointmentController
- [ ] Add role-based endpoint security (@PreAuthorize)
- [ ] Create Service entity
- [ ] Implement service history tracking

---

**Current Status:** Authentication system is fully functional and ready for integration with frontend. You can now build the automobile-specific features on top of this secure foundation.


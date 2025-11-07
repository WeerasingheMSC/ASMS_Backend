# 🎯 Frontend Integration - Login Response with User ID

## What Your Frontend Will Receive

### Login Request
```javascript
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "customer@test.com",
  "password": "password123"
}
```

### Login Response ✅
```javascript
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lckB0ZXN0LmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNjk5MjcwMDAwLCJleHAiOjE2OTkzNTY0MDB9.R7uZ...",
  "userId": 1,                    // ⭐⭐⭐ USER ID - NEW FIELD
  "username": "customer@test.com",
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "profileImage": null,
  "message": "Login successful"
}
```

---

## Complete Frontend Login Example (React)

```javascript
import { useState } from 'react';
import axios from 'axios';

function LoginPage() {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  const [userData, setUserData] = useState(null);

  const handleLogin = async (e) => {
    e.preventDefault();
    
    try {
      const response = await axios.post(
        'http://localhost:8080/api/auth/login',
        credentials
      );
      
      const data = response.data;
      
      // ⭐ Store all user data including userId
      localStorage.setItem('token', data.token);
      localStorage.setItem('userId', data.userId);           // ⭐ NEW
      localStorage.setItem('username', data.username);
      localStorage.setItem('email', data.email);
      localStorage.setItem('role', data.role);
      localStorage.setItem('profileImage', data.profileImage || '');
      
      // Set state
      setUserData(data);
      
      console.log('✅ Login successful!');
      console.log('👤 User ID:', data.userId);               // ⭐ NEW
      console.log('📧 Email:', data.email);
      console.log('🎭 Role:', data.role);
      
      // Redirect based on role
      if (data.role === 'ADMIN') {
        window.location.href = '/admin/dashboard';
      } else if (data.role === 'EMPLOYEE') {
        window.location.href = '/employee/dashboard';
      } else {
        window.location.href = '/customer/dashboard';
      }
      
    } catch (error) {
      console.error('❌ Login failed:', error);
      alert('Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <input
          type="text"
          placeholder="Username or Email"
          value={credentials.username}
          onChange={(e) => setCredentials({
            ...credentials,
            username: e.target.value
          })}
        />
        <input
          type="password"
          placeholder="Password"
          value={credentials.password}
          onChange={(e) => setCredentials({
            ...credentials,
            password: e.target.value
          })}
        />
        <button type="submit">Login</button>
      </form>
      
      {userData && (
        <div className="user-info">
          <h3>Welcome!</h3>
          <p>User ID: {userData.userId}</p>           {/* ⭐ Display userId */}
          <p>Username: {userData.username}</p>
          <p>Email: {userData.email}</p>
          <p>Role: {userData.role}</p>
        </div>
      )}
    </div>
  );
}

export default LoginPage;
```

---

## Using User ID Throughout Your App

### 1. Protected Route with User ID
```javascript
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');      // ⭐ Check userId
  
  if (!token || !userId) {
    return <Navigate to="/login" />;
  }
  
  return children;
}
```

### 2. Dashboard Component
```javascript
import { useEffect, useState } from 'react';
import axios from 'axios';

function Dashboard() {
  const [appointments, setAppointments] = useState([]);
  const [notifications, setNotifications] = useState([]);
  
  const userId = localStorage.getItem('userId');      // ⭐ Get userId
  const token = localStorage.getItem('token');
  
  useEffect(() => {
    fetchUserData();
  }, [userId]);
  
  const fetchUserData = async () => {
    try {
      // Fetch appointments for this user
      const appointmentsRes = await axios.get(
        'http://localhost:8080/api/customer/appointments',
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      setAppointments(appointmentsRes.data.data);
      
      // Fetch notifications for this user
      const notificationsRes = await axios.get(
        'http://localhost:8080/api/notifications',
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      setNotifications(notificationsRes.data.data);
      
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  };
  
  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      <div className="user-info">
        <p>User ID: {userId}</p>                      {/* ⭐ Display userId */}
        <p>Username: {localStorage.getItem('username')}</p>
      </div>
      
      <div className="appointments">
        <h2>My Appointments</h2>
        {appointments.map(apt => (
          <div key={apt.id}>
            <p>Appointment #{apt.id}</p>
            <p>Date: {apt.preferredDate}</p>
            <p>Status: {apt.status}</p>
          </div>
        ))}
      </div>
      
      <div className="notifications">
        <h2>My Notifications</h2>
        {notifications.map(notif => (
          <div key={notif.id}>
            <h3>{notif.title}</h3>
            <p>{notif.message}</p>
            <small>{notif.createdAt}</small>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Dashboard;
```

### 3. WebSocket with User ID
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useEffect } from 'react';

function NotificationListener() {
  const userId = localStorage.getItem('userId');     // ⭐ Get userId
  
  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, (frame) => {
      console.log('Connected to WebSocket');
      
      // Subscribe to user-specific notifications
      stompClient.subscribe(
        `/topic/notifications/user.${userId}`,       // ⭐ Use userId
        (message) => {
          const notification = JSON.parse(message.body);
          console.log('New notification for user', userId, ':', notification);
          
          // Show notification to user
          showToast(notification.title, notification.message);
        }
      );
    });
    
    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, [userId]);
  
  const showToast = (title, message) => {
    // Your toast/notification UI logic
    alert(`${title}: ${message}`);
  };
  
  return null;
}

export default NotificationListener;
```

### 4. Create Appointment with User ID
```javascript
function CreateAppointment() {
  const userId = localStorage.getItem('userId');     // ⭐ Get userId
  const token = localStorage.getItem('token');
  
  const [formData, setFormData] = useState({
    vehicleType: '',
    vehicleModel: '',
    vehiclePlateNumber: '',
    serviceType: '',
    preferredDate: '',
    description: ''
  });
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await axios.post(
        'http://localhost:8080/api/customer/appointments',
        {
          ...formData,
          customerId: userId                        // ⭐ Include userId
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      console.log('✅ Appointment created:', response.data);
      alert('Appointment created successfully!');
      
    } catch (error) {
      console.error('❌ Error creating appointment:', error);
      alert('Failed to create appointment');
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
      <button type="submit">Create Appointment</button>
    </form>
  );
}
```

### 5. User Profile Page
```javascript
function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const userId = localStorage.getItem('userId');     // ⭐ Get userId
  const token = localStorage.getItem('token');
  
  useEffect(() => {
    fetchProfile();
  }, [userId]);
  
  const fetchProfile = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/users/${userId}`, // ⭐ Use userId in URL
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      setProfile(response.data);
    } catch (error) {
      console.error('Error fetching profile:', error);
    }
  };
  
  return (
    <div className="profile">
      <h1>My Profile</h1>
      {profile && (
        <div>
          <p>User ID: {profile.id}</p>               {/* ⭐ Display userId */}
          <p>Username: {profile.username}</p>
          <p>Email: {profile.email}</p>
          <p>Role: {profile.role}</p>
          <p>Phone: {profile.phoneNumber}</p>
        </div>
      )}
    </div>
  );
}
```

---

## Axios Interceptor (Recommended)

Set up an Axios interceptor to automatically include the token in all requests:

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Request interceptor to add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

// Usage in components
import api from './api';

const fetchData = async () => {
  const userId = localStorage.getItem('userId');
  const response = await api.get(`/users/${userId}`);  // Token added automatically
};
```

---

## React Context for User Data

```javascript
// UserContext.js
import { createContext, useContext, useState, useEffect } from 'react';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState({
    token: localStorage.getItem('token'),
    userId: localStorage.getItem('userId'),           // ⭐ Include userId
    username: localStorage.getItem('username'),
    email: localStorage.getItem('email'),
    role: localStorage.getItem('role'),
    profileImage: localStorage.getItem('profileImage')
  });

  const login = (loginData) => {
    setUser({
      token: loginData.token,
      userId: loginData.userId,                       // ⭐ Store userId
      username: loginData.username,
      email: loginData.email,
      role: loginData.role,
      profileImage: loginData.profileImage
    });
    
    // Persist to localStorage
    localStorage.setItem('token', loginData.token);
    localStorage.setItem('userId', loginData.userId);
    localStorage.setItem('username', loginData.username);
    localStorage.setItem('email', loginData.email);
    localStorage.setItem('role', loginData.role);
    localStorage.setItem('profileImage', loginData.profileImage || '');
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

// Usage in components
function MyComponent() {
  const { user } = useUser();
  
  return (
    <div>
      <h1>Welcome, {user.username}</h1>
      <p>Your User ID: {user.userId}</p>              {/* ⭐ Access userId */}
    </div>
  );
}
```

---

## ✅ Summary

**What you receive from login:**
```javascript
{
  token: "eyJhbGci...",
  userId: 1,              ⭐ USER ID - Use this in your frontend
  username: "john@test.com",
  email: "john@test.com",
  role: "CUSTOMER",
  profileImage: null,
  message: "Login successful"
}
```

**How to use it:**
1. ✅ Store in localStorage
2. ✅ Access anywhere with `localStorage.getItem('userId')`
3. ✅ Use in API URLs
4. ✅ Use in WebSocket subscriptions
5. ✅ Display in UI components

**Your application is ready! Start it and test the login! 🚀**


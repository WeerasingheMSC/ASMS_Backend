# 🚀 WebSocket Quick Start Guide

## 5-Minute Setup

### ✅ Backend Setup (Already Done!)

Your Spring Boot backend is ready with:
- ✅ JWT authentication for WebSocket
- ✅ Token validation in handshake
- ✅ User-specific notification channels
- ✅ Multiple WebSocket endpoints

---

## 🌐 Frontend Setup

### Step 1: Install Dependencies

```bash
npm install sockjs-client @stomp/stompjs
```

### Step 2: Create WebSocket Service

Create `src/services/webSocketService.js`:

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
  }

  connect(onConnect, onError) {
    const token = localStorage.getItem('token');
    const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
    this.stompClient = Stomp.over(socket);
    
    this.stompClient.connect(
      {},
      (frame) => {
        this.connected = true;
        onConnect(frame);
      },
      (error) => {
        this.connected = false;
        onError(error);
      }
    );
  }

  subscribe(destination, callback) {
    if (this.stompClient && this.connected) {
      return this.stompClient.subscribe(destination, (message) => {
        callback(JSON.parse(message.body));
      });
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.connected = false;
    }
  }
}

export const webSocketService = new WebSocketService();
```

### Step 3: Use in Your Component

```javascript
import React, { useEffect, useState } from 'react';
import { webSocketService } from './services/webSocketService';

function App() {
  const [connected, setConnected] = useState(false);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    // Connect to WebSocket
    webSocketService.connect(
      () => {
        console.log('✅ Connected!');
        setConnected(true);
        
        // Subscribe to notifications
        const userId = localStorage.getItem('userId');
        webSocketService.subscribe(
          `/topic/notifications/user.${userId}`,
          (notification) => {
            console.log('📬 New notification:', notification);
            setNotifications(prev => [notification, ...prev]);
          }
        );
      },
      (error) => {
        console.error('❌ Connection failed:', error);
        setConnected(false);
      }
    );

    return () => webSocketService.disconnect();
  }, []);

  return (
    <div>
      <h1>WebSocket Status: {connected ? '🟢' : '🔴'}</h1>
      <div>
        <h2>Notifications ({notifications.length})</h2>
        {notifications.map((notif, i) => (
          <div key={i}>
            <h3>{notif.title}</h3>
            <p>{notif.message}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;
```

---

## 🧪 Test It!

### 1. Start Backend
```bash
cd C:\Users\M S I\Desktop\ASMS_Backend
mvnw.cmd spring-boot:run
```

### 2. Start Frontend
```bash
cd your-frontend-project
npm start
```

### 3. Login
```javascript
// Login to get token
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'customer@test.com',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => {
  localStorage.setItem('token', data.token);
  localStorage.setItem('userId', data.userId);
});
```

### 4. Connect WebSocket
The connection will happen automatically when your component mounts!

### 5. Test Notification
Create an appointment or perform any action that triggers a notification.

---

## 📋 Connection URL Format

```
ws://localhost:8080/ws?token=YOUR_JWT_TOKEN
```

**Example**:
```
ws://localhost:8080/ws?token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0...
```

---

## 🎯 Notification Channels

### For Each User
```javascript
const userId = localStorage.getItem('userId');
webSocketService.subscribe(
  `/topic/notifications/user.${userId}`,
  (notification) => {
    console.log('Personal notification:', notification);
  }
);
```

### For Admins Only
```javascript
if (userRole === 'ADMIN') {
  webSocketService.subscribe(
    '/topic/notifications/admin',
    (notification) => {
      console.log('Admin notification:', notification);
    }
  );
}
```

---

## 💡 Tips

### Enable Browser Notifications
```javascript
// Request permission
if ('Notification' in window) {
  Notification.requestPermission();
}

// Show notification
function showNotification(data) {
  if (Notification.permission === 'granted') {
    new Notification(data.title, {
      body: data.message,
      icon: '/icon.png'
    });
  }
}
```

### Auto-Reconnect
```javascript
function connectWithRetry(retries = 0) {
  webSocketService.connect(
    () => console.log('Connected'),
    (error) => {
      if (retries < 5) {
        setTimeout(() => connectWithRetry(retries + 1), 5000);
      }
    }
  );
}
```

### Connection Status Indicator
```javascript
function ConnectionStatus({ connected }) {
  return (
    <div className={`status ${connected ? 'connected' : 'disconnected'}`}>
      {connected ? '🟢 Connected' : '🔴 Disconnected'}
    </div>
  );
}
```

---

## 🔧 Troubleshooting

### Issue: Can't connect
**Check**:
- Is backend running?
- Do you have a valid token?
- Is token stored in localStorage?

```javascript
// Debug
console.log('Token:', localStorage.getItem('token'));
console.log('User ID:', localStorage.getItem('userId'));
```

### Issue: Token expired
**Solution**: Redirect to login
```javascript
if (error.message.includes('401')) {
  localStorage.clear();
  window.location.href = '/login';
}
```

### Issue: Not receiving notifications
**Check**:
- Are you subscribed to the correct channel?
- Does your userId match?
- Is the notification being sent from backend?

```javascript
// Verify subscription
const userId = localStorage.getItem('userId');
console.log('Subscribed to:', `/topic/notifications/user.${userId}`);
```

---

## ✅ Checklist

Before deploying:
- [ ] Backend running on port 8080
- [ ] Frontend has correct backend URL
- [ ] WebSocket dependencies installed
- [ ] Token stored after login
- [ ] UserId stored after login
- [ ] Subscribed to correct channel
- [ ] Error handling implemented
- [ ] Auto-reconnect logic added

---

## 📚 Full Documentation

For complete details, see:
- `WEBSOCKET_JWT_AUTHENTICATION_GUIDE.md` - Full guide
- `WEBSOCKET_REACT_COMPLETE_EXAMPLE.md` - Complete React app
- `WEBSOCKET_IMPLEMENTATION_SUMMARY.md` - Summary

---

## 🎉 Done!

Your WebSocket notification system is ready!

**Connection**: ✅  
**Authentication**: ✅  
**Notifications**: ✅  
**Real-time**: ✅  

**Happy coding! 🚀**


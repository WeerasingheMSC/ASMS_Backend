# WebSocket JWT Authentication - Complete Guide

## ✅ Implementation Complete!

Your WebSocket configuration now supports JWT authentication via query parameters. Users can connect to WebSocket endpoints with their authentication token.

---

## 🔧 What Was Changed

### Updated: `WebSocketConfig.java`

**Location**: `src/main/java/com/example/demo/config/WebSocketConfig.java`

**Key Changes**:
1. ✅ Added `JwtTokenProvider` autowiring for token validation
2. ✅ Created custom `JwtHandshakeHandler` to extract token from query parameters
3. ✅ Applied JWT authentication to all WebSocket endpoints
4. ✅ Stores `userId` and `username` in WebSocket session attributes

**Features**:
- Validates JWT token during WebSocket handshake
- Extracts username and userId from token
- Creates authenticated Principal for WebSocket session
- Supports multiple endpoints: `/ws`, `/ws/notifications`, `/ws/project-updates`

---

## 📡 WebSocket Endpoints

### 1. Main WebSocket Endpoint
```
ws://localhost:8080/ws?token=YOUR_JWT_TOKEN
```

### 2. Notifications Endpoint
```
ws://localhost:8080/ws/notifications?token=YOUR_JWT_TOKEN
```

### 3. Project Updates Endpoint
```
ws://localhost:8080/ws/project-updates?token=YOUR_JWT_TOKEN
```

---

## 🌐 Frontend Integration

### 1. React with SockJS and STOMP

#### Install Dependencies
```bash
npm install sockjs-client @stomp/stompjs
```

#### WebSocket Connection Component
```javascript
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function NotificationWebSocket() {
  const [stompClient, setStompClient] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    connectWebSocket();
    return () => disconnectWebSocket();
  }, []);

  const connectWebSocket = () => {
    // Get token and userId from localStorage
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (!token || !userId) {
      console.error('No token or userId found');
      return;
    }

    // Create WebSocket connection with token as query parameter
    const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
    const client = Stomp.over(socket);

    // Optional: Disable debug logs
    client.debug = () => {};

    // Connect to WebSocket
    client.connect(
      {},
      (frame) => {
        console.log('✅ Connected to WebSocket:', frame);
        setConnected(true);

        // Subscribe to user-specific notifications
        client.subscribe(`/topic/notifications/user.${userId}`, (message) => {
          const notification = JSON.parse(message.body);
          console.log('📬 New notification:', notification);
          
          // Add to notifications list
          setNotifications(prev => [notification, ...prev]);
          
          // Show browser notification (optional)
          showBrowserNotification(notification);
        });

        // Subscribe to admin notifications (if user is admin)
        const userRole = localStorage.getItem('role');
        if (userRole === 'ADMIN') {
          client.subscribe('/topic/notifications/admin', (message) => {
            const notification = JSON.parse(message.body);
            console.log('📬 Admin notification:', notification);
            setNotifications(prev => [notification, ...prev]);
          });
        }
      },
      (error) => {
        console.error('❌ WebSocket connection error:', error);
        setConnected(false);
        
        // Retry connection after 5 seconds
        setTimeout(() => {
          console.log('🔄 Reconnecting...');
          connectWebSocket();
        }, 5000);
      }
    );

    setStompClient(client);
  };

  const disconnectWebSocket = () => {
    if (stompClient) {
      stompClient.disconnect(() => {
        console.log('🔌 Disconnected from WebSocket');
        setConnected(false);
      });
    }
  };

  const showBrowserNotification = (notification) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/notification-icon.png',
        tag: notification.notificationId
      });
    }
  };

  return (
    <div className="notification-websocket">
      <div className="connection-status">
        {connected ? (
          <span className="status-connected">🟢 Connected</span>
        ) : (
          <span className="status-disconnected">🔴 Disconnected</span>
        )}
      </div>

      <div className="notifications-list">
        <h3>Real-time Notifications</h3>
        {notifications.map((notif, index) => (
          <div key={index} className="notification-item">
            <h4>{notif.title}</h4>
            <p>{notif.message}</p>
            <small>{notif.type}</small>
          </div>
        ))}
      </div>
    </div>
  );
}

export default NotificationWebSocket;
```

### 2. React Custom Hook for WebSocket

#### useWebSocket.js
```javascript
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export const useWebSocket = () => {
  const stompClientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    connect();
    return () => disconnect();
  }, []);

  const connect = () => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      console.error('No authentication token found');
      return;
    }

    const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
    const client = Stomp.over(socket);
    
    client.debug = () => {};

    client.connect(
      {},
      () => {
        console.log('✅ WebSocket connected');
        setConnected(true);
        stompClientRef.current = client;
      },
      (error) => {
        console.error('❌ WebSocket error:', error);
        setConnected(false);
        
        // Auto-reconnect
        setTimeout(connect, 5000);
      }
    );
  };

  const disconnect = () => {
    if (stompClientRef.current) {
      stompClientRef.current.disconnect();
      setConnected(false);
    }
  };

  const subscribe = (destination, callback) => {
    if (stompClientRef.current && connected) {
      return stompClientRef.current.subscribe(destination, callback);
    }
    return null;
  };

  const send = (destination, body) => {
    if (stompClientRef.current && connected) {
      stompClientRef.current.send(destination, {}, JSON.stringify(body));
    }
  };

  return {
    connected,
    subscribe,
    send,
    disconnect,
    reconnect: connect
  };
};

// Usage in component
function MyComponent() {
  const { connected, subscribe } = useWebSocket();
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    if (connected && userId) {
      const subscription = subscribe(
        `/topic/notifications/user.${userId}`,
        (message) => {
          const notification = JSON.parse(message.body);
          console.log('New notification:', notification);
        }
      );

      return () => subscription?.unsubscribe();
    }
  }, [connected, userId, subscribe]);

  return <div>WebSocket Status: {connected ? '🟢' : '🔴'}</div>;
}
```

### 3. Vanilla JavaScript

```javascript
// Connect to WebSocket with JWT token
function connectWebSocket() {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');

  if (!token || !userId) {
    console.error('Authentication required');
    return;
  }

  // Create SockJS connection with token
  const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
  const stompClient = Stomp.over(socket);

  // Connect
  stompClient.connect(
    {},
    function(frame) {
      console.log('Connected:', frame);

      // Subscribe to notifications
      stompClient.subscribe(
        `/topic/notifications/user.${userId}`,
        function(message) {
          const notification = JSON.parse(message.body);
          console.log('New notification:', notification);
          displayNotification(notification);
        }
      );
    },
    function(error) {
      console.error('Connection error:', error);
    }
  );
}

function displayNotification(notification) {
  // Your UI logic to display the notification
  const notificationDiv = document.createElement('div');
  notificationDiv.className = 'notification';
  notificationDiv.innerHTML = `
    <h4>${notification.title}</h4>
    <p>${notification.message}</p>
    <span>${notification.type}</span>
  `;
  document.getElementById('notifications-container').prepend(notificationDiv);
}

// Call when page loads
document.addEventListener('DOMContentLoaded', connectWebSocket);
```

### 4. Angular

```typescript
import { Injectable } from '@angular/core';
import * as SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: any;
  private connectedSubject = new BehaviorSubject<boolean>(false);
  public connected$ = this.connectedSubject.asObservable();

  constructor() {
    this.connect();
  }

  connect(): void {
    const token = localStorage.getItem('token');
    
    if (!token) {
      console.error('No authentication token');
      return;
    }

    const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect(
      {},
      (frame: any) => {
        console.log('Connected:', frame);
        this.connectedSubject.next(true);
      },
      (error: any) => {
        console.error('Connection error:', error);
        this.connectedSubject.next(false);
        
        // Retry connection
        setTimeout(() => this.connect(), 5000);
      }
    );
  }

  subscribe(destination: string, callback: (message: any) => void): any {
    if (this.stompClient && this.connectedSubject.value) {
      return this.stompClient.subscribe(destination, callback);
    }
    return null;
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.connectedSubject.next(false);
    }
  }
}

// Usage in component
export class NotificationComponent implements OnInit, OnDestroy {
  constructor(private webSocketService: WebSocketService) {}

  ngOnInit(): void {
    const userId = localStorage.getItem('userId');
    
    this.webSocketService.connected$.subscribe(connected => {
      if (connected && userId) {
        this.webSocketService.subscribe(
          `/topic/notifications/user.${userId}`,
          (message: any) => {
            const notification = JSON.parse(message.body);
            console.log('New notification:', notification);
          }
        );
      }
    });
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
  }
}
```

---

## 🧪 Testing WebSocket Connection

### Test with Postman

1. **Get JWT Token**:
   ```
   POST http://localhost:8080/api/auth/login
   Body: {
     "username": "customer@test.com",
     "password": "password123"
   }
   ```

2. **Copy the token from response**

3. **Test WebSocket in Postman**:
   - Create new WebSocket request
   - URL: `ws://localhost:8080/ws?token=YOUR_JWT_TOKEN`
   - Click Connect
   - You should see successful connection

### Test with Browser Console

```javascript
// 1. Login first to get token
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
  console.log('Token saved:', data.token);
});

// 2. Connect to WebSocket
const token = localStorage.getItem('token');
const userId = localStorage.getItem('userId');
const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('✅ Connected:', frame);
  
  stompClient.subscribe(`/topic/notifications/user.${userId}`, (message) => {
    console.log('📬 New notification:', JSON.parse(message.body));
  });
});
```

---

## 🔒 Security Features

### What's Protected
✅ WebSocket connections require valid JWT token  
✅ Token is validated during handshake  
✅ Invalid tokens are rejected  
✅ User identity extracted from token  
✅ User-specific channels secured by userId  

### Token Validation
- Token must be valid (not expired)
- Token signature must match
- Token must be in query parameter: `?token=YOUR_JWT_TOKEN`

### Connection Flow
```
1. Client initiates WebSocket connection with token
   ↓
2. Backend validates JWT token in JwtHandshakeHandler
   ↓
3. If valid: Extract username & userId, create Principal
   ↓
4. If invalid: Connection rejected, returns null
   ↓
5. Successful connection: User can subscribe to authorized channels
```

---

## 📊 Notification Channels

### User-Specific Channel
```javascript
// Each user has their own notification channel
stompClient.subscribe(`/topic/notifications/user.${userId}`, callback);
```

**Example**:
- User ID 1: `/topic/notifications/user.1`
- User ID 2: `/topic/notifications/user.2`
- User ID 123: `/topic/notifications/user.123`

### Admin Broadcast Channel
```javascript
// All admins receive notifications here
stompClient.subscribe('/topic/notifications/admin', callback);
```

### Project Updates Channel
```javascript
// Project-specific updates
stompClient.subscribe('/topic/project-updates', callback);
```

---

## 🐛 Troubleshooting

### Issue: Connection Rejected / 401 Unauthorized
**Cause**: Invalid or missing JWT token

**Solution**:
```javascript
// Make sure token is valid and not expired
const token = localStorage.getItem('token');
if (!token) {
  // Redirect to login
  window.location.href = '/login';
}
```

### Issue: Token Expired During Connection
**Cause**: JWT token expired

**Solution**:
```javascript
// Check token expiration before connecting
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch (e) {
    return true;
  }
}

if (isTokenExpired(token)) {
  // Refresh token or redirect to login
  window.location.href = '/login';
}
```

### Issue: Connection Drops Frequently
**Cause**: Network issues or server restart

**Solution**: Implement auto-reconnect
```javascript
function connectWithRetry(retryCount = 0) {
  const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
  const client = Stomp.over(socket);

  client.connect(
    {},
    () => console.log('Connected'),
    (error) => {
      console.error('Connection failed, retrying...', error);
      if (retryCount < 10) {
        setTimeout(() => connectWithRetry(retryCount + 1), 5000);
      }
    }
  );
}
```

### Issue: Not Receiving Notifications
**Cause**: Subscribed to wrong channel or userId mismatch

**Solution**:
```javascript
// Verify userId matches
const userId = localStorage.getItem('userId');
console.log('Subscribing to:', `/topic/notifications/user.${userId}`);

// Make sure backend sends to correct channel
// Backend should use: messagingTemplate.convertAndSend(`/topic/notifications/user.${recipientId}`, message);
```

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Compiling 91 source files ✓
[INFO] No compilation errors ✓
```

---

## 🚀 Start Application

```bash
mvnw.cmd spring-boot:run
```

---

## 📚 Complete Example App

See `WEBSOCKET_AUTHENTICATION_EXAMPLE.md` for a complete React example application.

---

## 🎯 Summary

**What's Implemented:**
✅ JWT authentication for WebSocket connections  
✅ Token validation during handshake  
✅ User identity extraction from token  
✅ Multiple WebSocket endpoints configured  
✅ User-specific notification channels  
✅ Admin broadcast channels  

**Frontend Connection:**
```javascript
const token = localStorage.getItem('token');
const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
```

**Subscribe to Notifications:**
```javascript
const userId = localStorage.getItem('userId');
stompClient.subscribe(`/topic/notifications/user.${userId}`, callback);
```

**Your WebSocket system is now secure and ready to use! 🎉**


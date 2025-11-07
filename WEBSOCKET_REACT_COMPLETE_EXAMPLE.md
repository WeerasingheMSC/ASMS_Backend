# Complete React WebSocket Example Application

## Full React App with WebSocket Notifications

### Project Structure
```
src/
├── components/
│   ├── Login.jsx
│   ├── Dashboard.jsx
│   ├── NotificationBell.jsx
│   └── NotificationList.jsx
├── services/
│   ├── authService.js
│   └── webSocketService.js
├── hooks/
│   └── useWebSocket.js
├── contexts/
│   └── UserContext.jsx
├── App.jsx
└── index.js
```

---

## 1. Services

### authService.js
```javascript
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const authService = {
  login: async (username, password) => {
    try {
      const response = await axios.post(`${API_URL}/auth/login`, {
        username,
        password
      });
      
      const { token, userId, email, role, profileImage } = response.data;
      
      // Store in localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('userId', userId);
      localStorage.setItem('username', username);
      localStorage.setItem('email', email);
      localStorage.setItem('role', role);
      if (profileImage) {
        localStorage.setItem('profileImage', profileImage);
      }
      
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || 'Login failed';
    }
  },

  logout: () => {
    localStorage.clear();
    window.location.href = '/login';
  },

  isAuthenticated: () => {
    const token = localStorage.getItem('token');
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch (e) {
      return false;
    }
  },

  getToken: () => localStorage.getItem('token'),
  getUserId: () => localStorage.getItem('userId'),
  getUsername: () => localStorage.getItem('username'),
  getRole: () => localStorage.getItem('role')
};
```

### webSocketService.js
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = [];
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
  }

  connect(onConnect, onError) {
    const token = localStorage.getItem('token');
    
    if (!token) {
      console.error('No authentication token found');
      if (onError) onError('No token');
      return;
    }

    try {
      const socket = new SockJS(`http://localhost:8080/ws?token=${token}`);
      this.stompClient = Stomp.over(socket);
      
      // Disable debug logging (optional)
      this.stompClient.debug = () => {};

      this.stompClient.connect(
        {},
        (frame) => {
          console.log('✅ WebSocket connected:', frame);
          this.connected = true;
          this.reconnectAttempts = 0;
          if (onConnect) onConnect(frame);
        },
        (error) => {
          console.error('❌ WebSocket connection error:', error);
          this.connected = false;
          if (onError) onError(error);
          
          // Auto-reconnect
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Reconnecting... Attempt ${this.reconnectAttempts}`);
            setTimeout(() => this.connect(onConnect, onError), 5000);
          }
        }
      );
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      if (onError) onError(error);
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions = [];
      this.stompClient.disconnect(() => {
        console.log('🔌 WebSocket disconnected');
        this.connected = false;
      });
    }
  }

  subscribe(destination, callback) {
    if (this.stompClient && this.connected) {
      const subscription = this.stompClient.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      });
      
      this.subscriptions.push(subscription);
      return subscription;
    }
    return null;
  }

  send(destination, body) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(destination, {}, JSON.stringify(body));
    }
  }

  isConnected() {
    return this.connected;
  }
}

// Export singleton instance
export const webSocketService = new WebSocketService();
```

---

## 2. Custom Hooks

### useWebSocket.js
```javascript
import { useEffect, useState, useCallback } from 'react';
import { webSocketService } from '../services/webSocketService';
import { authService } from '../services/authService';

export const useWebSocket = () => {
  const [connected, setConnected] = useState(false);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    if (authService.isAuthenticated()) {
      connectWebSocket();
    }

    return () => {
      webSocketService.disconnect();
    };
  }, []);

  const connectWebSocket = () => {
    webSocketService.connect(
      () => {
        console.log('WebSocket connected successfully');
        setConnected(true);
        subscribeToNotifications();
      },
      (error) => {
        console.error('WebSocket connection error:', error);
        setConnected(false);
      }
    );
  };

  const subscribeToNotifications = () => {
    const userId = authService.getUserId();
    const role = authService.getRole();

    // Subscribe to user-specific notifications
    webSocketService.subscribe(
      `/topic/notifications/user.${userId}`,
      (notification) => {
        console.log('📬 New notification:', notification);
        setNotifications(prev => [notification, ...prev]);
        
        // Show browser notification
        showBrowserNotification(notification);
      }
    );

    // Subscribe to admin notifications if user is admin
    if (role === 'ADMIN') {
      webSocketService.subscribe(
        '/topic/notifications/admin',
        (notification) => {
          console.log('📬 Admin notification:', notification);
          setNotifications(prev => [notification, ...prev]);
          showBrowserNotification(notification);
        }
      );
    }
  };

  const showBrowserNotification = (notification) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/notification-icon.png',
        tag: `notification-${notification.notificationId}`
      });
    }
  };

  const requestNotificationPermission = useCallback(async () => {
    if ('Notification' in window && Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }
    return Notification.permission === 'granted';
  }, []);

  const clearNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  return {
    connected,
    notifications,
    clearNotifications,
    requestNotificationPermission,
    reconnect: connectWebSocket
  };
};
```

---

## 3. Components

### Login.jsx
```javascript
import React, { useState } from 'react';
import { authService } from '../services/authService';
import './Login.css';

function Login({ onLoginSuccess }) {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const userData = await authService.login(
        credentials.username,
        credentials.password
      );
      
      console.log('✅ Login successful:', userData);
      
      if (onLoginSuccess) {
        onLoginSuccess(userData);
      }
    } catch (err) {
      setError(err || 'Login failed. Please try again.');
      console.error('❌ Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>🚗 ASMS Login</h2>
        <p className="subtitle">Automobile Service Management System</p>
        
        {error && (
          <div className="error-message">
            ⚠️ {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username or Email</label>
            <input
              type="text"
              id="username"
              name="username"
              value={credentials.username}
              onChange={handleChange}
              placeholder="Enter your username or email"
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={credentials.password}
              onChange={handleChange}
              placeholder="Enter your password"
              required
            />
          </div>

          <button 
            type="submit" 
            className="login-button"
            disabled={loading}
          >
            {loading ? '⏳ Logging in...' : '🔐 Login'}
          </button>
        </form>

        <div className="login-footer">
          <a href="/forgot-password">Forgot Password?</a>
          <span>•</span>
          <a href="/signup">Create Account</a>
        </div>
      </div>
    </div>
  );
}

export default Login;
```

### NotificationBell.jsx
```javascript
import React, { useState } from 'react';
import './NotificationBell.css';

function NotificationBell({ notifications, onMarkAllRead }) {
  const [showDropdown, setShowDropdown] = useState(false);
  
  const unreadCount = notifications.filter(n => !n.isRead).length;

  const handleBellClick = () => {
    setShowDropdown(!showDropdown);
  };

  const handleMarkAllRead = () => {
    if (onMarkAllRead) {
      onMarkAllRead();
    }
  };

  return (
    <div className="notification-bell-container">
      <button 
        className="notification-bell" 
        onClick={handleBellClick}
        aria-label="Notifications"
      >
        🔔
        {unreadCount > 0 && (
          <span className="notification-badge">{unreadCount}</span>
        )}
      </button>

      {showDropdown && (
        <div className="notification-dropdown">
          <div className="notification-header">
            <h3>Notifications</h3>
            {unreadCount > 0 && (
              <button 
                className="mark-all-read"
                onClick={handleMarkAllRead}
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="notification-list">
            {notifications.length === 0 ? (
              <div className="no-notifications">
                <p>📭 No notifications</p>
              </div>
            ) : (
              notifications.slice(0, 10).map((notification, index) => (
                <div 
                  key={index} 
                  className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                >
                  <div className="notification-icon">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="notification-content">
                    <h4>{notification.title}</h4>
                    <p>{notification.message}</p>
                    <span className="notification-time">
                      {formatTime(notification.timestamp || notification.createdAt)}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>

          {notifications.length > 10 && (
            <div className="notification-footer">
              <a href="/notifications">View All Notifications</a>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function getNotificationIcon(type) {
  const icons = {
    APPOINTMENT_CREATED: '📅',
    APPOINTMENT_CONFIRMED: '✅',
    APPOINTMENT_CANCELLED: '❌',
    EMPLOYEE_ASSIGNED: '👷',
    STATUS_CHANGED_IN_SERVICE: '🔧',
    STATUS_CHANGED_READY: '✨',
    STATUS_CHANGED_COMPLETED: '🎉',
    GENERAL: '📢'
  };
  return icons[type] || '📬';
}

function formatTime(timestamp) {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  
  return date.toLocaleDateString();
}

export default NotificationBell;
```

### Dashboard.jsx
```javascript
import React, { useEffect, useState } from 'react';
import { authService } from '../services/authService';
import { useWebSocket } from '../hooks/useWebSocket';
import NotificationBell from './NotificationBell';
import './Dashboard.css';

function Dashboard() {
  const { connected, notifications, requestNotificationPermission, reconnect } = useWebSocket();
  const [user, setUser] = useState({});

  useEffect(() => {
    // Load user data
    setUser({
      userId: authService.getUserId(),
      username: authService.getUsername(),
      role: authService.getRole()
    });

    // Request notification permission
    requestNotificationPermission();
  }, [requestNotificationPermission]);

  const handleLogout = () => {
    authService.logout();
  };

  const handleReconnect = () => {
    reconnect();
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>🚗 ASMS Dashboard</h1>
        </div>
        
        <div className="header-center">
          <div className="connection-status">
            {connected ? (
              <span className="status-indicator connected">
                🟢 WebSocket Connected
              </span>
            ) : (
              <span className="status-indicator disconnected">
                🔴 WebSocket Disconnected
                <button onClick={handleReconnect} className="reconnect-btn">
                  🔄 Reconnect
                </button>
              </span>
            )}
          </div>
        </div>

        <div className="header-right">
          <NotificationBell notifications={notifications} />
          <div className="user-menu">
            <span className="username">👤 {user.username}</span>
            <span className="user-role">{user.role}</span>
            <button onClick={handleLogout} className="logout-btn">
              🚪 Logout
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-content">
        <div className="welcome-section">
          <h2>Welcome back, {user.username}!</h2>
          <p>User ID: {user.userId}</p>
          <p>Role: {user.role}</p>
        </div>

        <div className="notifications-section">
          <h3>Recent Notifications ({notifications.length})</h3>
          <div className="notifications-grid">
            {notifications.length === 0 ? (
              <p className="no-data">📭 No notifications yet</p>
            ) : (
              notifications.map((notification, index) => (
                <div key={index} className="notification-card">
                  <div className="notification-type">{notification.type}</div>
                  <h4>{notification.title}</h4>
                  <p>{notification.message}</p>
                  <small>
                    {new Date(notification.timestamp || notification.createdAt).toLocaleString()}
                  </small>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="stats-section">
          <div className="stat-card">
            <h3>🔔 Total Notifications</h3>
            <p className="stat-number">{notifications.length}</p>
          </div>
          <div className="stat-card">
            <h3>📬 Unread</h3>
            <p className="stat-number">
              {notifications.filter(n => !n.isRead).length}
            </p>
          </div>
          <div className="stat-card">
            <h3>🌐 Connection</h3>
            <p className="stat-number">{connected ? '✅' : '❌'}</p>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;
```

### App.jsx
```javascript
import React, { useState, useEffect } from 'react';
import { authService } from './services/authService';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated
    const authenticated = authService.isAuthenticated();
    setIsAuthenticated(authenticated);
    setLoading(false);
  }, []);

  const handleLoginSuccess = (userData) => {
    console.log('Login successful:', userData);
    setIsAuthenticated(true);
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <h2>⏳ Loading...</h2>
      </div>
    );
  }

  return (
    <div className="App">
      {isAuthenticated ? (
        <Dashboard />
      ) : (
        <Login onLoginSuccess={handleLoginSuccess} />
      )}
    </div>
  );
}

export default App;
```

---

## 4. Styles (Optional)

### Login.css
```css
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 400px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.login-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  color: #666;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  margin: 0 5px;
}
```

### Dashboard.css
```css
.dashboard {
  min-height: 100vh;
  background: #f5f6fa;
}

.dashboard-header {
  background: white;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-indicator {
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
}

.status-indicator.connected {
  background: #d4edda;
  color: #155724;
}

.status-indicator.disconnected {
  background: #f8d7da;
  color: #721c24;
}

.reconnect-btn {
  margin-left: 10px;
  padding: 3px 10px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.dashboard-content {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  background: white;
  padding: 30px;
  border-radius: 10px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.notifications-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.notification-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-left: 4px solid #667eea;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #667eea;
  margin: 10px 0;
}

.logout-btn {
  margin-left: 20px;
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
```

---

## 5. Package.json Dependencies

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "sockjs-client": "^1.6.1",
    "@stomp/stompjs": "^7.0.0",
    "axios": "^1.6.2"
  }
}
```

---

## 🚀 Installation & Run

```bash
# Install dependencies
npm install sockjs-client @stomp/stompjs axios

# Start React app
npm start
```

---

## ✅ Complete!

This example provides:
- ✅ JWT authentication with login
- ✅ WebSocket connection with token
- ✅ Real-time notifications
- ✅ Notification bell with dropdown
- ✅ Auto-reconnect on disconnect
- ✅ Browser notifications
- ✅ User-specific channels
- ✅ Clean, modern UI

**Your complete React WebSocket notification system is ready! 🎉**


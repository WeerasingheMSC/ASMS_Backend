# Notification System Implementation Guide

## Overview
This notification system provides real-time notifications for your Automobile Service Management System (ASMS) using WebSocket technology and database persistence.

## Components Created

### 1. Model Layer

#### `Notification.java`
- Entity class for storing notifications in the database
- Fields: id, title, message, type, recipientId, appointmentId, isRead, createdAt, readAt
- Location: `src/main/java/com/example/demo/model/Notification.java`

#### `NotificationType.java`
- Enum defining notification types:
  - APPOINTMENT_CREATED
  - APPOINTMENT_CONFIRMED
  - APPOINTMENT_CANCELLED
  - EMPLOYEE_ASSIGNED
  - STATUS_CHANGED_IN_SERVICE
  - STATUS_CHANGED_READY
  - STATUS_CHANGED_COMPLETED
  - GENERAL
- Location: `src/main/java/com/example/demo/model/NotificationType.java`

### 2. Repository Layer

#### `NotificationRepository.java`
- JPA repository for Notification entity
- Custom query methods:
  - `findByRecipientIdOrderByCreatedAtDesc()` - Get all notifications for a user
  - `findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc()` - Get unread notifications
  - `findByAppointmentIdOrderByCreatedAtDesc()` - Get notifications by appointment
  - `countByRecipientIdAndIsReadFalse()` - Count unread notifications
- Location: `src/main/java/com/example/demo/repository/NotificationRepository.java`

### 3. Service Layer

#### `NotificationService.java`
- Core service for notification management
- Key methods:
  - `createAndSendNotification()` - Create and broadcast notification via WebSocket
  - `notifyCustomer()` - Send notification to customer
  - `notifyEmployee()` - Send notification to employee
  - `notifyAdmins()` - Broadcast notification to all admins
  - `getNotificationsByUser()` - Get all notifications for a user
  - `getUnreadNotifications()` - Get unread notifications
  - `markAsRead()` - Mark notification as read
  - `markAllAsRead()` - Mark all notifications as read
  - `deleteNotification()` - Delete a notification
- Location: `src/main/java/com/example/demo/service/NotificationService.java`

#### Updated `AppointmentService.java`
- Integrated notification calls for:
  - Appointment creation (notifies customer and admins)
  - Appointment approval (notifies customer)
  - Appointment rejection (notifies customer)
  - Employee assignment (notifies employee and customer)
  - Appointment cancellation (notifies admins and assigned employee)

### 4. Controller Layer

#### `NotificationController.java`
- REST API endpoints for notification management:
  - `GET /api/notifications` - Get all notifications
  - `GET /api/notifications/unread` - Get unread notifications
  - `GET /api/notifications/unread/count` - Get unread notification count
  - `PUT /api/notifications/{id}/read` - Mark notification as read
  - `PUT /api/notifications/read-all` - Mark all as read
  - `DELETE /api/notifications/{id}` - Delete a notification
- Location: `src/main/java/com/example/demo/controller/NotificationController.java`

#### Updated `EmployeeAppointmentController.java`
- Integrated notification sending when employee updates appointment status
- Automatically notifies customers and admins about status changes

### 5. DTO Layer

#### `NotificationDTO.java`
- Data transfer object for notification data
- Location: `src/main/java/com/example/demo/dto/NotificationDTO.java`

#### `WebSocketNotificationMessage.java`
- Message format for WebSocket notifications
- Location: `src/main/java/com/example/demo/dto/WebSocketNotificationMessage.java`

### 6. Configuration

#### Updated `WebSocketConfig.java`
- Added new WebSocket endpoint: `/ws/notifications`
- Clients connect to this endpoint for real-time notifications

## WebSocket Topics

### User-Specific Topics
- **Customer**: `/topic/notifications/user.{customerId}`
- **Employee**: `/topic/notifications/user.{employeeId}`
- **Admin**: `/topic/notifications/user.{adminId}` or `/topic/notifications/admin`

## Notification Workflow

### Customer Journey
1. **Creates Appointment**
   - Customer receives: "Appointment Created" notification
   - Admins receive: "New Appointment" notification

2. **Admin Confirms Appointment**
   - Customer receives: "Appointment Confirmed" notification

3. **Admin Assigns Employee**
   - Employee receives: "New Appointment Assigned" notification
   - Customer receives: "Employee Assigned" notification

4. **Employee Updates Status**
   - Customer receives status update notifications (IN_SERVICE, READY, COMPLETED)
   - Admins receive same notifications

5. **Customer Cancels**
   - Admins receive: "Appointment Cancelled by Customer" notification
   - Assigned employee receives: "Appointment Cancelled" notification

### Employee Journey
1. **Gets Assigned**
   - Receives: "New Appointment Assigned" notification with vehicle details

2. **Updates Status**
   - Customer gets notified of each status change
   - Admins get notified of each status change

### Admin Journey
1. **New Appointment**
   - Receives: "New Appointment" notification

2. **Status Changes**
   - Receives notifications for all appointment updates

3. **Customer Cancellations**
   - Receives: "Appointment Cancelled by Customer" notification

## Frontend Integration

### 1. Connect to WebSocket

```javascript
import SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8080/ws/notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // Subscribe to user-specific topic
    stompClient.subscribe(`/topic/notifications/user.${userId}`, (message) => {
        const notification = JSON.parse(message.body);
        // Handle notification (show toast, update UI, play sound, etc.)
        showNotification(notification);
    });
});
```

### 2. Fetch Notifications via REST API

```javascript
// Get all notifications
fetch('/api/notifications', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
})
.then(response => response.json())
.then(data => {
    // Display notifications in UI
});

// Get unread count
fetch('/api/notifications/unread/count', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
})
.then(response => response.json())
.then(data => {
    // Update notification badge
    updateNotificationBadge(data.data);
});

// Mark as read
fetch(`/api/notifications/${notificationId}/read`, {
    method: 'PUT',
    headers: {
        'Authorization': `Bearer ${token}`
    }
});
```

### 3. Example React Component

```javascript
import React, {useEffect, useState} from 'react';
import {toast} from 'react-toastify';

function NotificationHandler({userId, token}) {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        // Connect to WebSocket
        const socket = new SockJS('http://localhost:8080/ws/notifications');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            stompClient.subscribe(`/topic/notifications/user.${userId}`, (message) => {
                const notification = JSON.parse(message.body);
                
                // Show toast notification
                toast.info(notification.message, {
                    position: "top-right",
                    autoClose: 5000
                });

                // Update state
                setNotifications(prev => [notification, ...prev]);
                setUnreadCount(prev => prev + 1);
            });
        });

        // Fetch existing notifications
        fetchNotifications();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, [userId]);

    const fetchNotifications = async () => {
        const response = await fetch('/api/notifications', {
            headers: {'Authorization': `Bearer ${token}`}
        });
        const data = await response.json();
        setNotifications(data.data);
        
        const countResponse = await fetch('/api/notifications/unread/count', {
            headers: {'Authorization': `Bearer ${token}`}
        });
        const countData = await countResponse.json();
        setUnreadCount(countData.data);
    };

    const markAsRead = async (notificationId) => {
        await fetch(`/api/notifications/${notificationId}/read`, {
            method: 'PUT',
            headers: {'Authorization': `Bearer ${token}`}
        });
        setUnreadCount(prev => Math.max(0, prev - 1));
    };

    return (
        <div>
            <div className="notification-bell">
                <i className="bell-icon"></i>
                {unreadCount > 0 && (
                    <span className="badge">{unreadCount}</span>
                )}
            </div>
            
            <div className="notification-list">
                {notifications.map(notif => (
                    <div 
                        key={notif.id} 
                        className={notif.isRead ? 'read' : 'unread'}
                        onClick={() => markAsRead(notif.id)}
                    >
                        <h4>{notif.title}</h4>
                        <p>{notif.message}</p>
                        <small>{new Date(notif.createdAt).toLocaleString()}</small>
                    </div>
                ))}
            </div>
        </div>
    );
}
```

## Database Migration

Add this to your database schema:

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    recipient_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_recipient (recipient_id),
    INDEX idx_appointment (appointment_id),
    INDEX idx_unread (recipient_id, is_read)
);
```

## Testing

### 1. Test WebSocket Connection

```javascript
const socket = new SockJS('http://localhost:8080/ws/notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    console.log('Connected to WebSocket');
    stompClient.subscribe('/topic/notifications/user.1', (message) => {
        console.log('Received:', JSON.parse(message.body));
    });
});
```

### 2. Test Notification Flow

1. Create an appointment as a customer
2. Check if customer and admins receive notifications
3. Assign an employee as admin
4. Check if employee receives assignment notification
5. Update status as employee
6. Check if customer receives status update

### 3. Test REST API

```bash
# Get all notifications
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/notifications

# Get unread count
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/notifications/unread/count

# Mark as read
curl -X PUT -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/notifications/1/read
```

## Best Practices

1. **Connection Management**: Close WebSocket connections when components unmount
2. **Error Handling**: Implement reconnection logic for WebSocket disconnections
3. **Performance**: Use pagination for notification lists
4. **User Experience**: Show toast notifications for real-time updates
5. **Cleanup**: Periodically delete old read notifications (optional)

## Troubleshooting

### WebSocket Connection Issues
- Check CORS configuration in `WebSocketConfig.java`
- Verify endpoint URL: `/ws/notifications`
- Check if SockJS fallback is working

### Notifications Not Received
- Verify user is subscribed to correct topic
- Check if notification was saved to database
- Verify `NotificationService` is being called

### Build Errors
- Ensure Lombok is properly configured
- Check if all dependencies are in `pom.xml`
- Run `mvn clean compile`

## Next Steps

1. Add email notifications for important updates
2. Add SMS notifications for critical status changes
3. Implement notification preferences for users
4. Add push notifications for mobile apps
5. Implement notification history archival

## Support

For issues or questions, refer to:
- Spring Boot WebSocket documentation
- STOMP protocol documentation
- Your team's internal documentation

---
*Last Updated: November 7, 2025*


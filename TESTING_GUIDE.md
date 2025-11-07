# How to Test the Notification System Fix

## Step 1: Verify the Configuration Change
The `application.properties` file has been updated with:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

This will drop and recreate all tables on startup, fixing the migration issue.

## Step 2: Start the Application

### Using Maven Wrapper:
```cmd
mvnw.cmd spring-boot:run
```

### Or using your IDE:
Run the `AsmsBackendApplication` main class

## Step 3: Check for Successful Startup

Look for these log messages:
```
✓ Hibernate: drop table if exists notifications cascade
✓ Hibernate: create table notifications (...)
✓ Started AsmsBackendApplication in X.XXX seconds
```

If you see these messages without errors, the fix worked!

## Step 4: Test the Notification System

### Test 1: Create a Customer Account
```http
POST http://localhost:8080/api/auth/customer/signup
Content-Type: application/json

{
  "username": "customer@test.com",
  "password": "password123",
  "email": "customer@test.com",
  "name": "Test Customer",
  "contactNumber": "1234567890",
  "address": "123 Test St"
}
```

### Test 2: Create an Appointment (as Customer)
This should trigger a notification.

### Test 3: Assign Employee to Appointment (as Admin)
This should trigger an `EMPLOYEE_ASSIGNED` notification.

### Test 4: Get Notifications (as User)
```http
GET http://localhost:8080/api/notifications
Authorization: Bearer <your_jwt_token>
```

## Expected Notification Flow

### For Customers:
1. **APPOINTMENT_CREATED** - When they create an appointment
2. **APPOINTMENT_CONFIRMED** - When admin confirms it
3. **EMPLOYEE_ASSIGNED** - When employee is assigned
4. **STATUS_CHANGED_IN_SERVICE** - When service starts
5. **STATUS_CHANGED_READY** - When vehicle is ready
6. **STATUS_CHANGED_COMPLETED** - When service is completed

### For Employees:
1. **EMPLOYEE_ASSIGNED** - When assigned to a new appointment

### For Admins:
1. **APPOINTMENT_CREATED** - When customers create appointments
2. Various status updates for monitoring

## Troubleshooting

### If you still see errors:

1. **Check PostgreSQL is running:**
   ```cmd
   psql -U postgres -d demo
   ```

2. **Verify database connection:**
   Check the connection details in `application.properties`:
   - URL: `jdbc:postgresql://localhost:5432/demo`
   - Username: `postgres`
   - Password: `postgre`

3. **Clear target folder:**
   ```cmd
   mvnw.cmd clean
   ```

4. **Manually drop the database and recreate:**
   ```sql
   DROP DATABASE IF EXISTS demo;
   CREATE DATABASE demo;
   ```

### If you need to preserve data:

1. Stop the application
2. Change back to:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Run the `migration_fix.sql` script in PostgreSQL
4. Restart the application

## What's Fixed

✅ Notification entity with proper columns
✅ NotificationType enum with all status types
✅ Role enum for user roles
✅ Database migration strategy for development
✅ SQL script for production migration

## Next Steps

1. Test all notification scenarios
2. Implement WebSocket for real-time notifications (if needed)
3. Add notification preferences for users
4. Implement notification cleanup/archiving
5. Add email/SMS notification integration (already have email config)

## Production Deployment

⚠️ **IMPORTANT**: Before deploying to production:

1. **Backup your database**
2. **Change ddl-auto to `validate` or `update`**:
   ```properties
   spring.jpa.hibernate.ddl-auto=validate
   ```
3. **Use Flyway or Liquibase** for managed migrations
4. **Test the migration script** on a staging database first

## Summary

The fix applied:
- ✅ Changed `ddl-auto` to `create-drop` (development only)
- ✅ Verified all model classes compile correctly
- ✅ Created migration script for production use
- ✅ Documented the issue and solutions

Your application should now start successfully! 🎉


# Notification System - Database Migration Fix

## Problem
The application failed to start with the error:
```
ERROR: column "type" of relation "notifications" contains null values
ERROR: column "appointment_id" of relation "notifications" contains null values
ERROR: column "recipient_id" of relation "notifications" contains null values
ERROR: column "is_read" of relation "notifications" contains null values
ERROR: column "title" of relation "notifications" contains null values
```

This occurred because:
1. The `notifications` table already existed in the database with old schema
2. Hibernate tried to add new non-nullable columns to the existing table
3. Existing rows had NULL values for these new columns
4. PostgreSQL rejected the ALTER TABLE commands because you can't add NOT NULL columns without default values to a table with existing data

## Solution Applied

### Quick Fix (Development - DROPS ALL DATA)
Changed `spring.jpa.hibernate.ddl-auto` from `update` to `create-drop` in `application.properties`.

**Effect**: The database schema will be dropped and recreated on every application restart. This is suitable for development but will **delete all existing data**.

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

### Alternative Solutions (Production - Preserves Data)

#### Option 1: Manual SQL Migration (Recommended for Production)
Run the SQL script `migration_fix.sql` directly in PostgreSQL before starting the application:

```sql
-- Update existing rows with default values
UPDATE notifications SET title = 'Notification' WHERE title IS NULL;
UPDATE notifications SET type = 'GENERAL' WHERE type IS NULL;
UPDATE notifications SET recipient_id = 0 WHERE recipient_id IS NULL;
UPDATE notifications SET appointment_id = 0 WHERE appointment_id IS NULL;
UPDATE notifications SET is_read = false WHERE is_read IS NULL;

-- Then alter columns to be NOT NULL
ALTER TABLE notifications ALTER COLUMN title SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN type SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN recipient_id SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN appointment_id SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN is_read SET NOT NULL;
```

After running this script, change back to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

#### Option 2: Drop Table Manually
If you don't need existing notification data:

```sql
DROP TABLE IF EXISTS notifications CASCADE;
```

Then use `ddl-auto=update` and let Hibernate recreate the table.

#### Option 3: Use Flyway or Liquibase
For production applications, consider using a proper database migration tool like Flyway or Liquibase to manage schema changes safely.

## Notification System Features

The notification system now supports:

### Notification Types
- `APPOINTMENT_CREATED` - When customer creates an appointment
- `APPOINTMENT_CONFIRMED` - When admin confirms appointment
- `APPOINTMENT_CANCELLED` - When appointment is cancelled
- `EMPLOYEE_ASSIGNED` - When employee is assigned to appointment
- `STATUS_CHANGED_IN_SERVICE` - When status changes to IN_SERVICE
- `STATUS_CHANGED_READY` - When status changes to READY
- `STATUS_CHANGED_COMPLETED` - When status changes to COMPLETED
- `GENERAL` - General notification

### Notification Fields
- `id` - Unique identifier
- `title` - Notification title (required)
- `message` - Notification message (required, max 500 chars)
- `type` - Notification type (required, enum)
- `recipientId` - User ID who will receive the notification (required)
- `appointmentId` - Related appointment ID (required)
- `isRead` - Read status (default: false)
- `createdAt` - Timestamp when notification was created
- `readAt` - Timestamp when notification was read (nullable)

## Next Steps for Production

1. **Test the application** with the current `create-drop` setting to ensure everything works
2. **Export any important data** before switching to production
3. **Choose a migration strategy**:
   - For development: Keep `create-drop`
   - For production: Use `update` with proper migration scripts
   - For enterprise: Implement Flyway or Liquibase

4. **Change ddl-auto back to `update`** for production:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

## Important Warning
⚠️ **NEVER use `create-drop` in production!** It will delete all data on every restart.


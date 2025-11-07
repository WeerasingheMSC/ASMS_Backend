# 🚨 IMMEDIATE FIX: Clear PostgreSQL Connections

## Problem
Your application can't start because PostgreSQL has too many existing connections.

**Error**: `FATAL: sorry, too many clients already`

---

## ✅ SOLUTION: Clear Connections Using pgAdmin

### Step-by-Step Instructions:

#### 1. Open pgAdmin
- Look for pgAdmin in your Start Menu
- Or open from: `C:\Program Files\PostgreSQL\XX\pgAdmin 4\bin\pgAdmin4.exe`

#### 2. Connect to PostgreSQL
- Expand "Servers" in the left panel
- Click on "PostgreSQL XX"
- Enter password: `postgre`

#### 3. Open Query Tool
- Right-click on "PostgreSQL XX" server
- Select "Query Tool" (or press Alt+Shift+Q)

#### 4. Run This SQL Command

Copy and paste this into the Query Tool:

```sql
-- Terminate all connections to 'demo' database
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'demo'
  AND pid <> pg_backend_pid();
```

#### 5. Click Execute (▶️ button or press F5)

You should see: "Query returned successfully"

#### 6. Verify Connections Cleared

Run this to check:

```sql
SELECT COUNT(*) as active_connections
FROM pg_stat_activity
WHERE datname = 'demo';
```

**Expected**: Should show 0 or 1 connections

---

## Alternative Method: PowerShell Script

Run the PowerShell script I created:

```powershell
# Right-click and select "Run with PowerShell"
clear-connections.ps1
```

---

## Alternative Method: Restart PostgreSQL Service

### Option A: Services Manager

1. Press `Windows + R`
2. Type: `services.msc`
3. Press Enter
4. Find "postgresql-x64-XX" (where XX is version number)
5. Right-click → Restart

### Option B: Command Line (Run as Administrator)

```cmd
net stop postgresql-x64-16
net start postgresql-x64-16
```

*Note: Change "16" to your PostgreSQL version number*

---

## 🚀 Start Your Application

After clearing connections:

```cmd
cd C:\Users\M S I\Desktop\ASMS_Backend
mvnw.cmd spring-boot:run
```

---

## ✅ Expected Output

When application starts successfully:

```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: drop table if exists notifications cascade
Hibernate: drop table if exists users cascade
Hibernate: create table notifications (...)
Hibernate: create table users (...)
Admin user initialized successfully
Started AsmsBackendApplication in X.XXX seconds (JVM running for X.XXX)
```

---

## 🔍 Quick Checklist

Before starting application:

- [ ] PostgreSQL is running
- [ ] Connections to 'demo' database cleared
- [ ] No Java processes running (`tasklist | findstr java`)
- [ ] HikariCP configuration is in application.properties

---

## 📊 Why This Happens

1. Application crashes or stops improperly
2. Connections not closed properly
3. Multiple application restarts
4. Connection pool not configured (now fixed with HikariCP)

**With HikariCP configured**: This won't happen again! Maximum 5 connections.

---

## 🆘 Still Not Working?

### Last Resort: Drop and Recreate Database

**⚠️ WARNING: This will delete all data!**

In pgAdmin Query Tool:

```sql
-- Connect to 'postgres' database first (top dropdown)
-- Then run:

-- Terminate connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'demo';

-- Drop database
DROP DATABASE IF EXISTS demo;

-- Create fresh database
CREATE DATABASE demo;
```

Then start your application - tables will be auto-created.

---

## 📞 Need More Help?

Refer to these files:
- `POSTGRESQL_CONNECTION_ISSUE_RESOLVED.md` - Full explanation
- `FIX_POSTGRESQL_TOO_MANY_CLIENTS.md` - Detailed troubleshooting
- `clear_connections.sql` - SQL commands
- `clear-connections.ps1` - PowerShell script

---

## 🎯 Quick Summary

1. ✅ **Open pgAdmin**
2. ✅ **Run SQL**: `SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'demo' AND pid <> pg_backend_pid();`
3. ✅ **Start app**: `mvnw.cmd spring-boot:run`

**That's it! Your application will now start successfully! 🎉**


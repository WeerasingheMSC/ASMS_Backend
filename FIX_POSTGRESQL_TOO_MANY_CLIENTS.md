# Fix: PostgreSQL "Too Many Clients" Error

## Problem
PostgreSQL error: `FATAL: sorry, too many clients already`

This occurs when:
1. Connection pool is not properly configured
2. Old connections are not being closed
3. Multiple application restarts without closing connections
4. Default PostgreSQL max_connections limit reached (usually 100)

## Solution Applied

### 1. Added HikariCP Connection Pool Configuration

**File**: `application.properties`

```properties
# HikariCP Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=5         # Max 5 connections
spring.datasource.hikari.minimum-idle=2              # Keep 2 idle connections
spring.datasource.hikari.connection-timeout=20000    # 20 seconds timeout
spring.datasource.hikari.idle-timeout=300000         # 5 minutes idle timeout
spring.datasource.hikari.max-lifetime=1200000        # 20 minutes max lifetime
spring.datasource.hikari.leak-detection-threshold=60000  # 60 seconds leak detection
```

**Explanation**:
- `maximum-pool-size=5`: Limits your app to use only 5 database connections
- `minimum-idle=2`: Keeps 2 connections ready for quick use
- `connection-timeout=20000`: Waits max 20 seconds to get a connection
- `idle-timeout=300000`: Closes idle connections after 5 minutes
- `max-lifetime=1200000`: Recycles connections every 20 minutes
- `leak-detection-threshold=60000`: Warns if connection held for >60 seconds

## Immediate Fix: Clear Existing Connections

### Option 1: Restart PostgreSQL Service (Windows)

```cmd
# Open Command Prompt as Administrator

# Stop PostgreSQL
net stop postgresql-x64-16

# Start PostgreSQL
net start postgresql-x64-16
```

### Option 2: Kill Active Connections via SQL

1. **Open pgAdmin or psql**:
```bash
psql -U postgres -d demo
```

2. **Check active connections**:
```sql
SELECT 
    pid, 
    usename, 
    application_name, 
    client_addr, 
    state,
    query_start
FROM pg_stat_activity 
WHERE datname = 'demo'
ORDER BY query_start;
```

3. **Terminate idle connections**:
```sql
-- Terminate all connections to 'demo' database except your current one
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'demo'
  AND pid <> pg_backend_pid();
```

4. **Verify connections cleared**:
```sql
SELECT COUNT(*) as connection_count
FROM pg_stat_activity
WHERE datname = 'demo';
```

### Option 3: Drop and Recreate Database (Dev Only)

```sql
-- Connect to postgres database first
\c postgres

-- Terminate all connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'demo';

-- Drop and recreate
DROP DATABASE IF EXISTS demo;
CREATE DATABASE demo;
```

## Verify Configuration

After restarting your application, check connection usage:

```sql
-- Check current connections
SELECT 
    COUNT(*) as total_connections,
    application_name
FROM pg_stat_activity
WHERE datname = 'demo'
GROUP BY application_name;
```

**Expected Result**: You should see max 5 connections from your Spring Boot app.

## PostgreSQL Configuration (Optional)

If you need more connections globally, edit `postgresql.conf`:

### Windows Location:
```
C:\Program Files\PostgreSQL\16\data\postgresql.conf
```

### Change max_connections:
```conf
# Default is usually 100
max_connections = 200
```

**Note**: Requires PostgreSQL restart to take effect.

## Application Restart Process

1. **Stop your Spring Boot application** (Ctrl+C)

2. **Clear PostgreSQL connections** (use one of the options above)

3. **Start your application**:
```bash
mvnw.cmd spring-boot:run
```

4. **Monitor startup logs** - should see:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

## Prevention Tips

### 1. Always Close Connections in Code
```java
// Spring Boot handles this automatically with @Transactional
// But if using raw JDBC:
try (Connection conn = dataSource.getConnection()) {
    // Use connection
} // Auto-closes
```

### 2. Use @Transactional Properly
```java
@Transactional
public void myMethod() {
    // Connection automatically managed
}
```

### 3. Don't Create Multiple DataSources
```java
// ❌ BAD
DataSource ds = new DataSource();

// ✅ GOOD
@Autowired
private DataSource dataSource;
```

### 4. Monitor Connection Pool
Add Spring Boot Actuator to monitor connections:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Access metrics at: `http://localhost:8080/actuator/metrics/hikaricp.connections`

## Troubleshooting

### Issue: Still getting "too many clients"

**Check 1**: Application not fully stopped
```bash
# Windows - Find Java processes
tasklist | findstr java

# Kill if needed
taskkill /F /PID <process_id>
```

**Check 2**: Multiple applications connecting
```sql
SELECT application_name, COUNT(*)
FROM pg_stat_activity
WHERE datname = 'demo'
GROUP BY application_name;
```

**Check 3**: PostgreSQL max_connections too low
```sql
SHOW max_connections;
```

### Issue: Application slow to start

**Cause**: Connection pool waiting for available connections

**Solution**: Increase `connection-timeout` or reduce `maximum-pool-size`

```properties
spring.datasource.hikari.connection-timeout=30000  # Increase to 30 seconds
```

## Summary of Changes

✅ **Added HikariCP configuration** - Limits connection pool to 5  
✅ **Added connection timeout** - Prevents hanging connections  
✅ **Added idle timeout** - Closes unused connections  
✅ **Added leak detection** - Warns about connection leaks  
✅ **Added max lifetime** - Recycles old connections  

## Next Steps

1. ✅ **Configuration updated** - HikariCP settings added
2. ⚠️ **Clear existing connections** - Use one of the SQL commands above
3. 🚀 **Restart application** - Start fresh with new configuration
4. ✅ **Verify** - Check that only 5 connections are used

## Quick Fix Commands

```bash
# 1. Stop application (Ctrl+C)

# 2. Clear connections (psql)
psql -U postgres -d demo -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'demo' AND pid <> pg_backend_pid();"

# 3. Start application
mvnw.cmd spring-boot:run
```

---

**Your connection pool is now properly configured! 🎉**

The application will use maximum 5 connections instead of attempting to create unlimited connections.


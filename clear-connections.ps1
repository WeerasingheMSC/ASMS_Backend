# Clear PostgreSQL Connections Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PostgreSQL Connection Cleanup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find PostgreSQL installation
$pgPath = "C:\Program Files\PostgreSQL"
$psqlPath = ""

if (Test-Path $pgPath) {
    $versions = Get-ChildItem $pgPath -Directory | Sort-Object Name -Descending
    if ($versions.Count -gt 0) {
        $psqlPath = Join-Path $versions[0].FullName "bin\psql.exe"
        Write-Host "Found PostgreSQL: $($versions[0].Name)" -ForegroundColor Green
    }
}

if (-not (Test-Path $psqlPath)) {
    Write-Host "PostgreSQL not found in default location!" -ForegroundColor Red
    Write-Host "Please use pgAdmin to run this SQL:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "SELECT pg_terminate_backend(pid)" -ForegroundColor White
    Write-Host "FROM pg_stat_activity" -ForegroundColor White
    Write-Host "WHERE datname = 'demo' AND pid <> pg_backend_pid();" -ForegroundColor White
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit
}

Write-Host "Clearing connections to 'demo' database..." -ForegroundColor Yellow
Write-Host ""

# Set PostgreSQL password (you may need to change this)
$env:PGPASSWORD = "postgre"

# Execute the command
$output = & $psqlPath -U postgres -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'demo' AND pid <> pg_backend_pid();" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Connections cleared successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Checking remaining connections..." -ForegroundColor Yellow
    & $psqlPath -U postgres -d demo -c "SELECT COUNT(*) as active_connections FROM pg_stat_activity WHERE datname = 'demo';" 2>&1
} else {
    Write-Host "✗ Error clearing connections:" -ForegroundColor Red
    Write-Host $output -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleanup Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "You can now start your application:" -ForegroundColor Green
Write-Host "  mvnw.cmd spring-boot:run" -ForegroundColor White
Write-Host ""

# Clear environment variable
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

Read-Host "Press Enter to exit"


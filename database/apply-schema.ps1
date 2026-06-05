# Apply Utility Billing System database schema and test data (Windows)
# Usage: .\apply-schema.ps1 -Password "your_postgres_password"

param(
    [string]$Host = "localhost",
    [string]$User = "postgres",
    [string]$Password = "123",
    [string]$Database = "utility_billing_db",
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
)

$ErrorActionPreference = "Stop"
$env:PGPASSWORD = $Password

if (-not (Test-Path $PsqlPath)) {
    throw "psql not found at $PsqlPath. Update -PsqlPath to your PostgreSQL bin folder."
}

Write-Host "Creating database if it does not exist..."
& $PsqlPath -U $User -h $Host -tc "SELECT 1 FROM pg_database WHERE datname = '$Database'" | Out-Null
$dbExists = & $PsqlPath -U $User -h $Host -tc "SELECT 1 FROM pg_database WHERE datname = '$Database'"
if (-not $dbExists.Trim()) {
    & $PsqlPath -U $User -h $Host -c "CREATE DATABASE $Database"
}

Write-Host "Applying tables, triggers, functions, and procedures..."
Get-Content "$PSScriptRoot\schema.sql" | Select-Object -Skip 6 | & $PsqlPath -U $User -h $Host -d $Database

Write-Host "Applying business-rule migrations (v2, v3, v4)..."
& $PsqlPath -U $User -h $Host -d $Database -f "$PSScriptRoot\migration-v2-business-rules.sql"
& $PsqlPath -U $User -h $Host -d $Database -f "$PSScriptRoot\migration-v3-wasac-business-rules.sql"
& $PsqlPath -U $User -h $Host -d $Database -f "$PSScriptRoot\migration-v4-project-spec.sql"

Write-Host "Loading test data..."
Get-Content "$PSScriptRoot\test-data.sql" | Select-Object -Skip 6 | & $PsqlPath -U $User -h $Host -d $Database

Write-Host "Database setup complete."

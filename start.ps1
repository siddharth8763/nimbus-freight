# Starts MySQL, the Spring Boot API, and the Vite dashboard.
# Run from the nimbus-freight folder:  .\start.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root "backend"
$frontend = Join-Path $root "frontend"

if (-not (Test-Path (Join-Path $backend "mvnw.cmd"))) {
    throw "Expected backend\mvnw.cmd under $root"
}
if (-not (Test-Path (Join-Path $frontend "package.json"))) {
    throw "Expected frontend\package.json under $root"
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $jdk = Get-ChildItem "C:\Program Files\Java","D:\Softwares" -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
        Select-Object -First 1
    if ($jdk) {
        $env:JAVA_HOME = $jdk.FullName
    }
}
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    throw "JAVA_HOME is not set to a JDK. Set it, then run .\start.ps1 again."
}

Write-Host "Starting MySQL (Docker)..."
docker compose -f (Join-Path $backend "docker-compose.yml") up -d --wait
if ($LASTEXITCODE -ne 0) {
    throw "MySQL did not become healthy. Check that Docker Desktop is running."
}

if (-not (Test-Path (Join-Path $frontend "node_modules"))) {
    Write-Host "Installing frontend dependencies..."
    Push-Location $frontend
    npm install
    if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
    Pop-Location
}

$backendCommand = "`$env:JAVA_HOME = '$($env:JAVA_HOME)'; Set-Location '$backend'; .\mvnw.cmd spring-boot:run"
$frontendCommand = "Set-Location '$frontend'; npm run dev"

Start-Process -FilePath "powershell.exe" -WorkingDirectory $backend -ArgumentList @(
    "-NoExit", "-NoProfile", "-Command", $backendCommand
)
Start-Process -FilePath "powershell.exe" -WorkingDirectory $frontend -ArgumentList @(
    "-NoExit", "-NoProfile", "-Command", $frontendCommand
)

Write-Host ""
Write-Host "MySQL     localhost:3306   (database nimbus, user nimbus)"
Write-Host "Backend   http://localhost:8080"
Write-Host "Frontend  http://localhost:5173"
Write-Host ""
Write-Host "Two windows were opened for the API and the dashboard. When the UI loads, choose Replay seed data."

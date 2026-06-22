# PowerShell Script to download dependencies for the Weather Forecast Application

$LibDir = Join-Path $PSScriptRoot "lib"
$JarPath = Join-Path $LibDir "flatlaf.jar"
$DownloadUrl = "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5/flatlaf-3.5.jar"

if (-not (Test-Path $LibDir)) {
    Write-Host "Creating lib directory..."
    New-Item -ItemType Directory -Force -Path $LibDir | Out-Null
}

if (-not (Test-Path $JarPath)) {
    Write-Host "Downloading FlatLaf library from $DownloadUrl..."
    try {
        Invoke-WebRequest -Uri $DownloadUrl -OutFile $JarPath -ErrorAction Stop
        Write-Host "FlatLaf downloaded successfully!" -ForegroundColor Green
    } catch {
        Write-Error "Failed to download FlatLaf JAR: $_"
    }
} else {
    Write-Host "FlatLaf JAR is already present." -ForegroundColor Green
}

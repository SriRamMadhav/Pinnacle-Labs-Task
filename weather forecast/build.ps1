# PowerShell Script to compile and run the Weather Forecast Application

$BinDir = Join-Path $PSScriptRoot "bin"
$LibDir = Join-Path $PSScriptRoot "lib"
$FlatLafJar = Join-Path $LibDir "flatlaf.jar"

# 1. Download dependency if missing
& (Join-Path $PSScriptRoot "download_dependencies.ps1")

if (-not (Test-Path $BinDir)) {
    Write-Host "Creating bin directory..."
    New-Item -ItemType Directory -Force -Path $BinDir | Out-Null
}

# 2. Get list of all Java source files
Write-Host "Locating source files..."
$JavaFiles = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

if ($JavaFiles.Count -eq 0) {
    Write-Error "No Java source files found in src/ folder!"
    exit 1
}

Write-Host "Compiling $($JavaFiles.Count) Java source files..." -ForegroundColor Cyan
$CompileCommand = "javac -cp `"$FlatLafJar`" -d `"$BinDir`" " + ($JavaFiles | ForEach-Object { "`"$_`"" } | Out-String)

# Compile using external call
try {
    javac -cp "$FlatLafJar" -d "$BinDir" $JavaFiles
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation successful!" -ForegroundColor Green
    } else {
        Write-Error "Compilation failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }
} catch {
    Write-Error "Error during compilation: $_"
    exit 1
}

# 3. Run the application
Write-Host "Starting Skyline Weather Forecast Application..." -ForegroundColor Green
java -cp "$BinDir;$FlatLafJar" com.weather.Main

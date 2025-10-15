# Script de configuracion para PracticaDeCampo08
# Ejecutar como: .\setup.ps1

Write-Host "Configurando proyecto PracticaDeCampo08..." -ForegroundColor Green

# Verificar Java
Write-Host "`nVerificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_ -replace '.*"([^"]*)".*', '$1' }
    Write-Host "Java encontrado: $javaVersion" -ForegroundColor Green
    
    # Extraer numero de version mayor
    $majorVersion = [int]($javaVersion -split '\\.')[0]
    if ($majorVersion -lt 21) {
        Write-Host "Advertencia: Se recomienda Java 21 o superior. Version actual: $majorVersion" -ForegroundColor Yellow
        Write-Host "El proyecto puede necesitar ajustes en pom.xml" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Java no encontrado o no esta en el PATH" -ForegroundColor Red
    Write-Host "Por favor instala JDK 21 o superior" -ForegroundColor Red
    return
}

# Buscar JAVA_HOME automaticamente
Write-Host "`nConfigurando JAVA_HOME..." -ForegroundColor Yellow
$javaExecutable = Get-Command java -ErrorAction SilentlyContinue
if ($javaExecutable) {
    $javaPath = $javaExecutable.Source
    $javaHome = Split-Path (Split-Path $javaPath -Parent) -Parent
    
    Write-Host "Java encontrado en: $javaPath" -ForegroundColor Cyan
    Write-Host "JAVA_HOME sugerido: $javaHome" -ForegroundColor Cyan
    
    # Configurar JAVA_HOME
    $env:JAVA_HOME = $javaHome
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')
    Write-Host "JAVA_HOME configurado: $javaHome" -ForegroundColor Green
} else {
    Write-Host "No se pudo determinar JAVA_HOME automaticamente" -ForegroundColor Red
}

# Verificar Maven Wrapper
Write-Host "`nVerificando Maven Wrapper..." -ForegroundColor Yellow
if (Test-Path "mvnw.cmd") {
    Write-Host "Maven Wrapper encontrado" -ForegroundColor Green
} else {
    Write-Host "Maven Wrapper no encontrado" -ForegroundColor Red
    Write-Host "El proyecto puede no estar completo" -ForegroundColor Red
}

# Limpiar y compilar
Write-Host "`nCompilando proyecto..." -ForegroundColor Yellow
try {
    & .\mvnw.cmd clean compile
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilacion exitosa" -ForegroundColor Green
    } else {
        Write-Host "Error en compilacion" -ForegroundColor Red
    }
} catch {
    Write-Host "Error ejecutando Maven" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
}

# Resumen
Write-Host "`nResumen de configuracion:" -ForegroundColor Cyan
Write-Host "  Java Version: $javaVersion" -ForegroundColor White
Write-Host "  JAVA_HOME: $env:JAVA_HOME" -ForegroundColor White
$wrapperStatus = if (Test-Path 'mvnw.cmd') { 'Disponible' } else { 'No encontrado' }
Write-Host "  Maven Wrapper: $wrapperStatus" -ForegroundColor White

Write-Host "`nComandos para ejecutar:" -ForegroundColor Green
Write-Host "  .\mvnw.cmd clean javafx:run" -ForegroundColor White
Write-Host "  .\mvnw.cmd clean compile exec:java" -ForegroundColor White

Write-Host "`nLee README.md para mas informacion" -ForegroundColor Yellow
Write-Host "Configuracion completada!" -ForegroundColor Green

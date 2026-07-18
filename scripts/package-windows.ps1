param(
    [ValidateSet("app-image", "exe")]
    [string]$Type = "app-image",
    [string]$AppVersion = "1.0.0",
    [string]$JavaHome
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $JavaHome = $env:JAVA_HOME
}
if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $jdkRoot = Join-Path $HOME ".jdks"
    if (Test-Path $jdkRoot) {
        $JavaHome = Get-ChildItem $jdkRoot -Directory |
            Where-Object { $_.Name -match "^(temurin|jdk|openjdk)-?21" } |
            Sort-Object Name -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
}
if ([string]::IsNullOrWhiteSpace($JavaHome) -or !(Test-Path (Join-Path $JavaHome "bin\jpackage.exe"))) {
    throw "JDK 21 não encontrado. Informe -JavaHome ou configure JAVA_HOME para o Temurin 21."
}

$releaseFile = Join-Path $JavaHome "release"
$javaRelease = if (Test-Path $releaseFile) { Get-Content $releaseFile -Raw } else { "" }
if ($javaRelease -notmatch 'JAVA_VERSION="21(?:\.|\")') {
    throw "O empacotamento exige JDK 21. JDK encontrado em: $JavaHome"
}
$env:JAVA_HOME = $JavaHome

$targetDir = Join-Path $projectRoot "target"
$inputDir = Join-Path $targetDir "installer-input"
$libDir = Join-Path $inputDir "lib"
$distDir = Join-Path $projectRoot "dist"
$iconPath = Join-Path $projectRoot "src\main\resources\images\eh-desktop.ico"

if (!(Test-Path $iconPath)) {
    throw "Ícone do aplicativo não encontrado em: $iconPath"
}

& (Join-Path $projectRoot "mvnw.cmd") --batch-mode clean package `
    dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$libDir"
if ($LASTEXITCODE -ne 0) {
    throw "A compilação Maven falhou."
}

New-Item -ItemType Directory -Force -Path $inputDir, $distDir | Out-Null
$jar = Get-ChildItem $targetDir -Filter "AgendaHe-*.jar" |
    Where-Object { $_.Name -notmatch "sources|javadoc" } |
    Select-Object -First 1
if ($null -eq $jar) {
    throw "JAR principal não encontrado em target."
}
Copy-Item $jar.FullName (Join-Path $inputDir $jar.Name) -Force

$jpackage = Join-Path $JavaHome "bin\jpackage.exe"
$arguments = @(
    "--type", $Type,
    "--name", "Agenda Eliza Hair",
    "--input", $inputDir,
    "--dest", $distDir,
    "--main-jar", $jar.Name,
    "--main-class", "com.helizahair.Main",
    "--app-version", $AppVersion,
    "--icon", $iconPath,
    "--vendor", "Eliza Hair",
    "--description", "Agenda semanal e controle de caixa para salão de beleza"
)

if ($Type -eq "exe") {
    $arguments += @(
        "--win-menu",
        "--win-menu-group", "Agenda Eliza Hair",
        "--win-shortcut",
        "--win-dir-chooser",
        "--win-per-user-install",
        "--win-upgrade-uuid", "7f00d810-5538-4ca3-bbdc-41bdc7b75c6b"
    )
}

& $jpackage @arguments
if ($LASTEXITCODE -ne 0) {
    throw "O jpackage falhou. Para gerar -Type exe, instale o WiX Toolset 3."
}

Write-Host "Aplicação criada em: $distDir" -ForegroundColor Green

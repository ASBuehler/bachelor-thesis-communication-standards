# ==============================================================================
# PowerShell-Skript zur Erstellung von JSON-Payload-Dateien 
# ==============================================================================

# --- Konfiguration ---
$outputDir = "payloads"
$targetSizes = @(100, 1000, 10000, 100000, 1000000, 10000000)

# --- Skript-Logik ---

if (-not (Test-Path -Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir
}

Write-Host "Erstelle Payload-Dateien im Ordner '$outputDir'..."

foreach ($size in $targetSizes) {
    
    $fileName = "payload_{0}B.json" -f $size
    $filePath = Join-Path -Path $outputDir -ChildPath $fileName
    
    Write-Host "Erstelle Datei: $fileName (Zielgrösse: $size Bytes)"
    
    $jsonOverhead = 11
    $dataLength = $size - $jsonOverhead
    
    if ($dataLength -lt 0) {
        $dataLength = 0
    }
    
    $dataString = "A" * $dataLength
    $jsonContent = '{"data":"' + $dataString + '"}'

    
    Set-Content -Path $filePath -Value $jsonContent -Encoding ASCII -NoNewline
    
    


    # Überprüfe die finale Dateigrösse
    $finalSize = (Get-Item $filePath).Length
    Write-Host "  -> Finale Grösse: $finalSize Bytes"
    
    if ($finalSize -ne $size) {
        Write-Warning "WARNUNG: Finale Grösse für $fileName weicht vom Ziel ab! ($finalSize vs $size)"
    }
}

Write-Host "Alle Payload-Dateien wurden erfolgreich erstellt."
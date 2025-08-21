# ==============================================================================
#                      PCAP-Analyse-Skript 
# ==============================================================================

# --- Konfiguration ---
$outputCsv = "analyse_final_results.csv"
$ErrorActionPreference = "Continue"

# IP-Adressen der Test-VMs
$senderIp = "192.168.100.10"
$receiverIp = "192.168.100.20"

# ==============================================================================
#                  # KORRIGIERTE HILFSFUNKTION
# ==============================================================================
function Get-FieldSum {
    param (
        [string]$FilePath,
        [string]$FieldName
    )
    try {
        # Wir filtern immer nach der Sender-IP, genau wie bei der Paket-Zählung
        $filter = "ip.src == $senderIp"
        
        # Führe tshark aus, um eine Liste der Werte für das gegebene Feld zu erhalten,
        # aber nur für die Pakete, die dem Filter entsprechen.
        $command = "tshark -r `"$FilePath`" -Y `"$filter`" -T fields -e $FieldName 2>`$null"
        $values = Invoke-Expression $command
        
        # Summiere die zurückgegebenen Werte.
        $sum = ($values | Where-Object { $_ } | Measure-Object -Sum).Sum
        return [long]$sum
    } catch { return 0 }
}
# ==============================================================================

# --- Hauptverarbeitung ---
$header = "Dateiname,Protokoll,Format,QoS,PayloadGroesse,Run," +
          "Paketanzahl_Hinweg,Paketanzahl_Datenpakete,GesamtBytes_Hinweg," +
          "HeaderBytes_ETH,HeaderBytes_IP,HeaderBytes_Transport"
$header | Set-Content -Path $outputCsv

Get-ChildItem -Path . -Recurse -Filter *.pcap | ForEach-Object {
    $file = $_
    Write-Host "Analysiere: $($file.FullName)"

    # Initialisierung
    $protocol="N/A"; $format="json"; $qos="N/A"; $payloadSize="N/A"; $run="N/A"
    $packetCount=0; $packetCountData=0; $totalBytes=0; $ethHeader=0; $ipHeader=0; $transportHeader=0

    try {
        # 1. Flexibles Metadaten-Parsing (DEIN CODE, UNVERÄNDERT)
        $fileName = $file.BaseName
        $parts = $fileName.Split('_')
        
        $protocol = $parts[1]
        
        if ($protocol -eq "mqtt") {
            $format = $parts[2]
            $payloadSize = $parts[3] -replace 'B',''
            $qos = $parts[4] -replace 'qos',''
            $run = $parts[5]
        } else {
            $format = $parts[2]
            $payloadSize = $parts[3] -replace 'B',''
            $run = $parts[4]
            $qos = "N/A"
        }

        # 2. HINWEG-PAKET-ZÄHLUNG (DEIN CODE, UNVERÄNDERT)
        $countFilter = "ip.src == $senderIp"
        $packetCount = (tshark -r $file.FullName -Y $countFilter 2>$null).Count
        if (-not $packetCount) {
            $packetCount = 0
        }

        # Zählung Pakete mit Daten-Segmente
        $countFilterData = ""
        if ($protocol -in @('http', 'mqtt', 'tcp')) {
            $countFilterData = "ip.src == $senderIp and tcp.len > 0"
        } elseif ($protocol -in @('coap', 'udp')) {
            $countFilterData = "ip.src == $senderIp and udp.length > 8"
        }
        
        if ($countFilterData) {
            $packetCountData = (tshark -r $file.FullName -Y $countFilterData 2>$null).Count
            if (-not $packetCountData) { $packetCountData = 0 }
        }

        if ($protocol -in @('http')) {
            $packetCountData = $packetCountData-2
        } elseif ($protocol -in @('coap')) {
            $packetCountData = $packetCountData-1
        }

        # 3. Summiere die Bytes und Header-Größen NUR vom Sender zum Empfänger
        # Die Aufrufe sind jetzt einfacher, da die Funktion den Filter intern anwendet.
        $totalBytes = Get-FieldSum -FilePath $file.FullName -FieldName "frame.len"
        # $ethHeader = Get-FieldSum -FilePath $file.FullName -FieldName "eth.hdr_len"
        $ipHeader = Get-FieldSum -FilePath $file.FullName -FieldName "ip.hdr_len"
        $tcpHeader = Get-FieldSum -FilePath $file.FullName -FieldName "tcp.hdr_len"
        $udpHeader = Get-FieldSum -FilePath $file.FullName -FieldName "udp.hdr_len"
        $transportHeader = $tcpHeader + $udpHeader

        # ==============================================================================
        #                  # NEUE, ROBUSTE BERECHNUNG DES ETH-HEADERS
        # ==============================================================================
        # Wir berechnen die Summe der IP-Paketlängen (ohne Header)
        $ipTotalLength = Get-FieldSum -FilePath $file.FullName -FieldName "ip.len"
        
        # Die Summe der Ethernet-Header ist die Differenz zwischen der Gesamtgröße
        # aller Frames und der Gesamtgröße aller IP-Pakete darin.
        $ethHeader = $totalBytes - $ipTotalLength
        # ==============================================================================
    }
    catch {
        Write-Warning "Fehler bei $($file.Name): $($_.Exception.Message)"
    }
    
    # 4. Schreibe die neue, saubere CSV-Zeile
    $csvLine = "$($file.BaseName),$protocol,$format,$qos,$payloadSize,$run," +
               "$packetCount,$packetCountData,$totalBytes," +
               "$ethHeader,$ipHeader,$transportHeader"
    $csvLine | Add-Content -Path $outputCsv
}

Write-Host "Analyse abgeschlossen. Ergebnisse sind in '$outputCsv' gespeichert."
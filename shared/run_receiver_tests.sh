#!/bin/bash

# --- Konfiguration ---
JAR_FILE="kke-test-framework-1.0-SNAPSHOT.jar"
SHARED_FOLDER="/mnt/hgfs/shared"
SIGNAL_FILE="$SHARED_FOLDER/receiver.ready"

# Überprüfe Argumente
if [ -z "$1" ]; then
  echo "FEHLER: Bitte geben Sie ein Protokoll an."
  exit 1
fi
PROTOCOL=$1
FORMAT="json" # Fest auf JSON gesetzt

PAYLOAD_SIZES=(100 1000 10000 100000 1000000 10000000)
RUNS=("run1" "run2" "run3")
QOS_LEVELS=(0 1 2)

echo ">>> EMPFÄNGER-SKRIPT für Protokoll '$PROTOCOL' und Format '$FORMAT' gestartet <<<"
sudo -v || exit 1

# Alte Signal-Datei vor dem ersten Start löschen
rm -f "$SIGNAL_FILE"

# --- Test-Schleife ---
for size in "${PAYLOAD_SIZES[@]}"; do
  for run in "${RUNS[@]}"; do
    
    if [ "$PROTOCOL" == "mqtt" ]; then
      for qos in "${QOS_LEVELS[@]}"; do
        echo ""
        echo "================================================================="
        echo "--> Bereite EMPFÄNGER vor für: $PROTOCOL (QoS $qos), $FORMAT, $size, $run <--"
        
        killall -q -9 java; sleep 1
        PCAP_FILENAME="kke_${PROTOCOL}_${FORMAT}_${size}B_qos${qos}_${run}.pcap"
        TCPDUMP_LOG="tcpdump.log" # Temporäre Log-Datei

        
        # 1. Starte tcpdump und leite seine Statusmeldungen in eine Log-Datei um
        sudo nice -n -20 tcpdump -i ens33 -B 8192 -w "$SHARED_FOLDER/$PCAP_FILENAME" 2> "$TCPDUMP_LOG" &
        TCPDUMP_PID=$!

        # 2. Warte aktiv, bis tcpdump bestätigt, dass es lauscht
        echo "Warte, bis tcpdump (PID $TCPDUMP_PID) bereit ist..."
        while ! grep -q "listening on ens33" "$TCPDUMP_LOG"; do
            if ! ps -p $TCPDUMP_PID > /dev/null; then
                echo "FEHLER: tcpdump ist unerwartet beendet worden!"
                cat "$TCPDUMP_LOG"; exit 1
            fi
            sleep 0.1
        done
        echo "tcpdump ist bereit."

        # 3. Starte jetzt den Java-Empfänger
        java -jar "$JAR_FILE" receiver $size $FORMAT $PROTOCOL $qos &
        RECEIVER_PID=$!
        
        sleep 2 # Kurze Pause nur für den Server-Start
        
        # 4. Sende das Signal
        touch "$SIGNAL_FILE"
        echo "Signal 'receiver.ready' gesendet. Warte auf Sender..."
        
        # 5. Warten und Aufräumen
        wait $RECEIVER_PID
        echo "Java-Empfänger-Prozess (PID $RECEIVER_PID) beendet."
        
        rm -f "$SIGNAL_FILE"
        sleep 1
        sudo kill -SIGINT $TCPDUMP_PID
        wait $TCPDUMP_PID 2>/dev/null
        rm -f "$TCPDUMP_LOG" # Lösche die Log-Datei
        echo "Empfänger-Durchlauf abgeschlossen."
        
      done
    else
      # Dasselbe für andere Protokolle
      echo ""
      echo "================================================================="
      echo "--> Bereite EMPFÄNGER vor für: $PROTOCOL, $FORMAT, $size, $run <--"

      killall -q -9 java; sleep 1
      PCAP_FILENAME="kke_${PROTOCOL}_${FORMAT}_${size}B_${run}.pcap"
      TCPDUMP_LOG="tcpdump.log"

      # 1. Starte tcpdump und leite seine Statusmeldungen in eine Log-Datei um
      sudo nice -n -20 tcpdump -i ens33 -B 8192 -w "$SHARED_FOLDER/$PCAP_FILENAME" 2> "$TCPDUMP_LOG" &
      TCPDUMP_PID=$!

      # 2. Warte aktiv, bis tcpdump bestätigt, dass es lauscht
      echo "Warte, bis tcpdump (PID $TCPDUMP_PID) bereit ist..."
      while ! grep -q "listening on ens33" "$TCPDUMP_LOG"; do
          if ! ps -p $TCPDUMP_PID > /dev/null; then
              echo "FEHLER: tcpdump ist unerwartet beendet worden!"
              cat "$TCPDUMP_LOG"; exit 1
          fi
          sleep 0.1
      done
      echo "tcpdump ist bereit."

      # 3. Starte jetzt den Java-Empfänger
      java -jar "$JAR_FILE" receiver $size $FORMAT $PROTOCOL $run &
      RECEIVER_PID=$!
      
      sleep 2
      
      # 4. Sende das Signal
      touch "$SIGNAL_FILE"
      echo "Signal 'receiver.ready' gesendet. Warte auf Sender..."

      # 5. Warten und Aufräumen
      wait $RECEIVER_PID
      echo "Java-Empfänger-Prozess (PID $RECEIVER_PID) beendet."

      rm -f "$SIGNAL_FILE"
      sleep 1
      sudo kill -SIGINT $TCPDUMP_PID
      wait $TCPDUMP_PID 2>/dev/null
      rm -f "$TCPDUMP_LOG"

      if [ "$PROTOCOL" == "http" ]; then
          echo "Warte 2 Sekunden, damit der HTTP-Port wieder frei wird..."
          sleep 2
      fi
      
      echo "Empfänger-Durchlauf abgeschlossen."
    fi
  done
done
echo ">>> Alle Empfänger-Durchläufe abgeschlossen! <<<"
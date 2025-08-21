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

echo ">>> SENDER-SKRIPT für Protokoll '$PROTOCOL' und Format '$FORMAT' gestartet <<<"

# --- Test-Schleife ---
for size in "${PAYLOAD_SIZES[@]}"; do
  for run in "${RUNS[@]}"; do
    
    # Prüfe, ob das Protokoll MQTT ist
    if [ "$PROTOCOL" == "mqtt" ]; then
      # JA: Iteriere über alle QoS-Level
      for qos in "${QOS_LEVELS[@]}"; do

        echo ""
        echo "================================================================="
        echo "--> Starte Test: $PROTOCOL (QoS $qos), $FORMAT, $size, $run <--"
        
        # Synchronisation
        echo "Warte, bis das alte 'receiver.ready'-Signal verschwindet..."
        while [ -f "$SIGNAL_FILE" ]; do sleep 0.5; done
        echo "Warte auf neues 'receiver.ready'-Signal..."
        while [ ! -f "$SIGNAL_FILE" ]; do sleep 0.5; done
        echo "Signal empfangen. Starte Sender..."
        
        # Sender starten
        java -jar "$JAR_FILE" sender $size $FORMAT $PROTOCOL $qos
        
        echo "Sender-Durchlauf für $size Bytes (QoS $qos, $run) beendet."
      done
    else
      
      echo ""
      echo "================================================================="
      echo "--> Starte Test: $PROTOCOL, $FORMAT, $size, $run <--"
      
      # Überspringe grosse Payloads für UDP
      if [ "$PROTOCOL" == "udp" ] && [ $size -gt 10000 ]; then
          echo "Überspringe Test: Payload ($size B) ist zu gross für UDP."
          continue
      fi
      
      # Synchronisation
      echo "Warte, bis das alte 'receiver.ready'-Signal verschwindet..."
      while [ -f "$SIGNAL_FILE" ]; do sleep 0.5; done
      echo "Warte auf neues 'receiver.ready'-Signal..."
      while [ ! -f "$SIGNAL_FILE" ]; do sleep 0.5; done
      echo "Signal empfangen. Starte Sender..."
      
      # Sender starten
      java -jar "$JAR_FILE" sender $size $FORMAT $PROTOCOL $run
      
      echo "Sender-Durchlauf für $size Bytes ($run) beendet."
    fi
  done
done

echo ">>> Alle Sender-Durchläufe für Protokoll '$PROTOCOL' und Format '$FORMAT' abgeschlossen! <<<"
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os
import numpy as np

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'diagramme_protokoll_skalierung'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

sns.set_theme(style="whitegrid", font_scale=1.2)

try:
    # Lade NUR noch die eine CSV-Datei mit den Netzwerk-Daten
    df = pd.read_csv('analyse_final_results.csv')
except FileNotFoundError as e:
    print(f"FEHLER: Datei nicht gefunden - {e.filename}")
    exit()

# ==============================================================================
#                 2. Datenbereinigung und aufbereiten
# ==============================================================================
print("Daten geladen. Bereite sie für die Analyse vor...")

# Erstelle eine eindeutige Kennung für MQTT mit QoS
df['Protokoll_Display'] = df['Protokoll']
mqtt_rows = df['Protokoll'] == 'mqtt'
df.loc[mqtt_rows, 'Protokoll_Display'] = 'mqtt_qos' + df.loc[mqtt_rows, 'QoS'].astype(str).str.strip('.0')

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
# Aggregiere die Durchschnittswerte über die Runs
grouping_cols = ['Protokoll_Display', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()

print("Daten aggregiert. Erstelle Skalierungs-Diagramme für jedes Protokoll...")

# ==============================================================================
#       4. DIAGRAMME FÜR JEDES PROTOKOLL IN EINER SCHLEIFE ERSTELLEN
# ==============================================================================
for protokoll_name in df_agg['Protokoll_Display'].unique():
    df_proto = df_agg[df_agg['Protokoll_Display'] == protokoll_name]

    if len(df_proto) < 2:
        continue

    plt.figure(figsize=(16, 9))
    
    # Plot 1: Gesamtübertragung (L2) - Hinweg
    plt.plot(df_proto['PayloadGroesse'], df_proto['GesamtBytes_Hinweg'], 
             marker='o', linestyle='-', color='red', label='Gesamtübertragung (L2)', zorder=3)
    
    # Plot 2: Nominale Nutzlast (aus dem Dateinamen)
    plt.plot(df_proto['PayloadGroesse'], df_proto['PayloadGroesse'], 
             marker='o', linestyle='--', color='blue', label='Nutzlast (Nominal)', zorder=3)

    # Berechne und fülle den Overhead-Bereich
    # Gesamt_Overhead_Hinweg = GesamtBytes_Hinweg - PayloadGroesse
    plt.fill_between(df_proto['PayloadGroesse'], df_proto['PayloadGroesse'], df_proto['GesamtBytes_Hinweg'], 
                     color='salmon', alpha=0.3, label='Netzwerk-Overhead (L2-L7)', zorder=1)
    
    # Füge die Anmerkungen für die Paketanzahl (Hinweg) hinzu
    for i, row in df_proto.iterrows():
        offset = row['GesamtBytes_Hinweg'] * 0.1 
        plt.text(row['PayloadGroesse'], row['GesamtBytes_Hinweg'] + offset, f"{row['Paketanzahl_Hinweg']:.1f} Pkt", 
                 fontsize=11, verticalalignment='bottom', horizontalalignment='center', color='darkred', zorder=4)

    plt.title(f'Skalierung des Datenvolumens für: {protokoll_name.upper()}', fontsize=18, weight='bold')
    plt.xlabel('Nominale Payload-Größe (Bytes)')
    plt.ylabel('Datenvolumen (Bytes)')
    
    plt.xscale('log')
    plt.yscale('log')
    
    plt.grid(True, which="both", ls="--")
    
    unique_payloads = sorted(df_proto['PayloadGroesse'].unique())
    plt.xticks(unique_payloads, labels=[f'{int(p/1000)} KB' if p >= 1000 else f'{int(p)} B' for p in unique_payloads])
    
    plt.legend()
    plt.tight_layout()
    
    safe_filename = protokoll_name.replace('+', '_')
    plt.savefig(os.path.join(output_dir, f'skalierung_{safe_filename}.png'), dpi=300)
    plt.close()

print(f"\n{len(df_agg['Protokoll_Display'].unique())} individuelle Skalierungs-Diagramme wurden im Ordner '{output_dir}' gespeichert.")
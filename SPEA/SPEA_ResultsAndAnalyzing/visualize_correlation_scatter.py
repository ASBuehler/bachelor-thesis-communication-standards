import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'diagramme_korrelation'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

sns.set_theme(style="whitegrid", font_scale=1.2)

try:
    df_net = pd.read_csv('analyse_final_results.csv')
    df_app = pd.read_csv('logausgabe.csv')
except FileNotFoundError as e:
    print(f"FEHLER: Datei nicht gefunden - {e.filename}")
    exit()

# ==============================================================================
#                         2. Daten zusammenführen und aufbereiten
# ==============================================================================
print("Daten geladen. Führe sie zusammen für die Korrelationsanalyse...")

df_net['key'] = df_net['Protokoll'].astype(str) + '_' + df_net['Format'].astype(str) + '_' + \
                df_net['PayloadGroesse'].astype(str) + '_' + df_net['Run'].astype(str)
                
df_app['key'] = df_app['Protokoll'].astype(str) + '_' + df_app['Format'].astype(str) + '_' + \
                df_app['PayloadGroesse'].astype(str) + '_' + df_app['Run'].astype(str)
                
df_merged = pd.merge(
    df_net, 
    df_app.drop(columns=['Protokoll', 'Format', 'PayloadGroesse', 'Run']), 
    on='key', 
    how='inner'
)

if df_merged.empty:
    print("FEHLER: Nach dem Zusammenführen sind keine Daten übrig. Überprüfe die Spaltennamen in den CSV-Dateien.")
    exit()

df_merged['RTT_Anwendung_ms'] = df_merged['RTT_Anwendung_ns'] / 1_000_000
df_merged['Gesamt_Overhead_Hinweg'] = df_merged['GesamtBytes_Hinweg'] - df_merged['PayloadGroesse']
df_merged.loc[df_merged['Gesamt_Overhead_Hinweg'] < 0, 'Gesamt_Overhead_Hinweg'] = 0

df_merged['Protokoll_Display'] = df_merged['Protokoll']
mqtt_rows = df_merged['Protokoll'] == 'mqtt'
df_merged.loc[mqtt_rows, 'Protokoll_Display'] = 'mqtt_qos' + df_merged.loc[mqtt_rows, 'QoS'].astype(int).astype(str)

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
grouping_cols = ['Protokoll_Display', 'PayloadGroesse']
df_agg = df_merged.groupby(grouping_cols).mean(numeric_only=True).reset_index()

# ==============================================================================
#                    4. VISUALISIERUNG: OVERHEAD VS. LATENZ (VEREINFACHT)
# ==============================================================================
print("Erstelle vereinfachten Scatter Plot für Overhead vs. Latenz...")

df_scatter = df_agg[df_agg['PayloadGroesse'] == 10000].copy()

if not df_scatter.empty:
    plt.figure(figsize=(12, 8))
    
    # ==============================================================================
    #               ★★★★★ ZENTRALE ÄNDERUNG: Benutzerdefinierte Farben ★★★★★
    # ==============================================================================
    # Definiere die Farben exakt so, wie sie im Referenzdiagramm sind
    custom_palette = {
        "http":      "#1f77b4",  # Blau
        "coap":      "#ff7f0e",  # Orange
        "mqtt_qos0": "#aadd9a",  # Hellgrün
        "mqtt_qos1": "#2ca02c",  # Mittelgrün
        "mqtt_qos2": "#006400",  # Dunkelgrün
        "tcp":       "#d62728",  # Rot
        "udp":       "#9467bd"   # Lila
    }
    # ==============================================================================

    sns.scatterplot(
        data=df_scatter,
        x='Gesamt_Overhead_Hinweg',
        y='RTT_Anwendung_ms',
        hue='Protokoll_Display',
        s=300,
        alpha=0.9,
        # Verwende die benutzerdefinierte Palette
        palette=custom_palette, 
        hue_order=['http', 'coap', 'mqtt_qos0', 'mqtt_qos1', 'mqtt_qos2', 'tcp', 'udp']
    )
    
    plt.title('Korrelation von Overhead und Latenz bei 10KB Payload', fontsize=18, weight='bold')
    plt.xlabel('Gesamt-Overhead des Hinwegs (Bytes)', fontsize=12)
    plt.ylabel('Anwendungs-Latenz (RTT in ms)', fontsize=12)
    plt.grid(True, which="both", ls="--")
    
    plt.legend(title='Protokoll', bbox_to_anchor=(1.05, 1), loc=2)
    
    plt.tight_layout(rect=[0, 0, 0.85, 1])
    output_filename = os.path.join(output_dir, 'analyse_korrelation_overhead_latenz_10kb.png')
    plt.savefig(output_filename, dpi=300)
    plt.close()

    print(f"\nVereinfachtes Korrelations-Diagramm wurde gespeichert: {output_filename}")
else:
    print("WARNUNG: Keine Daten für 10KB Payload gefunden. Diagramm wurde nicht erstellt.")
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'final_report'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

charts_dir = os.path.join(output_dir, 'diagramme')
tables_dir = os.path.join(output_dir, 'tabellen')
if not os.path.exists(charts_dir): os.makedirs(charts_dir)
if not os.path.exists(tables_dir): os.makedirs(tables_dir)

sns.set_theme(style="whitegrid", font_scale=1.2)

try:
    df = pd.read_csv('analyse_final_results.csv')
except FileNotFoundError:
    print("FEHLER: Die Datei 'analyse_final_results.csv' wurde nicht gefunden.")
    exit()

# ==============================================================================
#                         2. Datenbereinigung & Feature Engineering
# ==============================================================================
numeric_cols = [
    'PayloadGroesse', 'Paketanzahl_Hinweg', 'Paketanzahl_Datenpakete', 'GesamtBytes_Hinweg',
    'HeaderBytes_ETH', 'HeaderBytes_IP', 'HeaderBytes_Transport'
]
for col in numeric_cols:
    df[col] = pd.to_numeric(df[col], errors='coerce')

# Filtere Zeilen, in denen wesentliche Daten fehlen
df.dropna(subset=['Paketanzahl_Hinweg', 'GesamtBytes_Hinweg'], inplace=True)
df = df[df['Paketanzahl_Hinweg'] > 0].copy()

# Berechne die Overhead-Metriken aus den neuen Spalten
df['Gesamt_Overhead_Hinweg'] = df['GesamtBytes_Hinweg'] - df['PayloadGroesse']
df.loc[df['Gesamt_Overhead_Hinweg'] < 0, 'Gesamt_Overhead_Hinweg'] = 0 # Korrektur für eventuelle Messfehler

df['Overhead_Prozentual'] = (df['Gesamt_Overhead_Hinweg'] / df['GesamtBytes_Hinweg']) * 100
df['Overhead_pro_Paket'] = df['Gesamt_Overhead_Hinweg'] / df['Paketanzahl_Hinweg']

# Erstelle eine eindeutige Kennung für MQTT mit QoS
df['Protokoll_Display'] = df['Protokoll']
# Füge den QoS-Level zum Namen für MQTT hinzu, wo es relevant ist
df['QoS'] = df['QoS'].fillna(0)  # Fehlende Werte mit 0 ersetzen
df.loc[df['Protokoll'] == 'mqtt', 'Protokoll_Display'] = 'mqtt_qos' + df['QoS'].astype(int).astype(str)

numeric_cols.extend(['Gesamt_Overhead_Hinweg', 'Overhead_Prozentual', 'Overhead_pro_Paket'])

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
grouping_cols = ['Protokoll_Display', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()
print("Daten erfolgreich geladen und aggregiert. Erstelle finalen Report...")

# ==============================================================================
#       HILFSFUNKTION FÜR KONSISTENTE DIAGRAMME (Angepasst für Protokoll-Vergleich)
# ==============================================================================
def create_comparison_chart(data, y_metric, y_label, title, filename, use_log_scale=False):
    """ Erstellt und speichert ein standardisiertes FacetGrid-Vergleichsdiagramm. """
    
    # Definiere die Farbpalette mit Abstufungen für MQTT
    protocol_palette = {
        "http": "#1f77b4",
        "coap": "#ff7f0e",
        "mqtt_qos0": "#98df8a", # Hellgrün
        "mqtt_qos1": "#2ca02c", # Mittelgrün
        "mqtt_qos2": "#1a5e1a", # Dunkelgrün
        "tcp":  "#d62728",
        "udp":  "#9467bd"
    }
    
    # Sortiere die Protokolle für eine logische Anzeigereihenfolge
    protocol_order = [p for p in ['http', 'coap', 'mqtt_qos0', 'mqtt_qos1', 'mqtt_qos2', 'tcp', 'udp'] if p in data['Protokoll_Display'].unique()]
    
    g = sns.FacetGrid(data, col="PayloadGroesse", col_wrap=3, height=6, aspect=1.2, sharey=False,
                      col_order=sorted(data['PayloadGroesse'].unique()))
    
    # Verwende 'hue' für die Farbe und deaktiviere die automatische Legende von map
    g.map(sns.barplot, "Protokoll_Display", y_metric, "Protokoll_Display",
          order=protocol_order,
          palette=protocol_palette,
          hue_order=protocol_order)
    
    for ax in g.axes.flat:
        if ax.get_xticklabels():
            ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha="right")
        if use_log_scale:
            ax.set_yscale('log')
    
    # Erstelle eine saubere, manuelle Legende
    from matplotlib.patches import Patch
    legend_elements = [Patch(facecolor=protocol_palette[proto], label=proto) for proto in protocol_order]
    g.add_legend(legend_data={key.get_label(): val for key, val in zip(legend_elements, legend_elements)},
                 title="Protokoll")

    g.set_axis_labels("Protokoll", y_label)
    g.set_titles("Nominale Payload: {col_name:.0f} Bytes")
    g.fig.suptitle(title, y=1.03, fontsize=18, weight='bold')
    g.tight_layout(rect=[0, 0, 0.9, 1]) # Platz für Legende schaffen

    plt.savefig(os.path.join(charts_dir, filename), dpi=300)
    plt.close()
    print(f"Diagramm '{filename}' gespeichert.")

# ==============================================================================
#       HILFSFUNKTION FÜR KONSISTENTE TABELLEN
# ==============================================================================
def create_summary_table(data, value_metric, filename):
    """ Erstellt und speichert eine übersichtliche Pivot-Tabelle. """
    table = data.pivot_table(index='Protokoll_Display', columns='PayloadGroesse', values=value_metric)
    table.to_csv(os.path.join(tables_dir, filename), float_format='%.2f')
    print(f"Tabelle '{filename}' gespeichert.")

# ==============================================================================
#       FINALE ANALYSEN ERSTELLEN
# ==============================================================================
# Titel angepasst, da das Format jetzt immer JSON ist
json_title_suffix = " (Format: JSON)"

create_comparison_chart(df_agg, 'GesamtBytes_Hinweg', 'Gesamt übertragene Bytes (Hinweg)', 
                        'Vergleich der Gesamt-Übertragungsgröße' + json_title_suffix, '1_vergleich_gesamtgroesse.png')
create_summary_table(df_agg, 'GesamtBytes_Hinweg', 'tabelle_1_gesamtgroesse.csv')

# Wir verwenden jetzt den Gesamt-Overhead des Hinwegs
create_comparison_chart(df_agg, 'Gesamt_Overhead_Hinweg', 'Gesamt-Overhead in Bytes (log-Skala)',
                        'Vergleich des Gesamt-Overheads (Hinweg)' + json_title_suffix, '2_vergleich_netzwerk_overhead.png', use_log_scale=True)
create_summary_table(df_agg, 'Gesamt_Overhead_Hinweg', 'tabelle_2_netzwerk_overhead.csv')

# Wir vergleichen beide Paketanzahlen
create_comparison_chart(df_agg, 'Paketanzahl_Hinweg', 'Gesamte Paketanzahl (Hinweg, log-Skala)',
                        'Vergleich der gesamten Paketanzahl (Hinweg)' + json_title_suffix, '3a_vergleich_paketanzahl_hinweg.png', use_log_scale=True)
create_summary_table(df_agg, 'Paketanzahl_Hinweg', 'tabelle_3a_paketanzahl_hinweg.csv')

create_comparison_chart(df_agg, 'Paketanzahl_Datenpakete', 'Anzahl der Datenpakete (Hinweg, log-Skala)',
                        'Vergleich der Anzahl reiner Datenpakete (Hinweg)' + json_title_suffix, '3b_vergleich_paketanzahl_daten.png', use_log_scale=True)
create_summary_table(df_agg, 'Paketanzahl_Datenpakete', 'tabelle_3b_paketanzahl_daten.csv')

create_comparison_chart(df_agg, 'Overhead_Prozentual', 'Overhead in % der Gesamtübertragung',
                        'Vergleich des prozentualen Overheads' + json_title_suffix, '4_vergleich_overhead_prozentual.png')
create_summary_table(df_agg, 'Overhead_Prozentual', 'tabelle_4_overhead_prozentual.csv')

create_comparison_chart(df_agg, 'Overhead_pro_Paket', 'Overhead pro Paket in Bytes (log-Skala)',
                        'Vergleich des Overheads pro Paket' + json_title_suffix, '5_vergleich_overhead_pro_paket.png', use_log_scale=True)
create_summary_table(df_agg, 'Overhead_pro_Paket', 'tabelle_5_overhead_pro_paket.csv')

print("\nFinaler Report wurde im Ordner 'final_report' mit Unterordnern für Diagramme und Tabellen erstellt.")
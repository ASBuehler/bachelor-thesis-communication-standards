import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'diagramme_zeit_performance'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

sns.set_theme(style="whitegrid", font_scale=1.2)

try:
    # Lade die CSV-Datei mit den Log-Ausgaben
    df_full = pd.read_csv('logausgabe.csv')
except FileNotFoundError:
    print("FEHLER: Die Datei 'logausgabe.csv' wurde nicht gefunden.")
    exit()

# ==============================================================================
#                         2. Daten filtern und aufbereiten
# ==============================================================================
print("Daten geladen. Filtere auf JSON-Tests und bereite sie auf...")

# Filtere den DataFrame, sodass nur noch Zeilen mit dem Format 'json' übrig bleiben
df = df_full[df_full['Format'] == 'json'].copy()

if df.empty:
    print("FEHLER: Keine Daten für das Format 'json' in der CSV-Datei gefunden.")
    exit()

# Konvertiere Nanosekunden in Millisekunden für bessere Lesbarkeit
df['Serialization_ms'] = df['Serialization_ns'] / 1_000_000
df['Deserialization_ms'] = df['Deserialization_ns'] / 1_000_000
df['RTT_Anwendung_ms'] = df['RTT_Anwendung_ns'] / 1_000_000
df['Verarbeitungszeit_ms'] = df['Serialization_ms'] + df['Deserialization_ms']

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
# Aggregiere über alle Formate, um den Durchschnitt pro Protokoll zu erhalten
grouping_cols = ['Protokoll', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()

# ==============================================================================
#           ANALYSE 1: VERGLEICH DES VERARBEITUNGSAUFWANDS (CPU-ZEIT)
# ==============================================================================
print("Erstelle Diagramm 1: Verarbeitungsaufwand...")

g_processing = sns.FacetGrid(df_agg, col="PayloadGroesse", col_wrap=3, height=6, aspect=1.2, sharey=False,
                             col_order=sorted(df_agg['PayloadGroesse'].unique()))

g_processing.map(sns.barplot, "Protokoll", "Verarbeitungszeit_ms",
                 order=['http', 'coap', 'mqtt', 'tcp', 'udp'],
                 palette="viridis")

# Füge die Werte über die Säulen
def annotate_bars(x, y, **kwargs):
    ax = plt.gca()
    for p in ax.patches:
        height = p.get_height()
        if pd.notna(height):
            ax.annotate(f'{height:.1f} ms', (p.get_x() + p.get_width() / 2., height),
                        ha='center', va='center', xytext=(0, 9), textcoords='offset points',
                        fontsize=10, weight='bold')
g_processing.map_dataframe(annotate_bars, x="Protokoll", y="Verarbeitungszeit_ms")

g_processing.set_axis_labels("Protokoll", "Gesamte Verarbeitungszeit (ms)")
g_processing.set_titles("Nominale Payload: {col_name:.0f} Bytes")
g_processing.fig.suptitle('CPU-Verarbeitungsaufwand für JSON (Serialisierung + Deserialisierung)', y=1.03, fontsize=18, weight='bold')
g_processing.tight_layout()
plt.savefig(os.path.join(output_dir, '1_vergleich_verarbeitungszeit_json.png'), dpi=300)
plt.close()


# ==============================================================================
#              ANALYSE 2: VERGLEICH DER ANWENDUNGS-LATENZ (RTT)
# ==============================================================================
print("Erstelle Diagramm 2: Anwendungs-Latenz...")

plt.figure(figsize=(12, 8))
sns.lineplot(data=df_agg, x='PayloadGroesse', y='RTT_Anwendung_ms', hue='Protokoll',
             hue_order=['http', 'coap', 'mqtt', 'tcp', 'udp'], marker='o', lw=2.5)

plt.xscale('log')
plt.yscale('log')
plt.ylabel('Anwendungs-RTT in Millisekunden (log-Skala)')
plt.xlabel('Nominale Payload-Größe (Bytes)')
plt.title('Anwendungs-Latenz (RTT) der Protokolle mit JSON', fontsize=16, weight='bold')
plt.grid(True, which="both", ls="--")
plt.legend(title='Protokoll')

unique_payloads = sorted(df_agg['PayloadGroesse'].unique())
plt.xticks(unique_payloads, labels=[f'{int(p/1000)}KB' if p >= 1000 else f'{int(p)}B' for p in unique_payloads])

plt.tight_layout()
plt.savefig(os.path.join(output_dir, '2_vergleich_anwendungs_latenz_json.png'), dpi=300)
plt.close()

print(f"\nAlle Zeit-Performance-Analysen wurden im Ordner '{output_dir}' gespeichert.")
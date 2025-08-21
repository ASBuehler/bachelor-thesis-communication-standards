import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'diagramme_header_analyse' 
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

sns.set_theme(style="whitegrid", font_scale=1.2)

csv_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'analyse_final_results.csv'))
try:
    df = pd.read_csv(csv_path)
except FileNotFoundError:
    print(f"FEHLER: Die Datei '{csv_path}' wurde nicht gefunden.")
    exit()

# ==============================================================================
#                         2. Datenbereinigung & Feature Engineering
# ==============================================================================
numeric_cols = ['PayloadGroesse', 'GesamtBytes_Hinweg']
for col in numeric_cols:
    df[col] = pd.to_numeric(df[col], errors='coerce')
df.dropna(subset=numeric_cols, inplace=True)

# Berechne den Overhead basierend auf den verfügbaren Spalten
# Gesamt_Overhead_Hinweg = GesamtBytes_Hinweg - Nutzlast
df['Overhead_Hinweg_Bytes'] = df['GesamtBytes_Hinweg'] - df['PayloadGroesse']
# Korrigiere eventuelle negative Werte (falls Messung < nominaler Payload)
df.loc[df['Overhead_Hinweg_Bytes'] < 0, 'Overhead_Hinweg_Bytes'] = 0


# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
grouping_cols = ['Protokoll', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()

print("Daten geladen und aggregiert. Erstelle Kompositions-Diagramm mit Prozentwerten...")

# ==============================================================================
#     4. VISUALISIERUNG: PAYLOAD VS. OVERHEAD (GESTAPELTES SÄULENDIAGRAMM)
# ==============================================================================
protocol_colors = {
    "http": {"dark": "#1f77b4", "light": "#aec7e8"},
    "coap": {"dark": "#ff7f0e", "light": "#ffbb78"},
    "mqtt": {"dark": "#2ca02c", "light": "#98df8a"},
    "tcp":  {"dark": "#d62728", "light": "#ff9896"},
    "udp":  {"dark": "#9467bd", "light": "#c5b0d5"}
}

g = sns.FacetGrid(df_agg, col="PayloadGroesse", col_wrap=3, height=6, aspect=1.2, sharey=False,
                  col_order=sorted(df_agg['PayloadGroesse'].unique()))

def stacked_barplot_with_annotation(x, **kwargs):
    ax = plt.gca()
    data = kwargs.pop("data")
    protocol_order = [p for p in ['http', 'coap', 'mqtt', 'tcp', 'udp'] if p in data['Protokoll'].unique()]
    
    for i, protocol in enumerate(protocol_order):
        protocol_data = data[data['Protokoll'] == protocol]
        if not protocol_data.empty:
            # Verwende die korrekten Spaltennamen
            payload = protocol_data['PayloadGroesse'].iloc[0]
            overhead = protocol_data['Overhead_Hinweg_Bytes'].iloc[0]
            total_height = payload + overhead
            
            ax.bar(protocol, payload, color=protocol_colors[protocol]['dark'])
            ax.bar(protocol, overhead, bottom=payload, color=protocol_colors[protocol]['light'])

            if payload > 0:
                overhead_percent = (overhead / payload) * 100
                ax.annotate(f'{overhead_percent:.1f}%',
                            (i, total_height),
                            ha='center', va='center',
                            xytext=(0, 9),
                            textcoords='offset points',
                            fontsize=10, weight='bold')

g.map_dataframe(stacked_barplot_with_annotation, x="Protokoll")

g.set_axis_labels("Protokoll", "Gesamt übertragene Bytes (Hinweg)")
g.set_titles("Nominale Payload: {col_name:.0f} Bytes")
g.fig.suptitle('Zusammensetzung der Übertragung (Nutzlast vs. Overhead des Hinwegs)', y=1.03, fontsize=18, weight='bold')

from matplotlib.patches import Patch
legend_elements = []
for proto, colors in protocol_colors.items():
    legend_elements.append(Patch(facecolor=colors['dark'], label=f'Nutzlast ({proto})'))
    legend_elements.append(Patch(facecolor=colors['light'], label=f'Overhead ({proto})'))

g.add_legend(legend_data={key: val for key, val in zip([elem.get_label() for elem in legend_elements], legend_elements)},
             title="Komponente")
g.tight_layout()

output_filename = os.path.join(output_dir, 'analyse_komposition_mit_prozent.png')
plt.savefig(output_filename, dpi=300)
plt.close()

print(f"\nDiagramm zur Komposition mit Prozentwerten wurde gespeichert: {output_filename}")
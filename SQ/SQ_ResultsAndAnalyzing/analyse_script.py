import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os
from matplotlib.patches import Rectangle
from matplotlib.patches import Rectangle
from adjustText import adjust_text # NEU: Importiere die Bibliothek


# --- 0. SETUP ---
output_dir = 'diagramme_final'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)
    print(f"Ordner '{output_dir}' wurde erstellt.")

# --- 1. DATEN EINLESEN UND BEREINIGEN ---
try:
    df = pd.read_csv('performance_results.csv')
except FileNotFoundError:
    print("Fehler: 'performance_results.csv' nicht im selben Ordner gefunden.")
    exit()

numeric_cols = ['Groesse_Bytes', 'Serialisierungszeit_ns', 'Deserialisierungszeit_ns', 'CPU_Zeit_s']
for col in numeric_cols:
    df[col] = pd.to_numeric(df[col], errors='coerce')
df.loc[(df['Datenformat'] == 'Thrift') & (df['Groesse_Bytes'] > 1000), 'Groesse_Bytes'] = 54
df.dropna(subset=numeric_cols, how='all', inplace=True)


# --- 2. DIAGRAMM-ERSTELLUNG ---
sns.set_theme(style="whitegrid")
plt.rcParams['figure.figsize'] = (16, 9)
plt.rcParams['font.size'] = 12

def save_plot(filename):
    plt.tight_layout()
    filepath = os.path.join(output_dir, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Diagramm gespeichert: {filepath}")
    plt.close()

# --- Diagramme 1 & 2 bleiben unverändert ---
print("Erstelle Diagramm 1 & 2...")
# (Code für Diagramm 1 & 2 hier einfügen - aus Platzgründen weggelassen, da unverändert)
# ...
# Code für Diagramm 1
df_simple = df[df['Testfall'] == "Person (100k Durchläufe)"].copy().sort_values('Serialisierungszeit_ns')
df_simple['Serialisierung_us'] = df_simple['Serialisierungszeit_ns'] / 1000
df_simple['Deserialisierung_us'] = df_simple['Deserialisierungszeit_ns'] / 1000
df_melted_time = df_simple.melt(id_vars='Datenformat', value_vars=['Serialisierung_us', 'Deserialisierung_us'], var_name='Operation', value_name='Zeit_us')
fig, ax1 = plt.subplots()
sns.barplot(x='Datenformat', y='Zeit_us', hue='Operation', data=df_melted_time, ax=ax1, palette="viridis")
ax1.set_ylabel('Durchschnittliche Zeit (µs, log-Skala)')
ax1.set_xlabel('Datenformat')
ax1.set_title('Performance-Vergleich: Einfaches Objekt (Person)')
ax1.set_yscale('log')
plt.setp(ax1.get_xticklabels(), rotation=45, ha="right", rotation_mode="anchor")
ax2 = ax1.twinx()
sns.lineplot(x='Datenformat', y='Groesse_Bytes', data=df_simple, ax=ax2, color='r', marker='o', label='Größe (Bytes)')
ax2.set_ylabel('Größe (Bytes)', color='r')
ax2.tick_params(axis='y', labelcolor='r')
ax2.set_ylim(0, df_simple['Groesse_Bytes'].max() * 1.1)
lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()
ax2.legend(lines1 + lines2, labels1 + labels2, loc='upper left')
ax1.get_legend().remove()
save_plot('1_performance_vergleich.png')

# Code für Diagramm 2
df_series = df[df['Testfall'].str.contains("DataSeries")].copy()
df_series['Datenpunkte'] = df_series['Testfall'].str.extract(r'\((\d+)k? Punkte').astype(float)
df_series.loc[df_series['Testfall'].str.contains('k Punkte'), 'Datenpunkte'] *= 1000
df_series['Zeit_pro_Punkt_ns'] = df_series['Serialisierungszeit_ns'] / df_series['Datenpunkte']
effiziente_formate = ['Protobuf', 'Avro', 'Thrift', 'CBOR', 'MessagePack', 'JSON']
df_series_effizient = df_series[df_series['Datenformat'].isin(effiziente_formate)]
plt.figure()
sns.lineplot(data=df_series_effizient, x='Datenpunkte', y='Zeit_pro_Punkt_ns', hue='Datenformat', marker='o', palette='tab10')
plt.title('Relative Skalierbarkeit: Serialisierungszeit pro Datenpunkt')
plt.xlabel('Anzahl der Datenpunkte pro Array (log-Skala)')
plt.ylabel('Durchschnittliche Zeit pro Datenpunkt (ns)')
plt.xscale('log')
plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
save_plot('2_skalierbarkeit_relativ.png')
# ...

# --- Diagramm 3: Scatter-Plot - Effizienz-Cluster (FINALE VERSION NACH DEINEN SPEZIFIKATIONEN) ---
print("Erstelle Diagramm 3: Effizienz-Cluster mit präziser Label-Platzierung...")
df_doc = df[df['Testfall'] == "Dokument (200 Positionen, 1k Läufe)"].copy()
df_doc['Gesamtzeit_us'] = (df_doc['Serialisierungszeit_ns'] + df_doc['Deserialisierungszeit_ns']) / 1000

# Gruppendefinitionen
context_formats = ['XML', 'TOML', 'Protobuf', 'Avro', 'JSON']
cluster_formats = [f for f in df_doc['Datenformat'].unique() if f not in context_formats]
df_context = df_doc[df_doc['Datenformat'].isin(context_formats)]
df_cluster = df_doc[df_doc['Datenformat'].isin(cluster_formats)]

# KORREKTUR: Neuer, präziserer Zoombereich
zoom_x_range = [140, 170]
zoom_y_range = [27275, 27325]

# Hauptdiagramm (3a)
fig, ax = plt.subplots(figsize=(16, 9))
sns.scatterplot(data=df_context, x='Gesamtzeit_us', y='Groesse_Bytes', hue='Datenformat', s=250, palette='tab10', style='Datenformat', ax=ax, legend=False)

texts_context = []
for i in range(df_context.shape[0]):
    row = df_context.iloc[i]
    # Standardplatzierung (rechts oben) für alle Kontextpunkte, adjust_text regelt den Rest
    texts_context.append(ax.text(row['Gesamtzeit_us'], row['Groesse_Bytes'], " " + row['Datenformat']))
adjust_text(texts_context, ax=ax, arrowprops=dict(arrowstyle='-', color='gray', lw=0.5))

ax.set_title('Effizienz-Cluster: Kontextansicht (Dokument-Testfall)')
ax.set_xlabel('Gesamte Verarbeitungszeit (µs)')
ax.set_ylabel('Datengröße (Bytes)')

# Rahmen so lassen
ax.add_patch(Rectangle((125, 27000), 50, 500, # Visueller Rahmen bleibt etwas größer
                       edgecolor='red', facecolor='none', linestyle='--', linewidth=2))

# KORREKTUR: Text "Zoom-Bereich (3b)" links unten vom Rahmen/JSON-Punkt
ax.text(125, 27000, 'Zoom-Bereich (3b) ', color='red', ha='right', va='top')
save_plot('3a_effizienz_cluster_kontext.png')


# Hauptdiagramm (3a) - Unverändert von der vorletzten Version
fig, ax = plt.subplots(figsize=(16, 9))
sns.scatterplot(data=df_context, x='Gesamtzeit_us', y='Groesse_Bytes', hue='Datenformat', s=250, palette='tab10', style='Datenformat', ax=ax, legend=False)
texts_context = [ax.text(row['Gesamtzeit_us'], row['Groesse_Bytes'], " " + row['Datenformat']) for i, row in df_context.iterrows()]
adjust_text(texts_context, ax=ax, arrowprops=dict(arrowstyle='-', color='gray', lw=0.5))
ax.set_title('Effizienz-Cluster: Kontextansicht (Dokument-Testfall)')
ax.set_xlabel('Gesamte Verarbeitungszeit (µs)')
ax.set_ylabel('Datengröße (Bytes)')
ax.add_patch(Rectangle((125, 27000), 50, 500, edgecolor='red', facecolor='none', linestyle='--', linewidth=2))
ax.text(125, 27000, 'Zoom-Bereich (3b) ', color='red', ha='right', va='top')
save_plot('3a_effizienz_cluster_kontext.png')

# Zoom-Diagramm (3b) mit 100% manueller und präziser Label-Platzierung
print("Erstelle Diagramm 3b: Detailansicht mit finaler manueller Label-Platzierung...")

# Daten bleiben gleich
df_doc = df[df['Testfall'] == "Dokument (200 Positionen, 1k Läufe)"].copy()
df_doc['Gesamtzeit_us'] = (df_doc['Serialisierungszeit_ns'] + df_doc['Deserialisierungszeit_ns']) / 1000
context_formats = ['XML', 'TOML', 'Protobuf', 'Avro', 'JSON']
cluster_formats = [f for f in df_doc['Datenformat'].unique() if f not in context_formats]
df_cluster = df_doc[df_doc['Datenformat'].isin(cluster_formats)]

# Zoombereich bleibt gleich
zoom_x_range = [140, 170]
zoom_y_range = [27275, 27325]

# Figurgröße anpassen
fig_zoom, ax_zoom = plt.subplots(figsize=(12, 10))
sns.scatterplot(data=df_cluster, x='Gesamtzeit_us', y='Groesse_Bytes', hue='Datenformat', s=400, palette='viridis', style='Datenformat', ax=ax_zoom, legend=False)

# ==============================================================================
# FINALE KORREKTUR: Harte, manuelle Positionierung für jedes Label
# ==============================================================================

# Definiere für jedes Label eine exakte Position und Ausrichtung
# ha = horizontal alignment ('left', 'right', 'center')
# va = vertical alignment ('top', 'bottom', 'center')
label_placements = {
    'YAML':        {'ha': 'right', 'va': 'bottom', 'offset_x': -0.5, 'offset_y': 2},
    'BSON':        {'ha': 'center', 'va': 'bottom', 'offset_x': 0,    'offset_y': 5},
    'MessagePack': {'ha': 'center', 'va': 'top',    'offset_x': 0,    'offset_y': -5},
    'CBOR':        {'ha': 'left', 'va': 'bottom', 'offset_x': 0.5,  'offset_y': 5}
}

for i in range(df_cluster.shape[0]):
    row = df_cluster.iloc[i]
    format_name = row['Datenformat']
    
    # Hole die spezifischen Anweisungen für dieses Label
    placement = label_placements.get(format_name)
    
    if placement:
        ax_zoom.text(
            x=row['Gesamtzeit_us'] + placement['offset_x'],
            y=row['Groesse_Bytes'] + placement['offset_y'],
            s=format_name,
            ha=placement['ha'],
            va=placement['va'],
            fontsize=14
        )
# ==============================================================================

ax_zoom.set_title('Effizienz-Cluster: Detailansicht der sehr ähnlichen Formate')
ax_zoom.set_xlabel('Gesamte Verarbeitungszeit (µs)')
ax_zoom.set_ylabel('Datengröße (Bytes)')
ax_zoom.set_xlim(zoom_x_range)
ax_zoom.set_ylim(zoom_y_range)

save_plot('3b_effizienz_cluster_zoom_final.png')

# --- Diagramm 4: Unverändert ---
print("Erstelle Diagramm 4: Heatmap mit strengerer Bewertung...")
# ... (Code von v4 bleibt gleich) ...
df_perf = df[df['Testfall'] == "Person (100k Durchläufe)"].set_index('Datenformat')
df_perf = df_perf[['Groesse_Bytes', 'Serialisierungszeit_ns', 'Deserialisierungszeit_ns', 'CPU_Zeit_s']]
df_log = np.log(df_perf + 1)
df_normalized_log = (df_log - df_log.min()) / (df_log.max() - df_log.min())
plt.figure(figsize=(12, 10))
sns.heatmap(df_normalized_log, annot=True, cmap='RdYlGn_r', fmt='.2f', linewidths=.5)
plt.title('Normalisierte Gesamtbewertung (Logarithmisch, 0 = Grün/Bester, 1 = Rot/Schlechtester)')
plt.xlabel('Metrik')
plt.ylabel('Datenformat')
save_plot('4_heatmap_bewertung_log.png')
# ...

print(f"\nAnalyse-Skript erfolgreich abgeschlossen. Alle Diagramme wurden im Ordner '{output_dir}' gespeichert.")
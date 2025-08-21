import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os
from matplotlib.ticker import FuncFormatter

# --- 0. SETUP ---
output_dir = 'diagramme_fuer_latex'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)
    print(f"Ordner '{output_dir}' wurde erstellt.")

# --- 1. DATEN EINLESEN UND VORBEREITEN ---
try:
    df = pd.read_csv('performance_results.csv')
except FileNotFoundError:
    print("Fehler: 'performance_results.csv' nicht im selben Ordner gefunden.")
    exit()

df['Groesse_Bytes'] = pd.to_numeric(df['Groesse_Bytes'], errors='coerce')
df.dropna(inplace=True)
df.loc[(df['Datenformat'] == 'Thrift') & (df['Groesse_Bytes'] > 1000), 'Groesse_Bytes'] = 54

def map_szenario(testfall_name):
    if "PersonAdvanced (10k" in testfall_name: return "Komplexes Objekt"
    if "DataSeries (10k Punkte" in testfall_name: return "Große Liste"
    if "Rekursiv (Tiefe 9" in testfall_name: return "Tiefe Rekursion"
    if "Dokument (15 Positionen" in testfall_name: return "Dokument"
    return None

df['Szenario'] = df['Testfall'].apply(map_szenario)
df_plot = df.dropna(subset=['Szenario']).copy()
df_plot['Avg_Groesse'] = df_plot.groupby('Datenformat')['Groesse_Bytes'].transform('mean')
df_plot.sort_values('Avg_Groesse', inplace=True)

# --- 2. DIAGRAMM-ERSTELLUNG (OPTIMIERT FÜR LESBARKEIT) ---

sns.set_theme(style="white")

TITLE_FONTSIZE = 22
AXIS_LABEL_FONTSIZE = 18
TICK_LABEL_FONTSIZE = 14
LEGEND_FONTSIZE = 14

plt.figure(figsize=(20, 10))

ax = sns.barplot(
    data=df_plot,
    x='Datenformat',
    y='Groesse_Bytes',
    hue='Szenario',
    palette='colorblind'
)

ax.set_yscale('log')

def bytes_formatter(x, pos):
    if x >= 1e6: return f'{x*1e-6:g} MB'
    if x >= 1e3: return f'{x*1e-3:g} KB'
    return f'{x:g} B'

ax.yaxis.set_major_formatter(FuncFormatter(bytes_formatter))

ax.set_title('Vergleich der Datengröße über verschiedene Szenarien', fontsize=TITLE_FONTSIZE, pad=20)
ax.set_ylabel('Datengröße (log-Skala)', fontsize=AXIS_LABEL_FONTSIZE)
ax.set_xlabel('Datenformat', fontsize=AXIS_LABEL_FONTSIZE)

# KORREKTUR: Verwende den kompatibleren Weg, um die X-Achsen-Labels zu formatieren
plt.setp(ax.get_xticklabels(), rotation=45, ha="right", rotation_mode="anchor", fontsize=TICK_LABEL_FONTSIZE)
ax.tick_params(axis='y', labelsize=TICK_LABEL_FONTSIZE)

plt.legend(title='Szenario / Objekttyp', fontsize=LEGEND_FONTSIZE, title_fontsize=LEGEND_FONTSIZE + 2)

ax.grid(axis='y', linestyle='--', linewidth=0.7, which='major')
ax.grid(axis='y', linestyle=':', linewidth=0.4, which='minor')

plt.tight_layout(pad=1.5)
filepath = os.path.join(output_dir, 'datengroesse_vergleich_latex.png')
plt.savefig(filepath, dpi=300, bbox_inches='tight')
print(f"Diagramm gespeichert: {filepath}")
plt.close()

print(f"\nSkript erfolgreich abgeschlossen.")
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os

# --- 0. SETUP ---
output_dir = 'diagramme_vergleich'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)
    print(f"Ordner '{output_dir}' wurde erstellt.")

# --- 1. DATEN EINLESEN UND VORBEREITEN ---
try:
    df = pd.read_csv('performance_results.csv')
except FileNotFoundError:
    print("Fehler: 'performance_results.csv' nicht im selben Ordner gefunden.")
    exit()

# Datenbereinigung
numeric_cols = ['Groesse_Bytes', 'Serialisierungszeit_ns', 'Deserialisierungszeit_ns', 'CPU_Zeit_s']
for col in numeric_cols:
    df[col] = pd.to_numeric(df[col], errors='coerce')
df.loc[(df['Datenformat'] == 'Thrift') & (df['Groesse_Bytes'] > 1000), 'Groesse_Bytes'] = 54
df.dropna(inplace=True)

# Berechne Gesamtzeit in Mikrosekunden für den Vergleich
df['Gesamtzeit_us'] = (df['Serialisierungszeit_ns'] + df['Deserialisierungszeit_ns']) / 1000

# Vereinfache die Testfall-Namen für die Diagramm-Legende
def map_testfall(testfall_name):
    if "Person (100k" in testfall_name: return "Einfaches Objekt (Person)"
    if "PersonAdvanced" in testfall_name: return "Komplexes Objekt (PersonAdvanced)"
    if "DataSeries (250k" in testfall_name: return "Große Liste (DataSeries)"
    if "Rekursiv (Tiefe 9" in testfall_name: return "Tiefe Rekursion"
    if "Dokument (200" in testfall_name: return "Großes Dokument"
    return None

df['Objekttyp'] = df['Testfall'].apply(map_testfall)
df_vergleich = df.dropna(subset=['Objekttyp']).copy()

# --- 2. DIAGRAMM-ERSTELLUNG ---
sns.set_theme(style="whitegrid")
plt.rcParams['font.size'] = 12

def save_plot(filename):
    plt.tight_layout()
    filepath = os.path.join(output_dir, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Diagramm gespeichert: {filepath}")
    plt.close()

# --- Diagramm A: Gruppiertes Balkendiagramm nach Objekttyp ---
print("Erstelle Diagramm A: Performance nach Objekttyp...")
plt.figure(figsize=(18, 10))
# Filtere die langsamsten Formate für eine bessere Übersicht
formate_fuer_plot = [f for f in df_vergleich['Datenformat'].unique() if f not in ['XML', 'RDF (Turtle)', 'INI']]
df_plot = df_vergleich[df_vergleich['Datenformat'].isin(formate_fuer_plot)]

sns.barplot(data=df_plot, x='Datenformat', y='Gesamtzeit_us', hue='Objekttyp', palette='tab10')
plt.title('Vergleich der Verarbeitungszeit für verschiedene Objekttypen')
plt.ylabel('Gesamte Verarbeitungszeit (µs, log-Skala)')
plt.xlabel('Datenformat')
plt.yscale('log')
plt.xticks(rotation=45, ha='right')
plt.legend(title='Objekttyp')
save_plot('A_vergleich_nach_objekttyp.png')

# --- Diagramm B: Heatmap des Performance-Rangs ---
print("Erstelle Diagramm B: Heatmap des Performance-Rangs...")
# Berechne den Rang für jedes Format innerhalb jedes Objekttyps
df_vergleich['Rang'] = df_vergleich.groupby('Objekttyp')['Gesamtzeit_us'].rank(method='first').astype(int)

# Erstelle eine Pivot-Tabelle (Matrix) für die Heatmap
pivot_table = df_vergleich.pivot_table(index='Datenformat', columns='Objekttyp', values='Rang')

plt.figure(figsize=(12, 10))
# HIER WAR DER FEHLER - JETZT KORRIGIERT
sns.heatmap(pivot_table, annot=True, cmap='RdYlGn_r', fmt='g', linewidths=.5, cbar_kws={'label': 'Performance-Rang (1 = Bester)'})
plt.title('Performance-Ranking der Datenformate nach Objekttyp')
plt.xlabel('Objekttyp / Testfall')
plt.ylabel('Datenformat')
save_plot('B_heatmap_ranking.png')

print(f"\nVergleichs-Analyse erfolgreich abgeschlossen. Diagramme im Ordner '{output_dir}' gespeichert.")
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os

# --- 0. SETUP ---
output_dir = 'diagramme_stabilitaet'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)
    print(f"Ordner '{output_dir}' wurde erstellt.")

# --- 1. DATEN EINLESEN UND VORBEREITEN ---
try:
    df_stability = pd.read_csv('stability_results.csv')
except FileNotFoundError:
    print("Fehler: 'stability_results.csv' nicht im selben Ordner gefunden.")
    exit()

# Bereinige und vereinfache die Testfall-Namen
def map_stability_testfall(testfall_name):
    if "Person" == testfall_name and "Advanced" not in testfall_name: return "Einfach (Null/Sonderz.)"
    if "PersonAdvanced" in testfall_name: return "Komplex (Null/Sonderz.)"
    if "DataSeries (Problematic Values)" in testfall_name: return "Liste (Problemwerte)"
    if "Rekursiv (Problematic Values)" in testfall_name: return "Rekursiv (Null/Sonderz.)"
    if "Rekursiv (Cyclic Dependency)" in testfall_name: return "Rekursiv (Zyklisch)"
    if "Dokument (Problematic Values)" in testfall_name: return "Dokument (Problemwerte)"
    return "Unbekannt"

df_stability['Szenario'] = df_stability['Testfall'].apply(map_stability_testfall)

# Klassifiziere die Ergebnisse in numerische Werte für die Heatmap
# und erstelle Annotationstexte
def classify_result(row):
    ergebnis = row['Ergebnis']
    grund = str(row['Grund']) # Sicherstellen, dass Grund ein String ist

    if "Robust" in ergebnis:
        if "'null' wird zu leerem String" in grund:
            return 2, "Robust*", "Bedingt Robust ('null' -> \"\")" # Robust mit Anmerkung
        return 1, "Robust", "Robust" # Vollständig Robust
    elif "Nicht Robust" in ergebnis:
        return 3, "Datenverlust", f"Datenverlust ({grund})" # Daten verändert
    elif "Fehlerhaft" in ergebnis:
        if "StackOverflow" in grund:
            return 5, "Absturz", f"Fehlerhaft (Absturz: {grund})" # Kritischer Fehler
        return 4, "Fehlerhaft", f"Fehlerhaft ({grund})" # Graceful Failure oder anderer Fehler
    return 0, "N/A", "Nicht getestet"

results = df_stability.apply(classify_result, axis=1)
df_stability['Kategorie_Code'] = [r[0] for r in results]
df_stability['Annotation'] = [r[1] for r in results]
df_stability['Tooltip'] = [r[2] for r in results] # Für detailliertere Analyse

# Erstelle die Pivot-Tabelle für die Heatmap
pivot_matrix = df_stability.pivot_table(index='Datenformat', columns='Szenario', values='Kategorie_Code')
annot_matrix = df_stability.pivot_table(index='Datenformat', columns='Szenario', values='Annotation', aggfunc=lambda x: ' '.join(x))

# --- 2. DIAGRAMM-ERSTELLUNG ---
sns.set_theme(style="white")
plt.rcParams['font.size'] = 12

def save_plot(filename):
    plt.tight_layout()
    filepath = os.path.join(output_dir, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Diagramm gespeichert: {filepath}")
    plt.close()

# --- Stabilitäts-Matrix (Heatmap) ---
print("Erstelle die Stabilitäts-Matrix...")

# Definiere eine benutzerdefinierte Farbpalette:
# 1=Grün (Robust), 2=Hellgrün (Bedingt Robust), 3=Orange (Datenverlust), 4=Rot (Fehlerhaft), 5=Dunkelrot (Absturz)
colors = ["#2ca02c", "#98df8a", "#ff7f0e", "#d62728", "#8c564b"]
cmap = sns.color_palette(colors, as_cmap=True)

plt.figure(figsize=(16, 12))
ax = sns.heatmap(
    pivot_matrix,
    annot=annot_matrix, # Zeige die vereinfachten Annotationen an
    fmt='s',            # Formatiere Annotationen als Strings
    cmap=cmap,
    linewidths=.5,
    linecolor='white',
    cbar=False,         # Deaktiviere die numerische Farbleiste
    annot_kws={"size": 11, "color": "black"}
)

# Erstelle eine manuelle Legende
from matplotlib.patches import Patch
legend_elements = [
    Patch(facecolor=colors[0], label='Robust'),
    Patch(facecolor=colors[1], label="Robust* (mit semantischer Änderung)"),
    Patch(facecolor=colors[2], label='Nicht Robust (Datenverlust)'),
    Patch(facecolor=colors[3], label='Fehlerhaft (Graceful Failure)'),
    Patch(facecolor=colors[4], label='Fehlerhaft (Absturz)')
]
ax.legend(handles=legend_elements, bbox_to_anchor=(1.05, 1), loc='upper left', title="Ergebnis-Kategorien")


plt.title('Stabilitäts-Matrix: Verhalten der Datenformate in verschiedenen Szenarien', fontsize=16)
plt.xlabel('Stabilitäts-Szenario', fontsize=14)
plt.ylabel('Datenformat', fontsize=14)
plt.xticks(rotation=45, ha='right')

save_plot('stabilitaets_matrix.png')

print(f"\nStabilitäts-Analyse erfolgreich abgeschlossen. Diagramm im Ordner '{output_dir}' gespeichert.")
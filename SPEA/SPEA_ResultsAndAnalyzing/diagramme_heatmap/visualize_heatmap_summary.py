import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'diagramme_heatmap'
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

sns.set_theme(style="white")

try:
    df = pd.read_csv('analyse_final_results.csv')
except FileNotFoundError:
    print("FEHLER: 'analyse_final_results.csv' nicht gefunden.")
    exit()

# ==============================================================================
#                         2. Datenbereinigung & Feature Engineering
# ==============================================================================
print("Daten geladen. Bereite sie für die Heatmap vor...")

df['Protokoll_Display'] = df['Protokoll']
mqtt_rows = df['Protokoll'] == 'mqtt'
df.loc[mqtt_rows, 'Protokoll_Display'] = 'mqtt_qos' + df.loc[mqtt_rows, 'QoS'].astype(str).str.strip('.0')

df['Gesamt_Overhead_Hinweg'] = df['GesamtBytes_Hinweg'] - df['PayloadGroesse']
df.loc[df['Gesamt_Overhead_Hinweg'] < 0, 'Gesamt_Overhead_Hinweg'] = 0

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
grouping_cols = ['Protokoll_Display', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()

# ==============================================================================
#           4. Vorbereitung der Daten für die Heatmap (ANGEPASST)
# ==============================================================================
payloads_for_heatmap = [100, 10000, 10000000] # 100 B, 10 KB, 10 MB
df_heatmap = df_agg[df_agg['PayloadGroesse'].isin(payloads_for_heatmap)].copy()

metrics_for_heatmap = ['GesamtBytes_Hinweg', 'Gesamt_Overhead_Hinweg', 'Paketanzahl_Hinweg']

df_pivot = df_heatmap.pivot_table(
    index='Protokoll_Display', 
    columns='PayloadGroesse', 
    values=metrics_for_heatmap
)


nan_mask = df_pivot.isnull()

# Fülle NaN-Werte für die Rang-Berechnung, aber merke dir, wo sie waren
df_pivot.fillna(df_pivot.max().max(), inplace=True) # Fülle mit einem schlechten Wert

# Rang-basierte Normalisierung
df_ranked = df_pivot.rank(method="min") - 1
df_normalized = df_ranked / df_ranked.max() # Teile durch den max. Rang in der Spalte

# Benenne die Spalten für eine bessere Lesbarkeit um
new_columns = []
for val, size in df_normalized.columns:
    clean_val = val.replace('_Hinweg', '').replace('Bytes', '').replace('pakete', '').replace('anzahl', '').replace('Gesamt_', 'Gesamt').strip()
    size_label = f'{int(size/1000000)}MB' if size >= 1000000 else (f'{int(size/1000)}KB' if size >= 1000 else f'{int(size)}B')
    new_columns.append(f'{clean_val}\n{size_label}')
df_normalized.columns = new_columns

df_normalized = df_normalized.sort_index()

# ==============================================================================
#                          5. Erstelle die Heatmap
# ==============================================================================
plt.figure(figsize=(14, 10))
ax = sns.heatmap(
    df_normalized, 
    cmap='RdYlGn_r',
    annot=True, # Annotationen für die normalisierten Werte
    fmt=".2f",
    linewidths=.5,
    cbar=True,
    mask=nan_mask.values # Wende die Maske an, um NaN-Felder auszugrauen
)

# Zusätzliche Beschriftung für die NaN-Felder
for i in range(nan_mask.shape[0]):
    for j in range(nan_mask.shape[1]):
        if nan_mask.iloc[i, j]:
            ax.text(j + 0.5, i + 0.5, 'N/A',
                    ha='center', va='center', color='grey', fontsize=10)

plt.title('Normalisiertes Effizienz-Ranking (0=Bester, 1=Schlechtester)', fontsize=16, weight='bold')
plt.xlabel('Metrik und Payload-Größe', fontsize=12)
plt.ylabel('Protokoll', fontsize=12)
plt.xticks(rotation=45, ha="right")
plt.yticks(rotation=0)
plt.tight_layout()

output_filename = os.path.join(output_dir, 'analyse_heatmap_ranking_final.png')
plt.savefig(output_filename, dpi=300)
plt.close()

print(f"\nFinale Heatmap wurde erfolgreich gespeichert: {output_filename}")
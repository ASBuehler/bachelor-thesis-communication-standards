import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ==============================================================================
#                      1. Konfiguration & Daten laden
# ==============================================================================
output_dir = 'finale_overhead_analyse'
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
    print("FEHLER: 'analyse_final_results.csv' nicht gefunden.")
    exit()

# ==============================================================================
#                         2. Datenbereinigung & Feature Engineering
# ==============================================================================
print("Daten geladen. Berechne Overhead-Komponenten...")
df['Protokoll_Display'] = df['Protokoll']
mqtt_rows = df['Protokoll'] == 'mqtt'
df.loc[mqtt_rows, 'Protokoll_Display'] = 'mqtt_qos' + df.loc[mqtt_rows, 'QoS'].astype(str).str.strip('.0')
df['Gesamt_Overhead_Hinweg'] = df['GesamtBytes_Hinweg'] - df['PayloadGroesse']
df.loc[df['Gesamt_Overhead_Hinweg'] < 0, 'Gesamt_Overhead_Hinweg'] = 0
df['HeaderBytes_ETH_berechnet'] = df['Paketanzahl_Hinweg'] * 14
df['Overhead_L5_L7'] = df['Gesamt_Overhead_Hinweg'] - df['HeaderBytes_ETH_berechnet'] - df['HeaderBytes_IP'] - df['HeaderBytes_Transport']
df.loc[df['Overhead_L5_L7'] < 0, 'Overhead_L5_L7'] = 0

# ==============================================================================
#                           3. Datenaggregation
# ==============================================================================
grouping_cols = ['Protokoll_Display', 'PayloadGroesse']
df_agg = df.groupby(grouping_cols).mean(numeric_only=True).reset_index()

# ==============================================================================
#                 4. VISUALISIERUNG & TABELLEN ERSTELLEN
# ==============================================================================
stack_order_cols = ['HeaderBytes_ETH_berechnet', 'HeaderBytes_IP', 'HeaderBytes_Transport', 'Overhead_L5_L7']
stack_order_labels = ['ETH (L2)', 'IP (L3)', 'Transport (L4)', 'L5_L7']

# Definiere die Farben für jede Schicht explizit
palette = {"ETH (L2)": "#4c72b0", "IP (L3)": "#dd8452", "Transport (L4)": "#55a868", "L5_L7": "#c44e52"}

for size in sorted(df_agg['PayloadGroesse'].unique()):
    df_plot_base = df_agg[df_agg['PayloadGroesse'] == size].copy()
    if df_plot_base.empty: continue
    
    df_plot_base.sort_values('Gesamt_Overhead_Hinweg', inplace=True)
    df_plot_base = df_plot_base.set_index('Protokoll_Display')
    
    # Berechne Prozente für die Tabelle
    df_percent_table = df_plot_base[stack_order_cols].div(df_plot_base['Gesamt_Overhead_Hinweg'], axis=0) * 100
    df_percent_table.columns = [f'Prozent_{label}' for label in stack_order_labels]
    df_percent_table['Gesamt_Overhead_Hinweg'] = df_plot_base['Gesamt_Overhead_Hinweg']
    
    size_label_file = f'{int(size/1000)}KB' if size >= 1000 else f'{int(size)}B'
    df_percent_table.to_csv(os.path.join(tables_dir, f'tabelle_overhead_prozente_{size_label_file}.csv'), float_format='%.2f')
    print(f"Tabelle für {size_label_file} gespeichert.")
    
    # Erstelle das Diagramm
    ax = df_plot_base[stack_order_cols].plot(kind='bar', stacked=True, figsize=(16, 9),
                                             color=[palette[label] for label in stack_order_labels])
    
    ax.set_yscale('log')
    size_label_title = f'{int(size/1000)}KB' if size >= 1000 else f'{int(size)}B'
    ax.set_title(f'Zusammensetzung des Netzwerk-Overheads bei {size_label_title} Payload', fontsize=18, weight='bold')
    ax.set_xlabel('Protokoll', fontsize=12)
    ax.set_ylabel('Overhead in Bytes (log-Skala)', fontsize=12)
    ax.tick_params(axis='x', rotation=45)
    ax.grid(True, which="both", ls="--")
    
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, stack_order_labels, title='Header-Schicht')
    
    plt.tight_layout()
    output_filename = os.path.join(charts_dir, f'analyse_overhead_stack_{int(size)}B.png')
    plt.savefig(output_filename, dpi=300)
    plt.close()
    print(f"Diagramm für {size_label_title} gespeichert.")

print("\nFinale Overhead-Analyse wurde im Ordner 'finale_overhead_analyse' erstellt.")
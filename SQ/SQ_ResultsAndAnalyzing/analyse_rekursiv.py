import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os

# --- 0. SETUP ---
output_dir = 'diagramme_rekursiv_getrennt'
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
numeric_cols = ['Groesse_Bytes', 'Serialisierungszeit_ns', 'Deserialisierungszeit_ns']
for col in numeric_cols:
    df[col] = pd.to_numeric(df[col], errors='coerce')
df.dropna(inplace=True)

# Filtere nur die rekursiven Testfälle
df_rekursiv_all = df[df['Testfall'].str.contains("Rekursiv")].copy()

# Berechne Gesamtzeit in Mikrosekunden
df_rekursiv_all['Gesamtzeit_us'] = (df_rekursiv_all['Serialisierungszeit_ns'] + df_rekursiv_all['Deserialisierungszeit_ns']) / 1000

# --- 2. DIAGRAMM-ERSTELLUNG (ITERATIV FÜR JEDEN TESTFALL) ---
sns.set_theme(style="whitegrid")
plt.rcParams['font.size'] = 12

def save_plot(filename, folder):
    plt.tight_layout()
    filepath = os.path.join(folder, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Diagramm gespeichert: {filepath}")
    plt.close()

# Finde alle einzigartigen rekursiven Testfälle in den Daten
unique_test_cases = df_rekursiv_all['Testfall'].unique()
print(f"Folgende rekursive Testfälle wurden gefunden: {list(unique_test_cases)}")

# Erstelle für jeden Testfall ein eigenes Diagramm-Set
for test_case in unique_test_cases:
    print(f"\n--- Erstelle Diagramme für Testfall: '{test_case}' ---")
    
    # Filtere die Daten nur für den aktuellen Testfall
    df_current = df_rekursiv_all[df_rekursiv_all['Testfall'] == test_case].copy()
    
    # Erstelle einen sauberen Dateinamen-Präfix aus dem Testfall-Namen
    # z.B. "Rekursiv (Tiefe 3, Breite 20)" -> "rekursiv_tiefe_3_breite_20"
    file_prefix = test_case.lower().replace(' ', '_').replace('(', '').replace(')', '').replace(',', '')

    # --- Diagramm A: Datengröße für diesen spezifischen Testfall ---
    plt.figure(figsize=(16, 9))
    sns.barplot(data=df_current.sort_values('Groesse_Bytes'), x='Datenformat', y='Groesse_Bytes', palette='magma')
    
    plt.title(f'Datengröße - {test_case}')
    plt.ylabel('Datengröße (Bytes)')
    plt.xlabel('Datenformat')
    plt.xticks(rotation=45, ha='right')
    
    save_plot(f'{file_prefix}_groesse.png', output_dir)

    # --- Diagramm B: Verarbeitungszeit für diesen spezifischen Testfall ---
    plt.figure(figsize=(16, 9))
    sns.barplot(data=df_current.sort_values('Gesamtzeit_us'), x='Datenformat', y='Gesamtzeit_us', palette='magma')

    plt.title(f'Verarbeitungszeit - {test_case}')
    plt.ylabel('Gesamte Verarbeitungszeit (µs, log-Skala)')
    plt.xlabel('Datenformat')
    plt.yscale('log')
    plt.xticks(rotation=45, ha='right')
    
    save_plot(f'{file_prefix}_zeit.png', output_dir)
    
    # --- Diagramm C: Detaillierte Zeit-Analyse (Serialisierung vs. Deserialisierung) ---
    df_zeit_detail = df_current.melt(
        id_vars='Datenformat',
        value_vars=['Serialisierungszeit_ns', 'Deserialisierungszeit_ns'],
        var_name='Operation',
        value_name='Zeit_ns'
    )  


print(f"\nAnalyse der rekursiven Tests erfolgreich abgeschlossen. Alle Diagramme im Ordner '{output_dir}' gespeichert.")
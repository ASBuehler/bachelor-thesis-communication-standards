# bachelor-thesis-communication-standards


# Empirische Analyse von Kommunikationsstandards für effizienten Datenaustausch

Dieses Repository enthält den vollständigen Quellcode, die experimentellen Daten und die Automatisierungsskripte, die im Rahmen meiner Bachelorarbeit an der OST – Ostschweizer Fachhochschule entwickelt wurden. Die Arbeit führt eine tiefgehende, empirische Untersuchung der Performance von weit verbreiteten Kommunikationsprotokollen und Datenformaten durch.

Das Hauptziel war es, über pauschale Annahmen hinauszugehen und eine datengestützte, systematische Entscheidungsgrundlage für Entwickler und Architekten zu schaffen.

## Repository-Struktur

Dieses Projekt ist in zwei Hauptteile gegliedert, die die beiden komplementären experimentellen Verfahren der Arbeit widerspiegeln:

-   **`/SQ`**: Enthält alle Artefakte für den **Serialisierungsqualitätstest (SQ-Test)**.
    -   `/SQ_Test`: Der Quellcode des Java/Maven-basierten Test-Frameworks zur Messung der intrinsischen Eigenschaften von 13 Datenformaten.
    -   `/SQ_ResultsAndAnalyzing`: Jupyter Notebooks und Skripte zur Analyse der SQ-Test-Ergebnisse.
    -   `/SQ_Testdaten...`: Verzeichnisse mit den für die Tests verwendeten Datenmodellen und Schemata.
    -   `/SQ_TestMessreihen`: Die aggregierten Rohdaten der durchgeführten Messungen.

-   **`/SPEA`**: Enthält alle Artefakte für die **Systemische Protokoll-Effizienz-Analyse (SPEA-Test)**.
    -   `/SPEA_Test`: Der Quellcode des Java/Maven-basierten Test-Frameworks zur Messung der End-to-End-Performance von 5 Kommunikationsprotokollen.
    -   `/SPEA_ResultsAndAnalyzing`: PowerShell-Skripte zur Extraktion der Metriken aus den `.pcap`-Dateien und Jupyter Notebooks für die Analyse.
    -   `/shared`: Enthält die Bash-Skripte zur Automatisierung und Orchestrierung der Testdurchläufe auf den virtuellen Maschinen.

## Methodik im Überblick

1.  **SQ-Test (Serialisierungsqualitätstest):** Eine protokollunabhängige Analyse der reinen Serialisierungs- und Deserialisierungsleistung. Gemessen wurden **Datengrösse, Verarbeitungsgeschwindigkeit (CPU-Zeit)** und **Robustheit** der Formate.

2.  **SPEA-Test (Systemische Protokoll-Effizienz-Analyse):** Eine End-to-End-Messung der Protokolle in einem isolierten Netzwerk zwischen zwei VMs. Untersucht wurden **Netzwerk-Overhead, Paketanzahl ("Chattiness")** und **Anwendungs-Latenz (RTT)** über verschiedene Nutzlastgrössen (100 B bis 10 MB).

## Zentrale Ergebnisse

Die Experimente haben gezeigt, dass die optimale Technologiewahl kontextabhängig ist und massgeblich von der Nachrichtengrösse dominiert wird. Die Kernerkenntnisse wurden in drei "goldenen Regeln" und einem Entscheidungsmodell zusammengefasst, die im finalen Bericht detailliert erläutert werden.

## Verwendung

Die Java-Projekte in den `_Test`-Verzeichnissen basieren auf **Java 17** und **Apache Maven**. Um die Projekte zu bauen, navigieren Sie in das jeweilige Verzeichnis und führen Sie `mvn clean package` aus. Detaillierte Informationen zur Durchführung der Tests finden Sie in der Bachelorarbeit selbst.








# Empirical Analysis of Communication Standards for Efficient Data Exchange

This repository contains the complete source code, experimental data, and automation scripts developed as part of my Bachelor's thesis at OST – Eastern Switzerland University of Applied Sciences. The thesis conducts an in-depth, empirical investigation into the performance of widely used communication protocols and data formats.

The primary goal was to move beyond generalized assumptions and create a data-driven, systematic foundation for developers and architects to make informed decisions.

## Repository Structure

This project is divided into two main parts, reflecting the two complementary experimental methods used in the thesis:

-   **`/SQ`**: Contains all artifacts for the **Serialization Quality Test (SQ-Test)**.
    -   `/SQ_Test`: The source code of the Java/Maven-based test framework for measuring the intrinsic properties of 13 data formats.
    -   `/SQ_ResultsAndAnalyzing`: Jupyter Notebooks and scripts used for the analysis of the SQ-Test results.
    -   `/SQ_Testdaten...`: Directories containing the data models and schemas used in the tests.
    -   `/SQ_TestMessreihen`: The aggregated raw data from the performance measurements.

-   **`/SPEA`**: Contains all artifacts for the **Systemic Protocol Efficiency Analysis (SPEA-Test)**.
    -   `/SPEA_Test`: The source code of the Java/Maven-based test framework for measuring the end-to-end performance of 5 communication protocols.
    -   `/SPEA_ResultsAndAnalyzing`: PowerShell scripts for extracting metrics from `.pcap` files and Jupyter Notebooks for analysis.
    -   `/shared`: Contains the Bash scripts for automating and orchestrating the test runs on the virtual machines.

## Methodology Overview

1.  **SQ-Test (Serialization Quality Test):** A protocol-independent analysis of pure serialization and deserialization performance. The test measured **data size (compactness), processing speed (CPU time)**, and **robustness** of the formats.

2.  **SPEA-Test (Systemic Protocol Efficiency Analysis):** An end-to-end measurement of protocols in an isolated network between two VMs. This test examined **network overhead, packet count ("chattiness")**, and **application latency (RTT)** across various payload sizes (100 B to 10 MB).

## Key Findings

The experiments demonstrated that the optimal choice of technology is context-dependent and primarily dominated by the message payload size. The core findings were summarized in three "golden rules" and a decision-making model, which are detailed in the final thesis report.

## Usage

The Java projects located in the `_Test` directories are based on **Java 17** and **Apache Maven**. To build the projects, navigate to the respective directory and run `mvn clean package`. For detailed information on how to conduct the tests, please refer to the bachelor's thesis itself.
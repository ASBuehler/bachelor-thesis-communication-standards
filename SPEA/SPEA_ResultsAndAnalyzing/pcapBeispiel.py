from PIL import Image, ImageDraw, ImageFont

# ==============================================================================
# Globale Einstellungen
# ==============================================================================
# Farben (konsistent mit Ihren anderen Diagrammen)
BG_COLOR = "#FFFFFF" # Weisser Hintergrund für ein sauberes Diagramm
TEXT_COLOR = "#000000" # Schwarzer Text
FRAME_OUTLINE_COLOR = "#AAAAAA" # Grauer Rahmen
ETH_COLOR = "#D6EAF8" # Hellblau
IP_COLOR = "#A9CCE3"
TCP_COLOR = "#7FB3D5"
HTTP_COLOR = "#5499C7"

# Schriftarten (passen Sie den Pfad an, falls nötig)
try:
    FONT_PATH = "C:/Windows/Fonts/arial.ttf"
    font_large = ImageFont.truetype(FONT_PATH, 24)
    font_medium = ImageFont.truetype(FONT_PATH, 18)
    font_small = ImageFont.truetype(FONT_PATH, 14)
except IOError:
    print(f"Arial Font nicht gefunden. Verwende Standard-Font.")
    font_large = ImageFont.load_default()
    font_medium = ImageFont.load_default()
    font_small = ImageFont.load_default()

# Datenpunkte aus Ihrem Wireshark-Screenshot
FRAME_BYTES = 1066
IP_TOTAL_BYTES = 1052
TCP_SEGMENT_BYTES = 1032
HTTP_PAYLOAD_BYTES = 1000

ETH_HEADER_BYTES = FRAME_BYTES - IP_TOTAL_BYTES  # -> 14 Bytes
IP_HEADER_BYTES = 20 # aus Screenshot
TCP_HEADER_BYTES = 32 # aus Screenshot


# ==============================================================================
# Hauptfunktion zum Zeichnen des Diagramms
# ==============================================================================
def generate_packet_diagram():
    print("Generiere 'paket_struktur.png'...")
    img = Image.new('RGB', (1400, 500), color=BG_COLOR)
    draw = ImageDraw.Draw(img)

    # Basis-Koordinaten und Skalierungsfaktor
    padding = 50
    draw_width = img.width - 2 * padding
    pixel_per_byte = draw_width / FRAME_BYTES

    # === Layer 2: Ethernet Frame (Der äusserste Container) ===
    l2_y1, l2_y2 = 100, 180
    draw.rectangle((padding, l2_y1, padding + draw_width, l2_y2), outline=FRAME_OUTLINE_COLOR, fill=ETH_COLOR)
    draw.text((padding + 10, l2_y1 + 5), f"Ethernet II Frame (Gesamtgrösse: {FRAME_BYTES} Bytes)", font=font_large, fill=TEXT_COLOR)
    draw.text((img.width / 2, l2_y2 - 10), "tshark Feld: frame.len", font=font_small, fill=TEXT_COLOR, anchor="ms")

    # Ethernet Header
    eth_header_width = ETH_HEADER_BYTES * pixel_per_byte
    draw.text((padding + eth_header_width / 2, (l2_y1 + l2_y2) / 2), f"Ethernet\nHeader\n({ETH_HEADER_BYTES} B)", font=font_small, fill=TEXT_COLOR, anchor="mm", align="center")
    draw.line((padding + eth_header_width, l2_y1, padding + eth_header_width, l2_y2), fill=FRAME_OUTLINE_COLOR, width=2)


    # === Layer 3: IP Packet (Innerhalb des Ethernet Frames) ===
    l3_x1 = padding + eth_header_width
    l3_y1, l3_y2 = 200, 280
    ip_packet_width = IP_TOTAL_BYTES * pixel_per_byte
    draw.rectangle((l3_x1, l3_y1, l3_x1 + ip_packet_width, l3_y2), outline=FRAME_OUTLINE_COLOR, fill=IP_COLOR)
    draw.text((l3_x1 + 10, l3_y1 + 5), f"IP Packet (Gesamtgrösse: {IP_TOTAL_BYTES} Bytes)", font=font_medium, fill=TEXT_COLOR)
    draw.text((l3_x1 + ip_packet_width / 2, l3_y2 - 10), "tshark Feld: ip.len", font=font_small, fill=TEXT_COLOR, anchor="ms")

    # IP Header
    ip_header_width = IP_HEADER_BYTES * pixel_per_byte
    draw.text((l3_x1 + ip_header_width / 2, (l3_y1 + l3_y2) / 2), f"IP Header\n({IP_HEADER_BYTES} B)\nip.hdr_len", font=font_small, fill=TEXT_COLOR, anchor="mm", align="center")
    draw.line((l3_x1 + ip_header_width, l3_y1, l3_x1 + ip_header_width, l3_y2), fill=FRAME_OUTLINE_COLOR, width=2)
    
    # Verbindungslinien
    draw.line((l3_x1, l2_y2, l3_x1, l3_y1), fill=FRAME_OUTLINE_COLOR, width=1)
    draw.line((padding + draw_width, l2_y2, l3_x1 + ip_packet_width, l3_y1), fill=FRAME_OUTLINE_COLOR, width=1)


    # === Layer 4: TCP Segment (Innerhalb des IP Packets) ===
    l4_x1 = l3_x1 + ip_header_width
    l4_y1, l4_y2 = 300, 380
    tcp_segment_width = TCP_SEGMENT_BYTES * pixel_per_byte
    draw.rectangle((l4_x1, l4_y1, l4_x1 + tcp_segment_width, l4_y2), outline=FRAME_OUTLINE_COLOR, fill=TCP_COLOR)
    draw.text((l4_x1 + 10, l4_y1 + 5), f"TCP Segment (Nutzlast: {TCP_SEGMENT_BYTES} Bytes)", font=font_medium, fill=TEXT_COLOR)
    draw.text((l4_x1 + tcp_segment_width / 2, l4_y2 - 10), "tshark Feld: tcp.len", font=font_small, fill=TEXT_COLOR, anchor="ms")

    # TCP Header
    tcp_header_width = TCP_HEADER_BYTES * pixel_per_byte
    draw.text((l4_x1 + tcp_header_width / 2, (l4_y1 + l4_y2) / 2), f"TCP Header\n({TCP_HEADER_BYTES} B)\ntcp.hdr_len", font=font_small, fill=TEXT_COLOR, anchor="mm", align="center")
    draw.line((l4_x1 + tcp_header_width, l4_y1, l4_x1 + tcp_header_width, l4_y2), fill=FRAME_OUTLINE_COLOR, width=2)
    
    # Verbindungslinien
    draw.line((l4_x1, l3_y2, l4_x1, l4_y1), fill=FRAME_OUTLINE_COLOR, width=1)
    draw.line((l3_x1 + ip_packet_width, l3_y2, l4_x1 + tcp_segment_width, l4_y1), fill=FRAME_OUTLINE_COLOR, width=1)


    # === Layer 7: Anwendungs-Nutzlast (Innerhalb des TCP Segments) ===
    l7_x1 = l4_x1 + tcp_header_width
    l7_y1, l7_y2 = 400, 480
    payload_width = HTTP_PAYLOAD_BYTES * pixel_per_byte
    draw.rectangle((l7_x1, l7_y1, l7_x1 + payload_width, l7_y2), outline=FRAME_OUTLINE_COLOR, fill=HTTP_COLOR)
    draw.text((l7_x1 + payload_width / 2, (l7_y1 + l7_y2) / 2), f"Anwendungs-Nutzlast (HTTP)\n({HTTP_PAYLOAD_BYTES} Bytes)\nFeld: http.file_data", font=font_medium, fill="white", anchor="mm", align="center")

    # Verbindungslinien
    draw.line((l7_x1, l4_y2, l7_x1, l7_y1), fill=FRAME_OUTLINE_COLOR, width=1)
    draw.line((l4_x1 + tcp_segment_width, l4_y2, l7_x1 + payload_width, l7_y1), fill=FRAME_OUTLINE_COLOR, width=1)

    img.save('paket_struktur.png')
    print("...'paket_struktur.png' wurde erfolgreich erstellt.")

# ==============================================================================
# Haupt-Ausführung
# ==============================================================================
if __name__ == "__main__":
    generate_packet_diagram()
#!/usr/bin/env python3
"""Génère les textures de widgets de Nova UI dans src/main/resources/assets/minecraft/.

Usage : python3 tools/generate_textures.py   (nécessite Pillow)
Modifie les couleurs ci-dessous puis relance pour changer le thème.
"""
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GUI = ROOT / "src/main/resources/assets/minecraft/textures/gui"
SPRITES = GUI / "sprites"

# --- palette (RGBA) -----------------------------------------------------------
FILL = (20, 24, 38, 255)
FILL_HOVER = (30, 38, 66, 255)
FILL_OFF = (13, 14, 21, 255)
BORDER = (58, 64, 96, 255)
BORDER_HOVER = (79, 209, 255, 255)
BORDER_OFF = (30, 33, 48, 255)
SHINE = (255, 255, 255, 14)
ACCENT_LINE = (180, 107, 255, 255)


def box(w, h, fill, border, bottom=None):
    """Rectangle plein avec bordure de 1 px, reflet en haut et liseré optionnel en bas."""
    img = Image.new("RGBA", (w, h), fill)
    px = img.load()
    for x in range(w):
        for y in range(h):
            if x in (0, w - 1) or y in (0, h - 1):
                px[x, y] = border
    for x in range(1, w - 1):
        px[x, 1] = tuple(min(255, a + b) if i < 3 else a for i, (a, b) in enumerate(zip(fill, SHINE)))
    if bottom:
        for x in range(1, w - 1):
            px[x, h - 2] = bottom
    return img


def save(img, path, nine_slice=None):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    if nine_slice:
        w, h, border = nine_slice
        meta = {"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": border}}}
        Path(str(path) + ".mcmeta").write_text(json.dumps(meta, indent=2) + "\n")


# --- boutons ------------------------------------------------------------------
save(box(200, 20, FILL, BORDER, bottom=(40, 45, 72, 255)), SPRITES / "widget/button.png", (200, 20, 3))
save(box(200, 20, FILL_HOVER, BORDER_HOVER, bottom=ACCENT_LINE), SPRITES / "widget/button_highlighted.png", (200, 20, 3))
save(box(200, 20, FILL_OFF, BORDER_OFF), SPRITES / "widget/button_disabled.png", (200, 20, 3))

# --- champs de texte ----------------------------------------------------------
save(box(200, 20, (8, 9, 14, 255), BORDER), SPRITES / "widget/text_field.png", (200, 20, 1))
save(box(200, 20, (10, 12, 20, 255), BORDER_HOVER), SPRITES / "widget/text_field_highlighted.png", (200, 20, 1))

# --- sliders ------------------------------------------------------------------
save(box(200, 20, (11, 12, 19, 255), BORDER), SPRITES / "widget/slider.png", (200, 20, 3))
save(box(200, 20, (14, 16, 26, 255), BORDER_HOVER), SPRITES / "widget/slider_highlighted.png", (200, 20, 3))
save(box(8, 20, (60, 70, 110, 255), BORDER_HOVER), SPRITES / "widget/slider_handle.png", (8, 20, 3))
save(box(8, 20, (90, 105, 160, 255), (255, 255, 255, 255)), SPRITES / "widget/slider_handle_highlighted.png", (8, 20, 3))

# --- emplacement d'inventaire ---------------------------------------------------
save(box(18, 18, (8, 9, 14, 255), (42, 47, 71, 255)), SPRITES / "container/slot.png")

# --- fonds de menu (tuiles 16x16) ------------------------------------------------
bg = Image.new("RGBA", (16, 16), (10, 12, 19, 255))
bpx = bg.load()
for i in range(0, 16, 5):
    bpx[(i * 7) % 16, (i * 3) % 16] = (16, 19, 30, 255)
SPRITES.parent.mkdir(parents=True, exist_ok=True)
bg.save(GUI / "menu_background.png")

inworld = Image.new("RGBA", (16, 16), (8, 9, 15, 205))
inworld.save(GUI / "inworld_menu_background.png")

# --- séparateurs d'en-tête / pied (32x2) -----------------------------------------
for name in ("header_separator.png", "footer_separator.png"):
    sep = Image.new("RGBA", (32, 2), (0, 0, 0, 255))
    spx = sep.load()
    for x in range(32):
        spx[x, 0] = (79, 209, 255, 255)
        spx[x, 1] = (40, 45, 72, 255)
    sep.save(GUI / name)

print("Textures générées dans", GUI)

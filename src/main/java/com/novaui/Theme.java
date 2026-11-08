package com.novaui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Palette et utilitaires de dessin partagés par tout le mod.
 * Toutes les couleurs sont en ARGB.
 */
public final class Theme {
	public static final int BG_TOP = 0xFF090B12;
	public static final int BG_BOTTOM = 0xFF1B1036;
	public static final int PANEL = 0xD0101320;
	public static final int SLOT = 0xB0080A12;
	public static final int BORDER = 0xFF2A2F47;
	public static final int ACCENT = 0xFF4FD1FF;
	public static final int ACCENT_2 = 0xFFB46BFF;
	public static final int TEXT = 0xFFE8ECFF;
	public static final int TEXT_DIM = 0xFF8D94B5;

	public static final int HEALTH_A = 0xFFFF6B86;
	public static final int HEALTH_B = 0xFFC2243F;
	public static final int FOOD_A = 0xFFFFC857;
	public static final int FOOD_B = 0xFFD9822B;
	public static final int ARMOR = 0xFF9FB4D6;
	public static final int AIR = 0xFF5CC8FF;
	public static final int XP_A = 0xFF9BFF7A;
	public static final int XP_B = 0xFF2FBF5B;
	public static final int ABSORPTION = 0xFFFFE066;

	private Theme() {
	}

	// ---------------------------------------------------------------- couleurs

	public static int alpha(int argb, int a) {
		return ((a & 0xFF) << 24) | (argb & 0x00FFFFFF);
	}

	public static int lerp(int c1, int c2, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
		int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
		int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
		int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	// ------------------------------------------------------------------ formes

	public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill, int border) {
		g.fill(x, y, x + w, y + h, fill);
		g.outline(x, y, w, h, border);
	}

	/** Halo autour d'un rectangle : quelques contours de plus en plus transparents. */
	public static void glow(GuiGraphicsExtractor g, int x, int y, int w, int h, int color, int layers) {
		for (int i = 1; i <= layers; i++) {
			g.outline(x - i, y - i, w + 2 * i, h + 2 * i, alpha(color, 0x48 / (i + 1)));
		}
	}

	/** Dégradé horizontal (fillGradient de Minecraft est vertical). */
	public static void hGradient(GuiGraphicsExtractor g, int x, int y, int w, int h, int c1, int c2) {
		if (w <= 0) {
			return;
		}
		// Au plus 24 bandes : largement suffisant visuellement et 3x moins d'appels de dessin.
		int step = Math.max(1, (w + 23) / 24);
		for (int i = 0; i < w; i += step) {
			int sw = Math.min(step, w - i);
			g.fill(x + i, y, x + i + sw, y + h, lerp(c1, c2, (i + sw / 2f) / w));
		}
	}

	/** Disque plein dessiné ligne par ligne. */
	public static void disc(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
		// Bandes de 3 px de haut + demi-largeurs mises en cache : ~3x moins de fill() et aucun sqrt par image.
		int[] hws = halfWidths(r);
		for (int i = 0; i < hws.length; i++) {
			int dy = -r + i * BAND;
			g.fill(cx - hws[i], cy + dy, cx + hws[i], cy + dy + BAND, color);
		}
	}

	private static final int BAND = 3;
	private static final java.util.Map<Integer, int[]> HW_CACHE = new java.util.HashMap<>();

	private static int[] halfWidths(int r) {
		return HW_CACHE.computeIfAbsent(r, rr -> {
			int n = (2 * rr) / BAND + 1;
			int[] out = new int[n];
			for (int i = 0; i < n; i++) {
				double dy = -rr + i * BAND + BAND / 2.0;
				out[i] = (int) Math.sqrt(Math.max(0.0, (double) rr * rr - dy * dy));
			}
			return out;
		});
	}

	/** Champ d'étoiles animé, déterministe. */
	public static void stars(GuiGraphicsExtractor g, int w, int h, long ms) {
		for (int i = 0; i < 60; i++) {
			int hsh = mix(i);
			float speed = 2f + (hsh & 7);
			int x = (int) ((((hsh >>> 3) & 0xFFFF) % w + ms / 1000f * speed) % w);
			int y = ((hsh >>> 19) & 0x1FFF) % h;
			int size = (hsh & 0x30) == 0 ? 2 : 1;
			float tw = 0.5f + 0.5f * (float) Math.sin(ms / 700.0 + i);
			int a = (int) (60 + 140 * tw);
			g.fill(x, y, x + size, y + size, (a << 24) | 0xCFE8FF);
		}
	}

	private static int mix(int i) {
		int x = i * 0x9E3779B1;
		x ^= x >>> 16;
		x *= 0x85EBCA6B;
		x ^= x >>> 13;
		return x;
	}

	// ------------------------------------------------------------------- texte

	/**
	 * Texte agrandi, centré en cx. C'est la seule méthode qui utilise la pile de
	 * matrices : si l'API change dans une future version, c'est ici qu'il faut corriger.
	 */
	public static void bigText(GuiGraphicsExtractor g, Font font, String s, int cx, int y, float scale, int color) {
		g.pose().pushMatrix();
		g.pose().translate((float) cx, (float) y);
		g.pose().scale(scale, scale);
		g.text(font, s, -font.width(s) / 2, 0, color, true);
		g.pose().popMatrix();
	}
}

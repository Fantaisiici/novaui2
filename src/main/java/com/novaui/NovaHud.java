package com.novaui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * HUD en jeu : barre d'objets flottante, barres de vie / faim / armure / air / XP.
 * Les éléments vanilla correspondants sont remplacés (HOTBAR) ou rendus muets.
 */
public final class NovaHud {
	private static final int SLOT = 20;
	private static final int GAP = 3;
	private static final int SLOTS = 9;
	private static final int ROW_W = SLOTS * SLOT + (SLOTS - 1) * GAP; // 204
	private static final int BAR_W = 96;
	private static final int BAR_H = 10;

	private NovaHud() {
	}

	public static void register() {
		// Notre élément remplace la hotbar vanilla et dessine aussi toutes les barres.
		HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, previous -> NovaHud::extract);

		HudElement hidden = (graphics, delta) -> {
		};
		Identifier[] vanillaToHide = {
				VanillaHudElements.HEALTH_BAR,
				VanillaHudElements.FOOD_BAR,
				VanillaHudElements.ARMOR_BAR,
				VanillaHudElements.AIR_BAR,
				VanillaHudElements.INFO_BAR
		};
		for (Identifier id : vanillaToHide) {
			HudElementRegistry.replaceElement(id, previous -> hidden);
		}
	}

	private static void extract(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || player.isSpectator()) {
			return;
		}

		int sw = mc.getWindow().getGuiScaledWidth();
		int sh = mc.getWindow().getGuiScaledHeight();
		int x0 = (sw - ROW_W) / 2;
		int y0 = sh - 38;
		long now = Util.getMillis();

		// --- fond flottant de la barre d'objets
		Theme.panel(g, x0 - 5, y0 - 5, ROW_W + 10, SLOT + 10, Theme.PANEL, Theme.BORDER);
		g.fill(x0 - 4, y0 + SLOT + 4, x0 + ROW_W + 4, y0 + SLOT + 5, Theme.alpha(Theme.ACCENT, 0x90));

		// --- emplacements
		int selected = player.getInventory().getSelectedSlot();
		for (int i = 0; i < SLOTS; i++) {
			int sx = x0 + i * (SLOT + GAP);
			ItemStack stack = player.getInventory().getItem(i);
			boolean isSelected = i == selected;
			int sy = isSelected ? y0 - 3 : y0;

			g.fill(sx, sy, sx + SLOT, sy + SLOT, isSelected ? Theme.alpha(Theme.ACCENT, 0x38) : Theme.SLOT);
			if (isSelected) {
				Theme.glow(g, sx, sy, SLOT, SLOT, Theme.ACCENT, 3);
				g.outline(sx, sy, SLOT, SLOT, Theme.ACCENT);
			} else {
				g.outline(sx, sy, SLOT, SLOT, Theme.alpha(Theme.BORDER, 0xB0));
			}

			if (stack.isEmpty()) {
				String n = num(i + 1);
				g.text(mc.font, n, sx + (SLOT - mc.font.width(n)) / 2 + 1, sy + 6, Theme.alpha(Theme.TEXT_DIM, 0x70), false);
			} else {
				g.item(stack, sx + 2, sy + 2);
				g.itemDecorations(mc.font, stack, sx + 2, sy + 2);
			}
		}

		// --- main secondaire
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.isEmpty()) {
			int ox = x0 - 5 - 8 - SLOT - 4;
			Theme.panel(g, ox - 4, y0 - 5, SLOT + 8, SLOT + 10, Theme.PANEL, Theme.BORDER);
			g.item(offhand, ox, y0 + 0);
			g.itemDecorations(mc.font, offhand, ox, y0 + 0);
		}

		// --- barres de statut (uniquement en mode où l'on peut subir des dégâts)
		boolean survival = mc.gameMode != null && mc.gameMode.canHurtPlayer();
		if (survival) {
			int barsY = y0 - 18;

			// vie, à gauche
			float maxHealth = Math.max(1f, player.getMaxHealth());
			float health = Math.max(0f, player.getHealth());
			int healthBorder = Theme.BORDER;
			if (health <= 6f) {
				float pulse = 0.5f + 0.5f * (float) Math.sin(now / 160.0);
				healthBorder = Theme.lerp(Theme.BORDER, 0xFFFF2D4D, pulse);
			}
			bar(g, x0, barsY, BAR_W, BAR_H, health / maxHealth, Theme.HEALTH_A, Theme.HEALTH_B, healthBorder);
			float absorb = player.getAbsorptionAmount();
			if (absorb > 0f) {
				int aw = (int) ((BAR_W - 2) * Math.min(1f, absorb / maxHealth));
				g.fill(x0 + 1, barsY + 1, x0 + 1 + aw, barsY + 3, Theme.ABSORPTION);
			}
			label(g, mc, num((int) Math.ceil(health)), x0 + 4, barsY + 1);

			// faim, à droite
			int food = player.getFoodData().getFoodLevel();
			int foodX = x0 + ROW_W - BAR_W;
			bar(g, foodX, barsY, BAR_W, BAR_H, food / 20f, Theme.FOOD_A, Theme.FOOD_B, Theme.BORDER);
			String foodText = num(food);
			label(g, mc, foodText, foodX + BAR_W - 4 - mc.font.width(foodText), barsY + 1);

			// armure : fine ligne au-dessus de la vie
			int armor = player.getArmorValue();
			if (armor > 0) {
				thin(g, x0, barsY - 5, BAR_W, armor / 20f, Theme.ARMOR);
			}

			// air : fine ligne au-dessus de la faim, seulement quand il diminue
			int air = player.getAirSupply();
			int maxAir = player.getMaxAirSupply();
			if (air < maxAir) {
				thin(g, foodX, barsY - 5, BAR_W, Math.max(0f, (float) air / maxAir), Theme.AIR);
			}
		}

		// --- XP : fine barre sous la barre d'objets + niveau
		int xpY = sh - 7;
		g.fill(x0 - 5, xpY, x0 + ROW_W + 5, xpY + 3, 0xA0050609);
		int xpW = (int) ((ROW_W + 10) * Math.max(0f, Math.min(1f, player.experienceProgress)));
		Theme.hGradient(g, x0 - 5, xpY, xpW, 3, Theme.XP_B, Theme.XP_A);
		if (player.experienceLevel > 0) {
			String lvl = num(player.experienceLevel);
			g.text(mc.font, lvl, (sw - mc.font.width(lvl)) / 2, xpY - 11, Theme.XP_A, true);
		}
	}

	private static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float frac, int top, int bottom, int border) {
		g.fill(x, y, x + w, y + h, 0xB004050A);
		int fw = (int) ((w - 2) * Math.max(0f, Math.min(1f, frac)));
		if (fw > 0) {
			g.fillGradient(x + 1, y + 1, x + 1 + fw, y + h - 1, top, bottom);
		}
		g.outline(x, y, w, h, border);
	}

	private static void thin(GuiGraphicsExtractor g, int x, int y, int w, float frac, int color) {
		g.fill(x, y, x + w, y + 3, 0xA0050609);
		int fw = (int) (w * Math.max(0f, Math.min(1f, frac)));
		g.fill(x, y, x + fw, y + 3, color);
	}

	private static void label(GuiGraphicsExtractor g, Minecraft mc, String text, int x, int y) {
		g.text(mc.font, text, x, y, Theme.TEXT, true);
	}

	/** Chaînes 0..99 pré-calculées : évite une allocation par image pour les valeurs du HUD. */
	private static final String[] NUMBERS = new String[100];

	static {
		for (int i = 0; i < NUMBERS.length; i++) {
			NUMBERS[i] = Integer.toString(i);
		}
	}

	private static String num(int v) {
		return v >= 0 && v < NUMBERS.length ? NUMBERS[v] : Integer.toString(v);
	}
}

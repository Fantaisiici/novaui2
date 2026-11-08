package com.novaui;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

/**
 * Écran titre de remplacement : fond animé, panneau latéral, menu vertical.
 * Il remplace le TitleScreen vanilla (voir NovaUI#swapTitleScreen).
 */
public class NovaTitleScreen extends Screen {
	private static final int PANEL_W = 240;

	public NovaTitleScreen() {
		super(Component.literal("Nova"));
	}

	@Override
	protected void init() {
		int bx = 30;
		int bw = PANEL_W - 60;
		int bh = 22;
		int gap = 7;
		int y = this.height / 2 - 6;

		this.addRenderableWidget(Button.builder(Component.translatable("menu.singleplayer"),
				b -> this.minecraft.gui.setScreen(new SelectWorldScreen(this)))
				.bounds(bx, y, bw, bh).build());
		y += bh + gap;

		this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"),
				b -> this.minecraft.gui.setScreen(new JoinMultiplayerScreen(this)))
				.bounds(bx, y, bw, bh).build());
		y += bh + gap;

		this.addRenderableWidget(Button.builder(Component.translatable("menu.options"),
				b -> this.minecraft.gui.setScreen(new OptionsScreen(this, this.minecraft.options)))
				.bounds(bx, y, bw, bh).build());
		y += bh + gap + 10;

		this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"),
				b -> this.minecraft.stop())
				.bounds(bx, y, bw, bh).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		long now = Util.getMillis();

		// --- fond : dégradé + orbes lumineux + étoiles
		g.fillGradient(0, 0, this.width, this.height, Theme.BG_TOP, Theme.BG_BOTTOM);

		float t = now / 1000f;
		int ox1 = (int) (this.width * 0.68f + Math.sin(t * 0.35f) * 40);
		int oy1 = (int) (this.height * 0.40f + Math.cos(t * 0.27f) * 25);
		Theme.disc(g, ox1, oy1, 110, Theme.alpha(Theme.ACCENT_2, 0x0C));
		Theme.disc(g, ox1, oy1, 72, Theme.alpha(Theme.ACCENT_2, 0x12));
		Theme.disc(g, ox1, oy1, 36, Theme.alpha(Theme.ACCENT_2, 0x1C));

		int ox2 = (int) (this.width * 0.42f + Math.cos(t * 0.22f) * 50);
		int oy2 = (int) (this.height * 0.72f + Math.sin(t * 0.31f) * 20);
		Theme.disc(g, ox2, oy2, 90, Theme.alpha(Theme.ACCENT, 0x0A));
		Theme.disc(g, ox2, oy2, 52, Theme.alpha(Theme.ACCENT, 0x10));
		Theme.disc(g, ox2, oy2, 24, Theme.alpha(Theme.ACCENT, 0x18));

		Theme.stars(g, this.width, this.height, now);

		// --- panneau latéral
		g.fill(0, 0, PANEL_W, this.height, 0xE00C0E18);
		Theme.hGradient(g, 0, 0, PANEL_W, 2, Theme.ACCENT, Theme.ACCENT_2);
		g.fillGradient(PANEL_W - 1, 0, PANEL_W, this.height, Theme.ACCENT, Theme.ACCENT_2);

		// --- titre
		int cx = PANEL_W / 2;
		int titleY = this.height / 2 - 92;
		Theme.bigText(g, this.font, "NOVA", cx, titleY, 4.5f, Theme.TEXT);
		String sub = "I N T E R F A C E";
		g.text(this.font, sub, cx - this.font.width(sub) / 2, titleY + 44, Theme.ACCENT, false);
		Theme.hGradient(g, 30, titleY + 58, PANEL_W - 60, 1, Theme.ACCENT, Theme.ACCENT_2);

		// --- boutons (on ne passe pas par super : il redessinerait le fond vanilla)
		for (GuiEventListener child : this.children()) {
			if (child instanceof Renderable r) {
				r.extractRenderState(g, mouseX, mouseY, delta);
			}
		}

		// --- pied de page
		String version = "Minecraft " + this.minecraft.getLaunchedVersion();
		g.text(this.font, version, 30, this.height - 18, Theme.TEXT_DIM, false);
	}
}

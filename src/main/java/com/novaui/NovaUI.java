package com.novaui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Point d'entrée client de Nova UI.
 * <ul>
 *   <li>HUD : {@link NovaHud}</li>
 *   <li>Écran titre : {@link NovaTitleScreen}</li>
 *   <li>Tous les autres écrans : habillage via ScreenEvents + textures du resource pack intégré
 *       (boutons, champs de texte, sliders, fond des menus).</li>
 * </ul>
 */
public class NovaUI implements ClientModInitializer {
	public static final String MOD_ID = "novaui";

	@Override
	public void onInitializeClient() {
		NovaHud.register();

		// Remplace l'écran titre vanilla dès qu'il apparaît.
		ClientTickEvents.END_CLIENT_TICK.register(NovaUI::swapTitleScreen);

		// Habillage commun à tous les écrans (sauf le nôtre, qui gère son propre rendu).
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof NovaTitleScreen) {
				return;
			}
			ScreenEvents.afterExtract(screen).register(NovaUI::decorate);
		});
	}

	private static void swapTitleScreen(Minecraft client) {
		if (client.gui.screen() instanceof TitleScreen) {
			client.gui.setScreen(new NovaTitleScreen());
		}
	}

	/** Voile coloré très léger + liserés dégradés en haut et en bas de l'écran. */
	private static void decorate(Screen screen, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int w = screen.width;
		int h = screen.height;
		g.fillGradient(0, 0, w, h, Theme.alpha(Theme.ACCENT_2, 0x10), Theme.alpha(Theme.ACCENT, 0x0A));
		Theme.hGradient(g, 0, 0, w, 1, Theme.ACCENT, Theme.ACCENT_2);
		Theme.hGradient(g, 0, h - 1, w, 1, Theme.ACCENT_2, Theme.ACCENT);
	}
}

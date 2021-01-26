package com.simibubi.create.foundation.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ScreenOpener {

	@Environment(EnvType.CLIENT)
	private static PresetsScreen openedGuiNextTick;

	public static void tick() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if (openedGuiNextTick != null) {
				KeyBinding.B().a(openedGuiNextTick);
				openedGuiNextTick = null;
			}
		});
	}

	public static void open(PresetsScreen gui) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			openedGuiNextTick = gui;
		});
	}

}

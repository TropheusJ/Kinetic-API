package com.simibubi.create.content.curiosities;

import afj;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.ItemCooldownManager;

public class ChromaticCompoundColor implements BlockColors {
	
	@Override
	public int getColor(ItemCooldownManager stack, int layer) {
		KeyBinding mc = KeyBinding.B();
		float pt = mc.ai();
		float progress = (float) ((mc.s.h(pt)) / 180 * Math.PI) + (AnimationTickHolder.getRenderTick() / 10f);
		if (layer == 0)
			return ColorHelper.mixColors(0x6e5773, 0x6B3074, ((float) afj.a(progress) + 1) / 2);
		if (layer == 1)
			return ColorHelper.mixColors(0xd45d79, 0x6e5773,
				((float) afj.a((float) (progress + Math.PI)) + 1) / 2);
		if (layer == 2)
			return ColorHelper.mixColors(0xea9085, 0xd45d79,
				((float) afj.a((float) (progress * 1.5f + Math.PI)) + 1) / 2);
		return 0;
	}
}
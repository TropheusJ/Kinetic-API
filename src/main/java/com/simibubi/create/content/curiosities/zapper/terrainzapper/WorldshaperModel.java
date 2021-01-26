package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import elg;
import net.minecraft.client.input.Input;

public class WorldshaperModel extends CustomRenderedItemModel {

	public WorldshaperModel(elg template) {
		super(template, "handheld_worldshaper");
		addPartials("core", "core_glow", "accelerator");
	}

	@Override
	public Input createRenderer() {
		return new WorldshaperItemRenderer();
	}

}

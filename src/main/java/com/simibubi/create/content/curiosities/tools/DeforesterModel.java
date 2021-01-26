package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import elg;
import net.minecraft.client.input.Input;

public class DeforesterModel extends CustomRenderedItemModel {

	public DeforesterModel(elg template) {
		super(template, "deforester");
		addPartials("gear", "core", "core_glow");
	}

	@Override
	public Input createRenderer() {
		return new DeforesterItemRenderer();
	}

}
package com.simibubi.kinetic_api.content.curiosities.symmetry.client;

import com.simibubi.kinetic_api.foundation.block.render.CustomRenderedItemModel;
import elg;
import net.minecraft.client.input.Input;

public class SymmetryWandModel extends CustomRenderedItemModel {

	public SymmetryWandModel(elg template) {
		super(template, "wand_of_symmetry");
		addPartials("bits", "core", "core_glow");
	}

	@Override
	public Input createRenderer() {
		return new SymmetryWandItemRenderer();
	}
	
}

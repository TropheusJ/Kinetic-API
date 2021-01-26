package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import elg;
import net.minecraft.client.input.Input;

public class WrenchModel extends CustomRenderedItemModel {

	public WrenchModel(elg template) {
		super(template, "wrench");
		addPartials("gear");
	}

	@Override
	public Input createRenderer() {
		return new WrenchItemRenderer();
	}

}

package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import elg;
import net.minecraft.client.input.Input;

public class ExtendoGripModel extends CustomRenderedItemModel {

	public ExtendoGripModel(elg template) {
		super(template, "extendo_grip");
		addPartials("cog", "thin_short", "wide_short", "thin_long", "wide_long");
	}

	@Override
	public Input createRenderer() {
		return new ExtendoGripItemRenderer();
	}

}

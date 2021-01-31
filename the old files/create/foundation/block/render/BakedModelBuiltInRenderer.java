package com.simibubi.kinetic_api.foundation.block.render;

import elg;

public class BakedModelBuiltInRenderer extends WrappedBakedModel {

	public BakedModelBuiltInRenderer(elg template) {
		super(template);
	}
	
	@Override
	public boolean d() {
		return true;
	}

}

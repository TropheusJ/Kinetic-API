package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.math.Direction.Axis;

public abstract class AnimatedKinetics implements IDrawable {

	public static float getCurrentAngle() {
		return ((AnimationTickHolder.ticks + KeyBinding.B().ai()) * 4f) % 360;
	}
	
	protected PistonHandler shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState().a(BambooLeaves.F, axis);
	}
	
	protected AllBlockPartials cogwheel() {
		return AllBlockPartials.SHAFTLESS_COGWHEEL;
	}
	
	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 50;
	}

}

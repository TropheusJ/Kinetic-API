package com.simibubi.kinetic_api.foundation.utility.worldWrappers;

import java.util.function.BiFunction;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;

public class RayTraceWorld implements MobSpawnerLogic {

	private GrassColors template;
	private BiFunction<BlockPos, PistonHandler, PistonHandler> stateGetter;

	public RayTraceWorld(GrassColors template, BiFunction<BlockPos, PistonHandler, PistonHandler> stateGetter) {
		this.template = template;
		this.stateGetter = stateGetter;
	}

	@Override
	public BeehiveBlockEntity c(BlockPos pos) {
		return template.c(pos);
	}

	@Override
	public PistonHandler d_(BlockPos pos) {
		return stateGetter.apply(pos, template.d_(pos));
	}

	@Override
	public EmptyFluid b(BlockPos pos) {
		return template.b(pos);
	}

}

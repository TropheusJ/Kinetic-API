package com.simibubi.kinetic_api.foundation.utility.placement;

import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BannerItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import java.util.function.Function;

public class PlacementOffset {

	private final boolean success;
	private final Vec3i pos;
	private final Function<PistonHandler, PistonHandler> stateTransform;

	private PlacementOffset(boolean success, Vec3i pos, Function<PistonHandler, PistonHandler> transform) {
		this.success = success;
		this.pos = pos;
		this.stateTransform = transform == null ? Function.identity() : transform;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false, Vec3i.ZERO, null);
	}

	public static PlacementOffset success(Vec3i pos) {
		return new PlacementOffset(true, pos, null);
	}

	public static PlacementOffset success(Vec3i pos, Function<PistonHandler, PistonHandler> transform) {
		return new PlacementOffset(true, pos, transform);
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vec3i getPos() {
		return pos;
	}

	public Function<PistonHandler, PistonHandler> getTransform() {
		return stateTransform;
	}

	public boolean isReplaceable(GameMode world) {
		if (!success)
			return false;

		return world.d_(new BlockPos(pos)).c().e();
	}

	public void placeInWorld(GameMode world, BannerItem blockItem, PlayerAbilities player, ItemCooldownManager item) {
		placeInWorld(world, blockItem.e().n(), player, item);
	}
	
	public void placeInWorld(GameMode world, PistonHandler defaultState, PlayerAbilities player, ItemCooldownManager item) {
		if (world.v)
			return;

		BlockPos newPos = new BlockPos(pos);
		PistonHandler state = stateTransform.apply(defaultState);
		if (state.b(BambooLeaves.C)) {
			EmptyFluid fluidState = world.b(newPos);
			state = state.a(BambooLeaves.C, fluidState.a() == FlowableFluid.c);
		}

		world.a(newPos, state);

		if (!player.b_())
			item.g(1);
	}
}

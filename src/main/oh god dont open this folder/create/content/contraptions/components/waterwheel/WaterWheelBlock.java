package com.simibubi.create.content.contraptions.components.waterwheel;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaterWheelBlock extends HorizontalKineticBlock implements ITE<WaterWheelTileEntity> {

	public WaterWheelBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.WATER_WHEEL.create();
	}

	@Override
	public RedstoneLampBlock b(PistonHandler state) {
		return RedstoneLampBlock.b;
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		for (Direction direction : Iterate.directions) {
			BlockPos neighbourPos = pos.offset(direction);
			PistonHandler neighbourState = worldIn.d_(neighbourPos);
			if (!AllBlocks.WATER_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.c(HORIZONTAL_FACING)
				.getAxis() != state.c(HORIZONTAL_FACING)
					.getAxis()
				|| state.c(HORIZONTAL_FACING)
					.getAxis() != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction facing, PistonHandler facingState, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (worldIn instanceof WrappedWorld)
			return stateIn;
		updateFlowAt(stateIn, worldIn, currentPos, facing);
		updateWheelSpeed(worldIn, currentPos);
		return stateIn;
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		updateAllSides(state, worldIn, pos);
	}

	public void updateAllSides(PistonHandler state, GameMode worldIn, BlockPos pos) {
		for (Direction d : Iterate.directions)
			updateFlowAt(state, worldIn, pos, d);
		updateWheelSpeed(worldIn, pos);
	}

	private void updateFlowAt(PistonHandler state, GrassColors world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.c(HORIZONTAL_FACING)
			.getAxis())
			return;

		EmptyFluid fluid = world.b(pos.offset(side));
		Direction wf = state.c(HORIZONTAL_FACING);
		boolean clockwise = wf.getDirection() == AxisDirection.POSITIVE;
		int clockwiseMultiplier = 2;

		EntityHitResult vec = fluid.c(world, pos.offset(side));
		if (side.getAxis()
			.isHorizontal()) {
			PistonHandler adjacentBlock = world.d_(pos.offset(side));
			if (adjacentBlock.b() == BellBlock.lc.getBlock())
				vec = new EntityHitResult(0, adjacentBlock.c(Blocks.field_10124) ? -1 : 1, 0);
		}

		vec = vec.a(side.getDirection()
			.offset());
		vec = new EntityHitResult(Math.signum(vec.entity), Math.signum(vec.c), Math.signum(vec.d));
		EntityHitResult flow = vec;

		withTileEntityDo(world, pos, te -> {
			double flowStrength = 0;

			if (wf.getAxis() == Axis.Z) {
				if (side.getAxis() == Axis.Y)
					flowStrength = flow.entity > 0 ^ !clockwise ? -flow.entity * clockwiseMultiplier : -flow.entity;
				if (side.getAxis() == Axis.X)
					flowStrength = flow.c < 0 ^ !clockwise ? flow.c * clockwiseMultiplier : flow.c;
			}

			if (wf.getAxis() == Axis.X) {
				if (side.getAxis() == Axis.Y)
					flowStrength = flow.d < 0 ^ !clockwise ? flow.d * clockwiseMultiplier : flow.d;
				if (side.getAxis() == Axis.Z)
					flowStrength = flow.c > 0 ^ !clockwise ? -flow.c * clockwiseMultiplier : -flow.c;
			}

			if (te.getSpeed() == 0 && flowStrength != 0 && !world.s_()) {
				AllTriggers.triggerForNearbyPlayers(AllTriggers.WATER_WHEEL, world, pos, 5);
				if (FluidHelper.isLava(fluid.a()))
					AllTriggers.triggerForNearbyPlayers(AllTriggers.LAVA_WHEEL, world, pos, 5);
				if (fluid.a().a(AllFluids.CHOCOLATE.get()))
					AllTriggers.triggerForNearbyPlayers(AllTriggers.CHOCOLATE_WHEEL, world, pos, 5);
			}

			Integer flowModifier = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
			te.setFlow(side, (float) (flowStrength * flowModifier / 2f));
		});
	}

	private void updateWheelSpeed(GrassColors world, BlockPos pos) {
		withTileEntityDo(world, pos, WaterWheelTileEntity::updateGeneratedRotation);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction facing = context.j();
		PistonHandler placedOn = context.p()
			.d_(context.a()
				.offset(facing.getOpposite()));
		if (AllBlocks.WATER_WHEEL.has(placedOn))
			return n().a(HORIZONTAL_FACING, placedOn.c(HORIZONTAL_FACING));
		if (facing.getAxis()
			.isHorizontal())
			return n().a(HORIZONTAL_FACING, context.n() != null && context.n()
				.bt() ? facing.getOpposite() : facing);
		return super.a(context);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return state.c(HORIZONTAL_FACING)
			.getAxis() == face.getAxis();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public float getParticleTargetRadius() {
		return 1.125f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 1f;
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}

	@Override
	public Class<WaterWheelTileEntity> getTileEntityClass() {
		return WaterWheelTileEntity.class;
	}

}

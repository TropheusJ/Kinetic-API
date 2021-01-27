package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class AxisPipeBlock extends RepeaterBlock implements IWrenchableWithBracket, IAxisPipe {

	public AxisPipeBlock(c p_i48339_1_) {
		super(p_i48339_1_);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		boolean blockTypeChanged = state.b() != newState.b();
		if (blockTypeChanged && !world.v)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> BeetrootsBlock.a(world, pos, stack));
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.o(pos);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg hit) {
		if (!AllBlocks.COPPER_CASING.isIn(player.b(hand)))
			return Difficulty.PASS;
		if (!world.v) {
			PistonHandler newState = AllBlocks.ENCASED_FLUID_PIPE.getDefaultState();
			for (Direction d : Iterate.directionsInAxis(getAxis(state)))
				newState = newState.a(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), true);
			world.a(pos, newState);
		}
		AllTriggers.triggerFor(AllTriggers.CASING_PIPE, player);
		return Difficulty.SUCCESS;
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (world.v)
			return;
		if (state != oldState)
			world.I()
				.a(pos, this, 1, StructureAccessor.c);
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
		PlayerAbilities player) {
		return AllBlocks.FLUID_PIPE.asStack();
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugInfoSender.a(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.I()
			.a(pos, this, 1, StructureAccessor.c);
	}

	public static boolean isOpenAt(PistonHandler state, Direction d) {
		return d.getAxis() == state.c(e);
	}

	@Override
	public void a(PistonHandler state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.EIGHT_VOXEL_POLE.get(state.c(e));
	}

	public PistonHandler toRegularPipe(GrassColors world, BlockPos pos, PistonHandler state) {
		Direction side = Direction.get(AxisDirection.POSITIVE, state.c(e));
		Map<Direction, BedPart> facingToPropertyMap = FluidPipeBlock.g;
		return AllBlocks.FLUID_PIPE.get()
			.updateBlockState(AllBlocks.FLUID_PIPE.getDefaultState()
				.a(facingToPropertyMap.get(side), true)
				.a(facingToPropertyMap.get(side.getOpposite()), true), side, null, world, pos);
	}

	@Override
	public Axis getAxis(PistonHandler state) {
		return state.c(e);
	}

	@Override
	public Optional<ItemCooldownManager> removeBracket(MobSpawnerLogic world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		PistonHandler bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == BellBlock.FACING.n())
			return Optional.empty();
		return Optional.of(new ItemCooldownManager(bracket.b()));
	}

}

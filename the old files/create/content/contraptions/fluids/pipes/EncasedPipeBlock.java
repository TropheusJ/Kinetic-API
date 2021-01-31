package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import static net.minecraft.block.enums.BambooLeaves.H;
import static net.minecraft.block.enums.BambooLeaves.J;
import static net.minecraft.block.enums.BambooLeaves.I;
import static net.minecraft.block.enums.BambooLeaves.K;
import static net.minecraft.block.enums.BambooLeaves.G;
import static net.minecraft.block.enums.BambooLeaves.L;

import bnx;
import java.util.Map;
import java.util.Random;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidPropagator;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.kinetic_api.content.schematics.ItemRequirement;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.NyliumBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class EncasedPipeBlock extends BeetrootsBlock implements IWrenchable, ISpecialBlockItemRequirement {

	public static final Map<Direction, BedPart> FACING_TO_PROPERTY_MAP = NyliumBlock.g;

	public EncasedPipeBlock(c p_i48339_1_) {
		super(p_i48339_1_);
		j(n().a(I, false)
			.a(K, false)
			.a(H, false)
			.a(G, false)
			.a(L, false)
			.a(J, false));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(I, J, K, L, G, H);
		super.a(builder);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		boolean blockTypeChanged = state.b() != newState.b();
		if (blockTypeChanged && !world.v)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.o(pos);
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (!world.v && state != oldState)
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
		if (!state.c(FACING_TO_PROPERTY_MAP.get(d)))
			return;
		world.I()
			.a(pos, this, 1, StructureAccessor.c);
	}

	@Override
	public void a(PistonHandler state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ENCASED_FLUID_PIPE.create();
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();

		if (world.v)
			return Difficulty.SUCCESS;

		context.p()
			.syncWorldEvent(2001, context.a(), BeetrootsBlock.i(state));
		PistonHandler equivalentPipe = transferSixWayProperties(state, AllBlocks.FLUID_PIPE.getDefaultState());

		Direction firstFound = Direction.UP;
		for (Direction d : Iterate.directions)
			if (state.c(FACING_TO_PROPERTY_MAP.get(d))) {
				firstFound = d;
				break;
			}

		world.a(pos, AllBlocks.FLUID_PIPE.get()
			.updateBlockState(equivalentPipe, firstFound, null, world, pos));
		return Difficulty.SUCCESS;
	}

	public static PistonHandler transferSixWayProperties(PistonHandler from, PistonHandler to) {
		for (Direction d : Iterate.directions) {
			BedPart property = FACING_TO_PROPERTY_MAP.get(d);
			to = to.a(property, from.c(property));
		}
		return to;
	}
	
	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState());
	}

}

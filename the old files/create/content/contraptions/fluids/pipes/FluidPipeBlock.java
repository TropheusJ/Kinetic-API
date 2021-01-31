package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;
import bnx;
import bqx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidPropagator;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.NyliumBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.gen.StructureAccessor;

public class FluidPipeBlock extends NyliumBlock implements SeagrassBlock, IWrenchableWithBracket {

	public FluidPipeBlock(c properties) {
		super(4 / 16f, properties);
		this.j(super.n().a(BambooLeaves.C, false));
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (tryRemoveBracket(context))
			return Difficulty.SUCCESS;

		GameMode world = context.p();
		BlockPos pos = context.a();
		Axis axis = getAxis(world, pos, state);
		if (axis == null)
			return Difficulty.PASS;
		if (context.j()
			.getAxis() == axis)
			return Difficulty.PASS;
		if (!world.v)
			world.a(pos, AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
				.a(GlassFluidPipeBlock.e, axis).a(BambooLeaves.C, state.c(BambooLeaves.C)));
		return Difficulty.SUCCESS;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg hit) {
		if (!AllBlocks.COPPER_CASING.isIn(player.b(hand)))
			return Difficulty.PASS;
		AllTriggers.triggerFor(AllTriggers.CASING_PIPE, player);
		if (!world.v)
			world.a(pos,
				EncasedPipeBlock.transferSixWayProperties(state, AllBlocks.ENCASED_FLUID_PIPE.getDefaultState()));
		return Difficulty.SUCCESS;
	}

	@Nullable
	private Axis getAxis(MobSpawnerLogic world, BlockPos pos, PistonHandler state) {
		return FluidPropagator.getStraightPipeAxis(state);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FLUID_PIPE.create();
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
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		if (world.v)
			return;
		if (state != oldState)
			world.I()
				.a(pos, this, 1, StructureAccessor.c);
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

	@Override
	public void a(PistonHandler state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	public static boolean isPipe(PistonHandler state) {
		return state.b() instanceof FluidPipeBlock;
	}

	public static boolean canConnectTo(bqx world, BlockPos neighbourPos, PistonHandler neighbour, Direction direction) {
		if (FluidPropagator.hasFluidCapability(world, neighbourPos, direction.getOpposite()))
			return true;
		FluidTransportBehaviour transport = TileEntityBehaviour.get(world, neighbourPos, FluidTransportBehaviour.TYPE);
		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, neighbourPos, BracketedTileEntityBehaviour.TYPE);
		if (isPipe(neighbour))
			return bracket == null || !bracket.isBacketPresent()
				|| FluidPropagator.getStraightPipeAxis(neighbour) == direction.getAxis();
		if (transport == null)
			return false;
		return transport.canHaveFlowToward(neighbour, direction.getOpposite());
	}

	public static boolean shouldDrawRim(bqx world, BlockPos pos, PistonHandler state,
		Direction direction) {
		BlockPos offsetPos = pos.offset(direction);
		PistonHandler facingState = world.d_(offsetPos);
		if (!isPipe(facingState))
			return true;
		if (!canConnectTo(world, offsetPos, facingState, direction))
			return true;
		if (!isCornerOrEndPipe(world, pos, state))
			return false;
		if (FluidPropagator.getStraightPipeAxis(facingState) != null)
			return true;
		if (!shouldDrawCasing(world, pos, state) && shouldDrawCasing(world, offsetPos, facingState))
			return true;
		if (isCornerOrEndPipe(world, offsetPos, facingState))
			return direction.getDirection() == AxisDirection.POSITIVE;
		return true;
	}

	public static boolean isOpenAt(PistonHandler state, Direction direction) {
		return state.c(g.get(direction));
	}

	public static boolean isCornerOrEndPipe(bqx world, BlockPos pos, PistonHandler state) {
		return isPipe(state) && FluidPropagator.getStraightPipeAxis(state) == null
			&& !shouldDrawCasing(world, pos, state);
	}

	public static boolean shouldDrawCasing(bqx world, BlockPos pos, PistonHandler state) {
		if (!isPipe(state))
			return false;
		for (Axis axis : Iterate.axes) {
			int connections = 0;
			for (Direction direction : Iterate.directions)
				if (direction.getAxis() != axis && isOpenAt(state, direction))
					connections++;
			if (connections > 2)
				return true;
		}
		return false;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(a, b, c, d, e, f, BambooLeaves.C);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		EmptyFluid FluidState = context.p()
			.b(context.a());
		return updateBlockState(n(), context.d(), null, context.p(),
			context.a()).a(BambooLeaves.C,
				Boolean.valueOf(FluidState.a() == FlowableFluid.c));
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbourState,
		GrassColors world, BlockPos pos, BlockPos neighbourPos) {
		if (state.c(BambooLeaves.C))
			world.H()
				.a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
		if (isOpenAt(state, direction) && neighbourState.b(BambooLeaves.C))
			world.I()
				.a(pos, this, 1, StructureAccessor.c);
		return updateBlockState(state, direction, direction.getOpposite(), world, pos);
	}

	public PistonHandler updateBlockState(PistonHandler state, Direction preferredDirection, @Nullable Direction ignore,
		bqx world, BlockPos pos) {

		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (bracket != null && bracket.isBacketPresent())
			return state;

		// Update sides that are not ignored
		for (Direction d : Iterate.directions)
			if (d != ignore) {
				boolean shouldConnect = canConnectTo(world, pos.offset(d), world.d_(pos.offset(d)), d);
				state = state.a(g.get(d), shouldConnect);
			}

		// See if it has enough connections
		Direction connectedDirection = null;
		for (Direction d : Iterate.directions) {
			if (isOpenAt(state, d)) {
				if (connectedDirection != null)
					return state;
				connectedDirection = d;
			}
		}

		// Add opposite end if only one connection
		if (connectedDirection != null)
			return state.a(g.get(connectedDirection.getOpposite()), true);

		// Use preferred
		return state.a(g.get(preferredDirection), true)
			.a(g.get(preferredDirection.getOpposite()), true);
	}

	@Override
	public EmptyFluid d(PistonHandler state) {
		return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false)
			: FlowableFluid.FALLING.h();
	}

	@Override
	public Optional<ItemCooldownManager> removeBracket(MobSpawnerLogic world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour =
			BracketedTileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		PistonHandler bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == BellBlock.FACING.n())
			return Optional.empty();
		return Optional.of(new ItemCooldownManager(bracket.b()));
	}
}

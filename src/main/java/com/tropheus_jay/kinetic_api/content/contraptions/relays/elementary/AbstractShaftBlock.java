package com.tropheus_jay.kinetic_api.content.contraptions.relays.elementary;

import bnx;
import com.simibubi.kinetic_api.AllTileEntities;
import com.tropheus_jay.kinetic_api.content.contraptions.base.RotatedPillarKineticBlock;
import com.tropheus_jay.kinetic_api.content.contraptions.wrench.IWrenchableWithBracket;
import com.tropheus_jay.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import javafx.util.Builder;
import net.minecraft.block.*;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.*;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.*;

import java.util.Optional;

public abstract class AbstractShaftBlock extends RotatedPillarKineticBlock implements Waterloggable, IWrenchableWithBracket {

	public AbstractShaftBlock(Settings settings) {
		super(settings);
		setDefaultState(super.getDefaultState().with(Properties.WATERLOGGED, false));
	}

	@Override
	public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
		return IWrenchableWithBracket.super.onWrenched(state, context);
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.NORMAL;
	}

	/*@Override todo: KILL ME ALREADY
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return AllTileEntities.SIMPLE_KINETIC.kinetic_api();
	}*/

	@Override
	@SuppressWarnings("deprecation")
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> Block.dropStack(world, pos, stack));
		super.onStateReplaced(state, world, pos, newState, isMoving);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false)
				: Fluids.EMPTY.getDefaultState();
	}

/* todo: why is builder the wrong builder */

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(Properties.WATERLOGGED);
		super.appendProperties(builder);
	}

	@Override
	public BlockState a(BlockState state, Direction direction, BlockState neighbourState,
		GrassColors world, BlockPos pos, BlockPos neighbourPos) {
		if (state.c(BambooLeaves.C)) {
			world.H()
				.a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
		}
		return state;
	}

	@Override
	public BlockState a(PotionUtil context) {
		EmptyFluid ifluidstate = context.p()
			.b(context.a());
		return super.a(context).a(BambooLeaves.C,
			Boolean.valueOf(ifluidstate.a() == FlowableFluid.c));
	}

	@Override
	public Optional<ItemCooldownManager> removeBracket(MobSpawnerLogic world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		BlockState bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == BellBlock.FACING.n())
			return Optional.empty();
		return Optional.of(new ItemCooldownManager(bracket.b()));
	}
}

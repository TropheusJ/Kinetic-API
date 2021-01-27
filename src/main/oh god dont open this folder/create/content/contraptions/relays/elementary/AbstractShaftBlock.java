package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Optional;
import bnx;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public abstract class AbstractShaftBlock extends RotatedPillarKineticBlock implements SeagrassBlock, IWrenchableWithBracket {

	public AbstractShaftBlock(c properties) {
		super(properties);
		j(super.n().a(BambooLeaves.C, false));
	}
	
	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		return IWrenchableWithBracket.super.onWrenched(state, context);
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.SIMPLE_KINETIC.create();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> BeetrootsBlock.a(world, pos, stack));
		super.a(state, world, pos, newState, isMoving);
	}
	
	// IRotate:

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == state.c(AXIS);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(AXIS);
	}

	@Override
	public EmptyFluid d(PistonHandler state) {
		return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false)
			: FlowableFluid.FALLING.h();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(BambooLeaves.C);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbourState,
		GrassColors world, BlockPos pos, BlockPos neighbourPos) {
		if (state.c(BambooLeaves.C)) {
			world.H()
				.a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
		}
		return state;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
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
		PistonHandler bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == BellBlock.FACING.n())
			return Optional.empty();
		return Optional.of(new ItemCooldownManager(bracket.b()));
	}
}

package com.simibubi.kinetic_api.content.logistics.block.funnel;

import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.kinetic_api.content.schematics.ItemRequirement;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.VoxelShaper;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BeltFunnelBlock extends HayBlock implements IWrenchable, ISpecialBlockItemRequirement {

	private BlockEntry<? extends FunnelBlock> parent;

	public static final BedPart POWERED = BambooLeaves.w;
	public static final DirectionProperty<Shape> SHAPE = DirectionProperty.a("shape", Shape.class);

	public enum Shape implements SmoothUtil {
		RETRACTED(AllShapes.BELT_FUNNEL_RETRACTED),
		EXTENDED(AllShapes.BELT_FUNNEL_EXTENDED),
		PUSHING(AllShapes.BELT_FUNNEL_PERPENDICULAR),
		PULLING(AllShapes.BELT_FUNNEL_PERPENDICULAR);
//		CONNECTED(AllShapes.BELT_FUNNEL_CONNECTED); 

		VoxelShaper shaper;

		private Shape(VoxelShaper shaper) {
			this.shaper = shaper;
		}

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	public BeltFunnelBlock(BlockEntry<? extends FunnelBlock> parent, c p_i48377_1_) {
		super(p_i48377_1_);
		this.parent = parent;
		PistonHandler defaultState = n().a(SHAPE, Shape.RETRACTED);
		if (hasPoweredProperty())
			defaultState = defaultState.a(POWERED, false);
		j(defaultState);
	}

	public abstract boolean hasPoweredProperty();

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FUNNEL.create();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		if (hasPoweredProperty())
			p_206840_1_.a(POWERED);
		super.a(p_206840_1_.a(aq, SHAPE));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return state.c(SHAPE).shaper.get(state.c(aq));
	}

	@Override
	public PistonHandler a(PotionUtil ctx) {
		PistonHandler stateForPlacement = super.a(ctx);
		BlockPos pos = ctx.a();
		GameMode world = ctx.p();
		Direction facing = ctx.n() == null || ctx.n()
			.bt() ? ctx.j()
				: ctx.d()
					.getOpposite();

		if (hasPoweredProperty())
			stateForPlacement = stateForPlacement.a(POWERED, world.r(pos));

		PistonHandler state = stateForPlacement.a(aq, facing);
		return state.a(SHAPE, getShapeForPosition(world, pos, facing));
	}

	public static Shape getShapeForPosition(MobSpawnerLogic world, BlockPos pos, Direction facing) {
		BlockPos posBelow = pos.down();
		PistonHandler stateBelow = world.d_(posBelow);
		if (!AllBlocks.BELT.has(stateBelow))
			return Shape.PUSHING;
		Direction movementFacing = stateBelow.c(BeltBlock.HORIZONTAL_FACING);
		return movementFacing.getAxis() != facing.getAxis() ? Shape.PUSHING : Shape.RETRACTED;
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.hasTileEntity() && (state.b() != newState.b() && !FunnelBlock.isFunnel(newState)
			|| !newState.hasTileEntity())) {
			TileEntityBehaviour.destroy(world, pos, FilteringBehaviour.TYPE);
			world.o(pos);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean addDestroyEffects(PistonHandler state, GameMode world, BlockPos pos, ItemPickupParticle manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
		PlayerAbilities player) {
		return parent.asStack();
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbour, GrassColors world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (!isOnValidBelt(state, world, pos)) {
			PistonHandler parentState = parent.getDefaultState();
			if (state.d(POWERED).orElse(false))
				parentState = parentState.a(POWERED, true);
			return parentState.a(FunnelBlock.SHAPE, state.c(aq));
		}
		Shape updatedShape = getShapeForPosition(world, pos, state.c(aq));
		Shape currentShape = state.c(SHAPE);
		if (updatedShape == currentShape)
			return state;

		// Don't revert wrenched states
		if (updatedShape == Shape.PUSHING && currentShape == Shape.PULLING)
			return state;
		if (updatedShape == Shape.RETRACTED && currentShape == Shape.EXTENDED)
			return state;

		return state.a(SHAPE, updatedShape);
	}

	public static boolean isOnValidBelt(PistonHandler state, ItemConvertible world, BlockPos pos) {
		PistonHandler stateBelow = world.d_(pos.down());
		if ((stateBelow.b() instanceof BeltBlock))
			return BeltBlock.canTransportObjects(stateBelow);
		DirectBeltInputBehaviour directBeltInputBehaviour =
			TileEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);
		if (directBeltInputBehaviour == null)
			return false;
		return directBeltInputBehaviour.canSupportBeltFunnels();

	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (!hasPoweredProperty())
			return;
		if (worldIn.v)
			return;
		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos))
			worldIn.a(pos, state.a(POWERED), 2);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		if (world.v)
			return Difficulty.SUCCESS;

		Shape shape = state.c(SHAPE);
		Shape newShape = shape;
		if (shape == Shape.PULLING)
			newShape = Shape.PUSHING;
		else if (shape == Shape.PUSHING)
			newShape = Shape.PULLING;
		else if (shape == Shape.EXTENDED)
			newShape = Shape.RETRACTED;
		else if (shape == Shape.RETRACTED) {
			PistonHandler belt = world.d_(context.a()
				.down());
			if (belt.b() instanceof BeltBlock && belt.c(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
				newShape = Shape.RETRACTED;
			else
				newShape = Shape.EXTENDED;
		}

		if (newShape == shape)
			return Difficulty.SUCCESS;

		world.a(context.a(), state.a(SHAPE, newShape));

		if (newShape == Shape.EXTENDED) {
			Direction facing = state.c(aq);
			PistonHandler opposite = world.d_(context.a()
				.offset(facing));
			if (opposite.b() instanceof BeltFunnelBlock && opposite.c(SHAPE) == Shape.EXTENDED
				&& opposite.c(aq) == facing.getOpposite())
				AllTriggers.triggerFor(AllTriggers.BELT_FUNNEL_KISS, context.n());
		}
		return Difficulty.SUCCESS;
	}
	
	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		return ItemRequirement.of(parent.getDefaultState());
	}

}

package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class BeltTunnelBlock extends BeetrootsBlock implements ITE<BeltTunnelTileEntity>, IWrenchable {

	public static final IntProperty<Shape> SHAPE = DirectionProperty.a("shape", Shape.class);
	public static final IntProperty<Axis> HORIZONTAL_AXIS = BambooLeaves.E;

	public BeltTunnelBlock(c properties) {
		super(properties);
		j(n().a(SHAPE, Shape.STRAIGHT));
	}

	public enum Shape implements SmoothUtil {
		STRAIGHT, WINDOW, CLOSED, T_LEFT, T_RIGHT, CROSS;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return BeltTunnelShapes.getShape(state);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.ANDESITE_TUNNEL.create();
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		PistonHandler blockState = worldIn.d_(pos.down());
		if (!isValidPositionForPlacement(state, worldIn, pos))
			return false;
		if (!blockState.c(BeltBlock.CASING))
			return false;
		return true;
	}

	public boolean isValidPositionForPlacement(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		PistonHandler blockState = worldIn.d_(pos.down());
		if (!AllBlocks.BELT.has(blockState))
			return false;
		if (blockState.c(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return false;
		return true;
	}

	public static boolean hasWindow(PistonHandler state) {
		return state.c(SHAPE) == Shape.WINDOW || state.c(SHAPE) == Shape.CLOSED;
	}

	public static boolean isStraight(PistonHandler state) {
		return hasWindow(state) || state.c(SHAPE) == Shape.STRAIGHT;
	}
	
	public static boolean isJunction(PistonHandler state) {
		Shape shape = state.c(SHAPE);
		return shape == Shape.CROSS || shape == Shape.T_LEFT || shape == Shape.T_RIGHT;
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return getTunnelState(context.p(), context.a());
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler p_220082_4_, boolean p_220082_5_) {
		if (!(world instanceof WrappedWorld) && !world.isClient())
			withTileEntityDo(world, pos, BeltTunnelTileEntity::updateTunnelConnections);
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction facing, PistonHandler facingState, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing.getAxis()
			.isVertical())
			return state;
		if (!(worldIn instanceof WrappedWorld) && !worldIn.s_())
			withTileEntityDo(worldIn, currentPos, BeltTunnelTileEntity::updateTunnelConnections);
		PistonHandler tunnelState = getTunnelState(worldIn, currentPos);
		if (tunnelState.c(HORIZONTAL_AXIS) == state.c(HORIZONTAL_AXIS)) {
			if (hasWindow(tunnelState) == hasWindow(state))
				return state;
		}

		return tunnelState;
	}

	public void updateTunnel(GrassColors world, BlockPos pos) {
		PistonHandler tunnel = world.d_(pos);
		PistonHandler newTunnel = getTunnelState(world, pos);
		if (tunnel != newTunnel) {
			world.a(pos, newTunnel, 3);
			BeehiveBlockEntity te = world.c(pos);
			if (te != null && (te instanceof BeltTunnelTileEntity))
				((BeltTunnelTileEntity) te).updateTunnelConnections();
		}
	}

	private PistonHandler getTunnelState(MobSpawnerLogic reader, BlockPos pos) {
		PistonHandler state = n();
		PistonHandler belt = reader.d_(pos.down());
		if (AllBlocks.BELT.has(belt))
			state = state.a(HORIZONTAL_AXIS, belt.c(BeltBlock.HORIZONTAL_FACING)
				.getAxis());
		Axis axis = state.c(HORIZONTAL_AXIS);

		// T and Cross
		Direction left = Direction.get(AxisDirection.POSITIVE, axis)
			.rotateYClockwise();
		boolean onLeft = hasValidOutput(reader, pos.down(), left);
		boolean onRight = hasValidOutput(reader, pos.down(), left.getOpposite());

		if (onLeft && onRight)
			state = state.a(SHAPE, Shape.CROSS);
		else if (onLeft)
			state = state.a(SHAPE, Shape.T_LEFT);
		else if (onRight)
			state = state.a(SHAPE, Shape.T_RIGHT);

		if (state.c(SHAPE) == Shape.STRAIGHT) {
			boolean canHaveWindow = canHaveWindow(reader, pos, axis);
			if (canHaveWindow)
				state = state.a(SHAPE, Shape.WINDOW);
		}

		return state;
	}

	protected boolean canHaveWindow(MobSpawnerLogic reader, BlockPos pos, Axis axis) {
		Direction fw = Direction.get(AxisDirection.POSITIVE, axis);
		PistonHandler blockState1 = reader.d_(pos.offset(fw));
		PistonHandler blockState2 = reader.d_(pos.offset(fw.getOpposite()));
		
		boolean funnel1 = blockState1.b() instanceof BeltFunnelBlock
			&& blockState1.c(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState1.c(BeltFunnelBlock.aq) == fw.getOpposite();
		boolean funnel2 = blockState2.b() instanceof BeltFunnelBlock
			&& blockState2.c(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState2.c(BeltFunnelBlock.aq) == fw;
		
		boolean valid1 = blockState1.b() instanceof BeltTunnelBlock || funnel1;
		boolean valid2 = blockState2.b() instanceof BeltTunnelBlock || funnel2;
		boolean canHaveWindow = valid1 && valid2 && !(funnel1 && funnel2);
		return canHaveWindow;
	}

	private boolean hasValidOutput(MobSpawnerLogic world, BlockPos pos, Direction side) {
		PistonHandler blockState = world.d_(pos.offset(side));
		if (AllBlocks.BELT.has(blockState))
			return blockState.c(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == side.getAxis();
		DirectBeltInputBehaviour behaviour =
			TileEntityBehaviour.get(world, pos.offset(side), DirectBeltInputBehaviour.TYPE);
		return behaviour != null && behaviour.canInsertFromSide(side);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (!hasWindow(state))
			return Difficulty.PASS;

		// Toggle windows
		Shape shape = state.c(SHAPE);
		shape = shape == Shape.CLOSED ? Shape.WINDOW : Shape.CLOSED;
		GameMode world = context.p();
		if (!world.v)
			world.a(context.a(), state.a(SHAPE, shape), 2);
		return Difficulty.SUCCESS;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.v)
			return;

		if (fromPos.equals(pos.down())) {
			if (!a(state, worldIn, pos)) {
				worldIn.b(pos, true);
				return;
			}
		}
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(HORIZONTAL_AXIS, SHAPE);
		super.a(builder);
	}

	@Override
	public Class<BeltTunnelTileEntity> getTileEntityClass() {
		return BeltTunnelTileEntity.class;
	}

}

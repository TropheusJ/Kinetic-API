package com.simibubi.kinetic_api.content.contraptions.components.flywheel;

import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class FlywheelBlock extends HorizontalKineticBlock {

	public static DirectionProperty<ConnectionState> CONNECTION = DirectionProperty.a("connection", ConnectionState.class);

	public FlywheelBlock(c properties) {
		super(properties);
		j(n().a(CONNECTION, ConnectionState.NONE));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(CONNECTION));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FLYWHEEL.create();
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (preferred != null)
			return n().a(HORIZONTAL_FACING, preferred.getOpposite());
		return this.n().a(HORIZONTAL_FACING, context.f());
	}

	public static boolean isConnected(PistonHandler state) {
		return getConnection(state) != null;
	}

	public static Direction getConnection(PistonHandler state) {
		Direction facing = state.c(HORIZONTAL_FACING);
		ConnectionState connection = state.c(CONNECTION);

		if (connection == ConnectionState.LEFT)
			return facing.rotateYCounterclockwise();
		if (connection == ConnectionState.RIGHT)
			return facing.rotateYClockwise();
		return null;
	}

	public static void setConnection(GameMode world, BlockPos pos, PistonHandler state, Direction direction) {
		Direction facing = state.c(HORIZONTAL_FACING);
		ConnectionState connection = ConnectionState.NONE;

		if (direction == facing.rotateYClockwise())
			connection = ConnectionState.RIGHT;
		if (direction == facing.rotateYCounterclockwise())
			connection = ConnectionState.LEFT;

		world.a(pos, state.a(CONNECTION, connection), 18);
		AllTriggers.triggerForNearbyPlayers(AllTriggers.FLYWHEEL, world, pos, 4);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING).getAxis();
	}

	public enum ConnectionState implements SmoothUtil {
		NONE, LEFT, RIGHT;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

}

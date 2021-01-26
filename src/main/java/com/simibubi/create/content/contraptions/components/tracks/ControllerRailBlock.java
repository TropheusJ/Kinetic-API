package com.simibubi.create.content.contraptions.components.tracks;

import static net.minecraft.block.enums.Instrument.NORTH_SOUTH;

import afj;
import bnx;
import cef;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ControllerRailBlock extends BlockWithEntity implements IWrenchable {

	public static final DirectionProperty<Instrument> SHAPE = BambooLeaves.ad;
	public static final BedPart BACKWARDS = BedPart.a("backwards");
	public static final DoubleBlockHalf POWER = BambooLeaves.az;

	public ControllerRailBlock(c properties) {
		super(true, properties);
		this.j(this.n.b()
			.a(POWER, 0)
			.a(BACKWARDS, false)
			.a(SHAPE, NORTH_SOUTH));
	}

	public static Vec3i getAccelerationVector(PistonHandler state) {
		Direction pointingTo = getPointingTowards(state);
		return (isStateBackwards(state) ? pointingTo.getOpposite() : pointingTo).getVector();
	}

	private static Direction getPointingTowards(PistonHandler state) {
		switch (state.c(SHAPE)) {
		case ASCENDING_WEST:
		case EAST_WEST:
			return Direction.WEST;
		case ASCENDING_EAST:
			return Direction.EAST;
		case ASCENDING_SOUTH:
			return Direction.SOUTH;
		default:
			return Direction.NORTH;
		}
	}

	@Override
	protected PistonHandler a(GameMode world, BlockPos pos, PistonHandler state, boolean p_208489_4_) {
		PistonHandler updatedState = super.a(world, pos, state, p_208489_4_);
		if (updatedState.c(SHAPE) == state.c(SHAPE))
			return updatedState;
		PistonHandler reversedUpdatedState = updatedState;

		// Rails snapping to others at 90 degrees should follow their direction
		if (getPointingTowards(state).getAxis() != getPointingTowards(updatedState).getAxis()) {
			for (boolean opposite : Iterate.trueAndFalse) {
				Direction offset = getPointingTowards(updatedState);
				if (opposite)
					offset = offset.getOpposite();
				for (BlockPos adjPos : Iterate.hereBelowAndAbove(pos.offset(offset))) {
					PistonHandler adjState = world.d_(adjPos);
					if (!AllBlocks.CONTROLLER_RAIL.has(adjState))
						continue;
					if (getPointingTowards(adjState).getAxis() != offset.getAxis())
						continue;
					if (adjState.c(BACKWARDS) != reversedUpdatedState.c(BACKWARDS))
						reversedUpdatedState = reversedUpdatedState.a(BACKWARDS);
				}
			}
		}

		// Replace if changed
		if (reversedUpdatedState != updatedState)
			world.a(pos, reversedUpdatedState);
		return reversedUpdatedState;
	}

	private static void decelerateCart(BlockPos pos, ScheduleBuilder cart) {
		EntityHitResult diff = VecHelper.getCenterOf(pos)
			.d(cart.cz());
		cart.n(diff.entity / 16f, 0, diff.d / 16f);

		if (cart instanceof MinecartEntity) {
			MinecartEntity fme = (MinecartEntity) cart;
			fme.b = fme.c = 0;
		}
	}

	private static boolean isStableWith(PistonHandler testState, MobSpawnerLogic world, BlockPos pos) {
		return c(world, pos.down()) && (!testState.c(SHAPE)
			.c() || c(world, pos.offset(getPointingTowards(testState))));
	}

	@Override
	public PistonHandler a(PotionUtil p_196258_1_) {
		Direction direction = p_196258_1_.f();
		PistonHandler base = super.a(p_196258_1_);
		return (base == null ? n() : base).a(BACKWARDS,
			direction.getDirection() == AxisDirection.POSITIVE);
	}

	@Override
	public IntProperty<Instrument> d() {
		return SHAPE;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		p_206840_1_.a(SHAPE, POWER, BACKWARDS);
	}

	@Override
	public void onMinecartPass(PistonHandler state, GameMode world, BlockPos pos, ScheduleBuilder cart) {
		if (world.v)
			return;
		EntityHitResult accelerationVec = EntityHitResult.b(getAccelerationVector(state));
		double targetSpeed = cart.getMaxSpeedWithRail() * state.c(POWER) / 15f;

		if (cart instanceof MinecartEntity) {
			MinecartEntity fme = (MinecartEntity) cart;
			fme.b = accelerationVec.entity;
			fme.c = accelerationVec.d;
		}

		EntityHitResult motion = cart.cB();
		if ((motion.b(accelerationVec) >= 0 || motion.g() < 0.0001) && targetSpeed > 0)
			cart.f(accelerationVec.a(targetSpeed));
		else
			decelerateCart(pos, cart);
	}

	@Override
	protected void a(PistonHandler state, GameMode world, BlockPos pos, BeetrootsBlock block) {
		int newPower = calculatePower(world, pos);
		if (state.c(POWER) != newPower)
			placeAndNotify(state.a(POWER, newPower), pos, world);
	}

	private int calculatePower(GameMode world, BlockPos pos) {
		int newPower = world.s(pos);
		if (newPower != 0)
			return newPower;

		int forwardDistance = 0;
		int backwardsDistance = 0;
		BlockPos lastForwardRail = pos;
		BlockPos lastBackwardsRail = pos;
		int forwardPower = 0;
		int backwardsPower = 0;

		for (int i = 0; i < 15; i++) {
			BlockPos testPos = findNextRail(lastForwardRail, world, false);
			if (testPos == null)
				break;
			forwardDistance++;
			lastForwardRail = testPos;
			forwardPower = world.s(testPos);
			if (forwardPower != 0)
				break;
		}
		for (int i = 0; i < 15; i++) {
			BlockPos testPos = findNextRail(lastBackwardsRail, world, true);
			if (testPos == null)
				break;
			backwardsDistance++;
			lastBackwardsRail = testPos;
			backwardsPower = world.s(testPos);
			if (backwardsPower != 0)
				break;
		}

		if (forwardDistance > 8 && backwardsDistance > 8)
			return 0;
		if (backwardsPower == 0 && forwardDistance <= 8)
			return forwardPower;
		if (forwardPower == 0 && backwardsDistance <= 8)
			return backwardsPower;
		if (backwardsPower != 0 && forwardPower != 0)
			return afj.f((backwardsPower * forwardDistance + forwardPower * backwardsDistance)
				/ (double) (forwardDistance + backwardsDistance));
		return 0;
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		if (world.v)
			return Difficulty.SUCCESS;
		BlockPos pos = context.a();
		for (RespawnAnchorBlock testRotation : new RespawnAnchorBlock[] { RespawnAnchorBlock.field_26442, RespawnAnchorBlock.field_26443,
			RespawnAnchorBlock.d }) {
			PistonHandler testState = a(state, testRotation);
			if (isStableWith(testState, world, pos)) {
				placeAndNotify(testState, pos, world);
				break;
			}
		}
		return Difficulty.SUCCESS;
	}

	@Override
	public Difficulty onSneakWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		PistonHandler testState = state.a(BACKWARDS, !state.c(BACKWARDS));
		if (isStableWith(testState, world, pos))
			placeAndNotify(testState, pos, world);
		return Difficulty.SUCCESS;
	}

	private void placeAndNotify(PistonHandler state, BlockPos pos, GameMode world) {
		world.a(pos, state, 3);
		world.b(pos.down(), this);
		if (state.c(SHAPE)
			.c())
			world.b(pos.up(), this);
	}

	@Nullable
	private BlockPos findNextRail(BlockPos from, MobSpawnerLogic world, boolean reversed) {
		PistonHandler current = world.d_(from);
		if (!(current.b() instanceof ControllerRailBlock))
			return null;
		Vec3i accelerationVec = getAccelerationVector(current);
		BlockPos baseTestPos = reversed ? from.subtract(accelerationVec) : from.add(accelerationVec);
		for (BlockPos testPos : Iterate.hereBelowAndAbove(baseTestPos)) {
			if (testPos.getY() > from.getY() && !current.c(SHAPE)
				.c())
				continue;
			PistonHandler testState = world.d_(testPos);
			if (testState.b() instanceof ControllerRailBlock
				&& getAccelerationVector(testState).equals(accelerationVec))
				return testPos;
		}
		return null;
	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler state, GameMode world, BlockPos pos) {
		return state.c(POWER);
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rotation) {
		if (rotation == RespawnAnchorBlock.CHARGES)
			return state;

		Instrument railshape = BellBlock.aN.n()
			.a(SHAPE, state.c(SHAPE))
			.a(rotation)
			.c(SHAPE);
		state = state.a(SHAPE, railshape);

		if (rotation == RespawnAnchorBlock.field_26443
			|| (getPointingTowards(state).getAxis() == Axis.Z) == (rotation == RespawnAnchorBlock.d))
			return state.a(BACKWARDS);

		return state;
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirror) {
		if (mirror == LoomBlock.TITLE)
			return state;

		Instrument railshape = BellBlock.aN.n()
			.a(SHAPE, state.c(SHAPE))
			.a(mirror)
			.c(SHAPE);
		state = state.a(SHAPE, railshape);

		if ((getPointingTowards(state).getAxis() == Axis.Z) == (mirror == LoomBlock.b))
			return state.a(BACKWARDS);

		return state;
	}

	public static boolean isStateBackwards(PistonHandler state) {
		return state.c(BACKWARDS) ^ isReversedSlope(state);
	}

	public static boolean isReversedSlope(PistonHandler state) {
		return state.c(SHAPE) == Instrument.ASCENDING_SOUTH || state.c(SHAPE) == Instrument.ASCENDING_EAST;
	}
}

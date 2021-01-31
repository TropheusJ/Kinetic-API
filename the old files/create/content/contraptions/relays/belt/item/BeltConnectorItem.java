package com.simibubi.kinetic_api.content.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltPart;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class BeltConnectorItem extends BannerItem {

	public BeltConnectorItem(a properties) {
		super(AllBlocks.BELT.get(), properties);
	}

	@Override
	public String a() {
		return m();
	}

	@Override
	public void a(ChorusFruitItem p_150895_1_, DefaultedList<ItemCooldownManager> p_150895_2_) {
		if (p_150895_1_ == Create.baseCreativeTab)
			return;
		super.a(p_150895_1_, p_150895_2_);
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		PlayerAbilities playerEntity = context.n();
		if (playerEntity != null && playerEntity.bt()) {
			context.m()
				.c(null);
			return Difficulty.SUCCESS;
		}

		GameMode world = context.p();
		BlockPos pos = context.a();
		boolean validAxis = validateAxis(world, pos);

		if (world.v)
			return validAxis ? Difficulty.SUCCESS : Difficulty.FAIL;

		CompoundTag tag = context.m()
			.p();
		BlockPos firstPulley = null;

		// Remove first if no longer existant or valid
		if (tag.contains("FirstPulley")) {
			firstPulley = NbtHelper.toBlockPos(tag.getCompound("FirstPulley"));
			if (!validateAxis(world, firstPulley) || !firstPulley.isWithinDistance(pos, maxLength() * 2)) {
				tag.remove("FirstPulley");
				context.m()
					.c(tag);
			}
		}

		if (!validAxis || playerEntity == null)
			return Difficulty.FAIL;

		if (tag.contains("FirstPulley")) {

			if (!canConnect(world, firstPulley, pos))
				return Difficulty.FAIL;

			if (firstPulley != null && !firstPulley.equals(pos)) {
				createBelts(world, firstPulley, pos);
				AllTriggers.triggerFor(AllTriggers.CONNECT_BELT, playerEntity);
				if (!playerEntity.b_())
					context.m()
						.g(1);
			}

			if (!context.m()
				.a()) {
				context.m()
					.c(null);
				playerEntity.eS()
					.a(this, 5);
			}
			return Difficulty.SUCCESS;
		}

		tag.put("FirstPulley", NbtHelper.fromBlockPos(pos));
		context.m()
			.c(tag);
		playerEntity.eS()
			.a(this, 5);
		return Difficulty.SUCCESS;
	}

	public static void createBelts(GameMode world, BlockPos start, BlockPos end) {

		BeltSlope slope = getSlopeBetween(start, end);
		Direction facing = getFacingFromTo(start, end);

		BlockPos diff = end.subtract(start);
		if (diff.getX() == diff.getZ())
			facing = Direction.get(facing.getDirection(), world.d_(start)
				.c(BambooLeaves.F) == Axis.X ? Axis.Z : Axis.X);

		List<BlockPos> beltsToCreate = getBeltChainBetween(start, end, slope, facing);
		PistonHandler beltBlock = AllBlocks.BELT.getDefaultState();

		for (BlockPos pos : beltsToCreate) {
			BeltPart part = pos.equals(start) ? BeltPart.START : pos.equals(end) ? BeltPart.END : BeltPart.MIDDLE;
			PistonHandler shaftState = world.d_(pos);
			boolean pulley = ShaftBlock.isShaft(shaftState);
			if (part == BeltPart.MIDDLE && pulley)
				part = BeltPart.PULLEY;
			if (pulley && shaftState.c(AbstractShaftBlock.AXIS) == Axis.Y)
				slope = BeltSlope.SIDEWAYS;
			world.a(pos, beltBlock.a(BeltBlock.SLOPE, slope)
				.a(BeltBlock.PART, part)
				.a(BeltBlock.HORIZONTAL_FACING, facing), 3);
		}
	}

	private static Direction getFacingFromTo(BlockPos start, BlockPos end) {
		Axis beltAxis = start.getX() == end.getX() ? Axis.Z : Axis.X;
		BlockPos diff = end.subtract(start);
		AxisDirection axisDirection = AxisDirection.POSITIVE;

		if (diff.getX() == 0 && diff.getZ() == 0)
			axisDirection = diff.getY() > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		else
			axisDirection = beltAxis.choose(diff.getX(), 0, diff.getZ()) > 0 ? AxisDirection.POSITIVE
				: AxisDirection.NEGATIVE;

		return Direction.get(axisDirection, beltAxis);
	}

	private static BeltSlope getSlopeBetween(BlockPos start, BlockPos end) {
		BlockPos diff = end.subtract(start);

		if (diff.getY() != 0) {
			if (diff.getZ() != 0 || diff.getX() != 0)
				return diff.getY() > 0 ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
			return BeltSlope.VERTICAL;
		}
		return BeltSlope.HORIZONTAL;
	}

	private static List<BlockPos> getBeltChainBetween(BlockPos start, BlockPos end, BeltSlope slope, Direction direction) {
		List<BlockPos> positions = new LinkedList<>();
		int limit = 1000;
		BlockPos current = start;

		do {
			positions.add(current);

			if (slope == BeltSlope.VERTICAL) {
				current = current.up(direction.getDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}

			current = current.offset(direction);
			if (slope != BeltSlope.HORIZONTAL)
				current = current.up(slope == BeltSlope.UPWARD ? 1 : -1);

		} while (!current.equals(end) && limit-- > 0);

		positions.add(end);
		return positions;
	}

	public static boolean canConnect(GameMode world, BlockPos first, BlockPos second) {
		if (!world.isAreaLoaded(first, 1))
			return false;
		if (!world.isAreaLoaded(second, 1))
			return false;
		if (!second.isWithinDistance(first, maxLength()))
			return false;

		BlockPos diff = second.subtract(first);
		Axis shaftAxis = world.d_(first)
			.c(BambooLeaves.F);

		int x = diff.getX();
		int y = diff.getY();
		int z = diff.getZ();
		int sames = ((Math.abs(x) == Math.abs(y)) ? 1 : 0) + ((Math.abs(y) == Math.abs(z)) ? 1 : 0)
			+ ((Math.abs(z) == Math.abs(x)) ? 1 : 0);

		if (shaftAxis.choose(x, y, z) != 0)
			return false;
		if (sames != 1)
			return false;
		if (shaftAxis != world.d_(second)
			.c(BambooLeaves.F))
			return false;
		if (shaftAxis == Axis.Y && x != 0 && z != 0)
			return false;

		BeehiveBlockEntity tileEntity = world.c(first);
		BeehiveBlockEntity tileEntity2 = world.c(second);

		if (!(tileEntity instanceof KineticTileEntity))
			return false;
		if (!(tileEntity2 instanceof KineticTileEntity))
			return false;

		float speed1 = ((KineticTileEntity) tileEntity).getTheoreticalSpeed();
		float speed2 = ((KineticTileEntity) tileEntity2).getTheoreticalSpeed();
		if (Math.signum(speed1) != Math.signum(speed2) && speed1 != 0 && speed2 != 0)
			return false;

		BlockPos step = new BlockPos(Math.signum(diff.getX()), Math.signum(diff.getY()), Math.signum(diff.getZ()));
		int limit = 1000;
		for (BlockPos currentPos = first.add(step); !currentPos.equals(second) && limit-- > 0; currentPos =
			currentPos.add(step)) {
			PistonHandler blockState = world.d_(currentPos);
			if (ShaftBlock.isShaft(blockState) && blockState.c(AbstractShaftBlock.AXIS) == shaftAxis)
				continue;
			if (!blockState.c()
				.e())
				return false;
		}

		return true;

	}

	protected static Integer maxLength() {
		return AllConfigs.SERVER.kinetics.maxBeltLength.get();
	}

	public static boolean validateAxis(GameMode world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 1))
			return false;
		if (!ShaftBlock.isShaft(world.d_(pos)))
			return false;
		return true;
	}

}

package com.simibubi.kinetic_api.content.contraptions.components.crafter;

import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.Pointing;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalCrafterBlock extends HorizontalKineticBlock implements ITE<MechanicalCrafterTileEntity> {

	public static final DirectionProperty<Pointing> POINTING = DirectionProperty.a("pointing", Pointing.class);

	public MechanicalCrafterBlock(c properties) {
		super(properties);
		j(n().a(POINTING, Pointing.UP));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(POINTING));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_CRAFTER.create();
	}

	@Override
	public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
		return true;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Direction face = context.j();
		BlockPos placedOnPos = context.a()
			.offset(face.getOpposite());
		PistonHandler blockState = context.p()
			.d_(placedOnPos);

		if ((blockState.b() != this) || (context.n() != null && context.n()
			.bt())) {
			PistonHandler stateForPlacement = super.a(context);
			Direction direction = stateForPlacement.c(HORIZONTAL_FACING);
			if (direction != face)
				stateForPlacement = stateForPlacement.a(POINTING, pointingFromFacing(face, direction));
			return stateForPlacement;
		}

		Direction otherFacing = blockState.c(HORIZONTAL_FACING);
		Pointing pointing = pointingFromFacing(face, otherFacing);
		return n().a(HORIZONTAL_FACING, otherFacing)
			.a(POINTING, pointing);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.b() == newState.b()) {
			if (getTargetDirection(state) != getTargetDirection(newState)) {
				MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
				if (crafter != null)
					crafter.blockChanged();
			}
		}

		if (state.hasTileEntity() && state.b() != newState.b()) {
			MechanicalCrafterTileEntity crafter = CrafterHelper.getCrafter(worldIn, pos);
			if (crafter != null) {
				if (crafter.covered)
					BeetrootsBlock.a(worldIn, pos, AllItems.CRAFTER_SLOT_COVER.asStack());
				crafter.ejectWholeGrid();
			}

			for (Direction direction : Iterate.directions) {
				if (direction.getAxis() == state.c(HORIZONTAL_FACING)
					.getAxis())
					continue;

				BlockPos otherPos = pos.offset(direction);
				ConnectedInput thisInput = CrafterHelper.getInput(worldIn, pos);
				ConnectedInput otherInput = CrafterHelper.getInput(worldIn, otherPos);

				if (thisInput == null || otherInput == null)
					continue;
				if (!pos.add(thisInput.data.get(0))
					.equals(otherPos.add(otherInput.data.get(0))))
					continue;

				ConnectedInputHandler.toggleConnection(worldIn, pos, otherPos);
			}

			worldIn.o(pos);
		}
	}

	public static Pointing pointingFromFacing(Direction pointingFace, Direction blockFacing) {
		boolean positive = blockFacing.getDirection() == AxisDirection.POSITIVE;

		Pointing pointing = pointingFace == Direction.DOWN ? Pointing.UP : Pointing.DOWN;
		if (pointingFace == Direction.EAST)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.WEST)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		if (pointingFace == Direction.NORTH)
			pointing = positive ? Pointing.LEFT : Pointing.RIGHT;
		if (pointingFace == Direction.SOUTH)
			pointing = positive ? Pointing.RIGHT : Pointing.LEFT;
		return pointing;
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (context.j() == state.c(HORIZONTAL_FACING)) {
			if (!context.p().v)
				KineticTileEntity.switchToBlockState(context.p(), context.a(), state.a(POINTING));
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager heldItem = player.b(handIn);
		boolean isHand = heldItem.a() && handIn == ItemScatterer.RANDOM;

		BeehiveBlockEntity te = worldIn.c(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return Difficulty.PASS;
		MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
		boolean wrenched = AllItems.WRENCH.isIn(heldItem);

		if (AllBlocks.MECHANICAL_ARM.isIn(heldItem))
			return Difficulty.PASS;
		
		if (hit.b() == state.c(HORIZONTAL_FACING)) {

			if (crafter.phase != Phase.IDLE && !wrenched) {
				crafter.ejectWholeGrid();
				return Difficulty.SUCCESS;
			}

			if (crafter.phase == Phase.IDLE && !isHand && !wrenched) {
				if (worldIn.v)
					return Difficulty.SUCCESS;

				if (AllItems.CRAFTER_SLOT_COVER.isIn(heldItem)) {
					if (crafter.covered)
						return Difficulty.PASS;
					if (!crafter.inventory.c())
						return Difficulty.PASS;
					crafter.covered = true;
					crafter.X_();
					crafter.sendData();
					if (!player.b_())
						heldItem.g(1);
					return Difficulty.SUCCESS;
				}

				LazyOptional<IItemHandler> capability =
					crafter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if (!capability.isPresent())
					return Difficulty.PASS;
				ItemCooldownManager remainder =
					ItemHandlerHelper.insertItem(capability.orElse(new ItemStackHandler()), heldItem.i(), false);
				if (remainder.E() != heldItem.E())
					player.a(handIn, remainder);
				return Difficulty.SUCCESS;
			}

			ItemCooldownManager inSlot = crafter.getInventory().a(0);
			if (inSlot.a()) {
				if (crafter.covered && !wrenched) {
					if (worldIn.v)
						return Difficulty.SUCCESS;
					crafter.covered = false;
					crafter.X_();
					crafter.sendData();
					if (!player.b_())
						player.bm.a(worldIn, AllItems.CRAFTER_SLOT_COVER.asStack());
					return Difficulty.SUCCESS;
				}
				return Difficulty.PASS;
			}
			if (!isHand && !ItemHandlerHelper.canItemStacksStack(heldItem, inSlot))
				return Difficulty.PASS;
			if (worldIn.v)
				return Difficulty.SUCCESS;
			player.bm.a(worldIn, inSlot);
			crafter.getInventory().setStackInSlot(0, ItemCooldownManager.tick);
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	public static Direction getTargetDirection(PistonHandler state) {
		if (!AllBlocks.MECHANICAL_CRAFTER.has(state))
			return Direction.UP;
		Direction facing = state.c(HORIZONTAL_FACING);
		Pointing point = state.c(POINTING);
		EntityHitResult targetVec = new EntityHitResult(0, 1, 0);
		targetVec = VecHelper.rotate(targetVec, -point.getXRotation(), Axis.Z);
		targetVec = VecHelper.rotate(targetVec, AngleHelper.horizontalAngle(facing), Axis.Y);
		return Direction.getFacing(targetVec.entity, targetVec.c, targetVec.d);
	}

	public static boolean isValidTarget(GameMode world, BlockPos targetPos, PistonHandler crafterState) {
		PistonHandler targetState = world.d_(targetPos);
		if (!world.p(targetPos))
			return false;
		if (!AllBlocks.MECHANICAL_CRAFTER.has(targetState))
			return false;
		if (crafterState.c(HORIZONTAL_FACING) != targetState.c(HORIZONTAL_FACING))
			return false;
		if (Math.abs(crafterState.c(POINTING)
			.getXRotation()
			- targetState.c(POINTING)
				.getXRotation()) == 180)
			return false;
		return true;
	}

	@Override
	public Class<MechanicalCrafterTileEntity> getTileEntityClass() {
		return MechanicalCrafterTileEntity.class;
	}

}

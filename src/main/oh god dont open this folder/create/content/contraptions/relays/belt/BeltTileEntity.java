package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltPart.MIDDLE;
import static com.simibubi.create.content.contraptions.relays.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.util.math.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.math.Direction.AxisDirection.POSITIVE;

import apx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.contraptions.relays.belt.transport.ItemHandlerBeltSegment;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.DebugStickItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTileEntity extends KineticTileEntity {

	public Map<apx, TransportedEntityInfo> passengers;
	public int color;
	public int beltLength;
	public int index;
	public Direction lastInsert;
	public CasingType casing;

	protected BlockPos controller;
	protected BeltInventory inventory;
	protected LazyOptional<IItemHandler> itemHandler;

	public CompoundTag trackerUpdateTag;

	public static enum CasingType {
		NONE, ANDESITE, BRASS;
	}

	public BeltTileEntity(BellBlockEntity<? extends BeltTileEntity> type) {
		super(type);
		controller = BlockPos.ORIGIN;
		itemHandler = LazyOptional.empty();
		casing = CasingType.NONE;
		color = -1;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom)
			.setInsertionHandler(this::tryInsertingFromSide));
		behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf));
	}

	@Override
	public void aj_() {
		super.aj_();

		// Init belt
		if (beltLength == 0)
			BeltBlock.initBelt(d, e);
		if (!AllBlocks.BELT.has(d.d_(e)))
			return;

		initializeItemHandler();

		// Move Items
		if (!isController())
			return;
		getInventory().tick();

		if (getSpeed() == 0)
			return;

		// Move Entities
		if (passengers == null)
			passengers = new HashMap<>();

		List<apx> toRemove = new ArrayList<>();
		passengers.forEach((entity, info) -> {
			boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
			boolean leftTheBelt =
				info.getTicksSinceLastCollision() > ((p().c(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
			if (!canBeTransported || leftTheBelt) {
				toRemove.add(entity);
				return;
			}

			info.tick();
			BeltMovementHandler.transportEntity(this, entity, info);
		});
		toRemove.forEach(passengers::remove);
	}

	@Override
	public float calculateStressApplied() {
		if (!isController())
			return 0;
		return super.calculateStressApplied();
	}

	@Override
	public Timer getRenderBoundingBox() {
		if (!isController())
			return super.getRenderBoundingBox();
		return super.getRenderBoundingBox().g(beltLength + 1);
	}

	protected void initializeItemHandler() {
		if (d.v || itemHandler.isPresent())
			return;
		if (!d.p(controller))
			return;
		BeehiveBlockEntity te = d.c(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return;
		BeltInventory inventory = ((BeltTileEntity) te).getInventory();
		if (inventory == null)
			return;
		IItemHandler handler = new ItemHandlerBeltSegment(inventory, index);
		itemHandler = LazyOptional.of(() -> handler);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == Direction.UP || BeltBlock.canAccessFromSide(side, p())) {
				return itemHandler.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void al_() {
		super.al_();
		itemHandler.invalidate();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (controller != null)
			compound.put("Controller", NbtHelper.fromBlockPos(controller));
		compound.putBoolean("IsController", isController());
		compound.putInt("Color", color);
		compound.putInt("Length", beltLength);
		compound.putInt("Index", index);
		NBTHelper.writeEnum(compound, "Casing", casing);

		if (isController())
			compound.put("Inventory", getInventory().write());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);

		if (compound.getBoolean("IsController"))
			controller = e;

		if (!wasMoved) {
			if (!isController())
				controller = NbtHelper.toBlockPos(compound.getCompound("Controller"));
			trackerUpdateTag = compound;
			color = compound.getInt("Color");
			beltLength = compound.getInt("Length");
			index = compound.getInt("Index");
		}

		if (isController())
			getInventory().read(compound.getCompound("Inventory"));

		CasingType casingBefore = casing;
		casing = NBTHelper.readEnum(compound, "Casing", CasingType.class);

		if (!clientPacket)
			return;
		if (casingBefore == casing)
			return;
		requestModelDataUpdate();
		if (n())
			d.a(o(), p(), p(), 16);
	}

	@Override
	public void clearKineticInformation() {
		super.clearKineticInformation();
		beltLength = 0;
		index = 0;
		controller = null;
		trackerUpdateTag = new CompoundTag();
	}

	public void applyColor(DebugStickItem colorIn) {
		int colorValue = colorIn.f().ai;
		for (BlockPos blockPos : BeltBlock.getBeltChain(d, getController())) {
			BeltTileEntity belt = BeltHelper.getSegmentTE(d, blockPos);
			if (belt == null)
				continue;
			belt.color = belt.color == -1 ? colorValue : ColorHelper.mixColors(belt.color, colorValue, .5f);
			belt.X_();
			belt.sendData();
		}
	}

	public BeltTileEntity getControllerTE() {
		if (controller == null)
			return null;
		if (!d.p(controller))
			return null;
		BeehiveBlockEntity te = d.c(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller == null ? e : controller;
	}

	public boolean isController() {
		return e.equals(controller);
	}

	public float getBeltMovementSpeed() {
		return getSpeed() / 480f;
	}

	public float getDirectionAwareBeltMovementSpeed() {
		int offset = getBeltFacing().getDirection()
			.offset();
		if (getBeltFacing().getAxis() == Axis.X)
			offset *= -1;
		return getBeltMovementSpeed() * offset;
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.has(p()))
			return false;
		return p().c(BeltBlock.PART) != BeltPart.MIDDLE;
	}

	protected boolean isLastBelt() {
		if (getSpeed() == 0)
			return false;

		Direction direction = getBeltFacing();
		if (p().c(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
			return false;

		BeltPart part = p().c(BeltBlock.PART);
		if (part == MIDDLE)
			return false;

		boolean movingPositively = (getSpeed() > 0 == (direction.getDirection()
			.offset() == 1)) ^ direction.getAxis() == Axis.X;
		return part == BeltPart.START ^ movingPositively;
	}

	public Vec3i getMovementDirection(boolean firstHalf) {
		return this.getMovementDirection(firstHalf, false);
	}

	public Vec3i getBeltChainDirection() {
		return this.getMovementDirection(true, true);
	}

	protected Vec3i getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
		if (getSpeed() == 0)
			return BlockPos.ORIGIN;

		final PistonHandler blockState = p();
		final Direction beltFacing = blockState.c(BambooLeaves.O);
		final BeltSlope slope = blockState.c(BeltBlock.SLOPE);
		final BeltPart part = blockState.c(BeltBlock.PART);
		final Axis axis = beltFacing.getAxis();

		Direction movementFacing = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
		boolean notHorizontal = blockState.c(BeltBlock.SLOPE) != HORIZONTAL;
		if (getSpeed() < 0)
			movementFacing = movementFacing.getOpposite();
		Vec3i movement = movementFacing.getVector();

		boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getDirection() == POSITIVE);
		boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (!onSlope)
			return movement;

		return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
	}

	public Direction getMovementFacing() {
		Axis axis = getBeltFacing().getAxis();
		return Direction.from(axis,
			getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
	}

	protected Direction getBeltFacing() {
		return p().c(BambooLeaves.O);
	}

	public BeltInventory getInventory() {
		if (!isController()) {
			BeltTileEntity controllerTE = getControllerTE();
			if (controllerTE != null)
				return controllerTE.getInventory();
			return null;
		}
		if (inventory == null) {
			inventory = new BeltInventory(this);
		}
		return inventory;
	}

	private void applyToAllItems(float maxDistanceFromCenter,
		Function<TransportedItemStack, TransportedResult> processFunction) {
		BeltTileEntity controller = getControllerTE();
		if (controller == null)
			return;
		BeltInventory inventory = controller.getInventory();
		if (inventory != null)
			inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
	}

	private EntityHitResult getWorldPositionOf(TransportedItemStack transported) {
		BeltTileEntity controllerTE = getControllerTE();
		if (controllerTE == null)
			return EntityHitResult.a;
		return BeltHelper.getVectorForOffset(controllerTE, transported.beltPosition);
	}

	public void setCasingType(CasingType type) {
		if (casing == type)
			return;
		if (casing != CasingType.NONE)
			d.syncWorldEvent(2001, e,
				BeetrootsBlock.i(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState()
					: AllBlocks.BRASS_CASING.getDefaultState()));
		casing = type;
		boolean shouldBlockHaveCasing = type != CasingType.NONE;
		PistonHandler blockState = p();
		if (blockState.c(BeltBlock.CASING) != shouldBlockHaveCasing)
			KineticTileEntity.switchToBlockState(d, e, blockState.a(BeltBlock.CASING, shouldBlockHaveCasing));
		X_();
		sendData();
	}

	private boolean canInsertFrom(Direction side) {
		if (getSpeed() == 0)
			return false;
		return getMovementFacing() != side.getOpposite();
	}

	private ItemCooldownManager tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		BeltTileEntity nextBeltController = getControllerTE();
		ItemCooldownManager inserted = transportedStack.stack;
		ItemCooldownManager empty = ItemCooldownManager.tick;

		if (nextBeltController == null)
			return inserted;
		BeltInventory nextInventory = nextBeltController.getInventory();
		if (nextInventory == null)
			return inserted;

		BeehiveBlockEntity teAbove = d.c(e.up());
		if (teAbove instanceof BrassTunnelTileEntity) {
			BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) teAbove;
			if (tunnelTE.hasDistributionBehaviour()) {
				if (!tunnelTE.getStackToDistribute()
					.a())
					return inserted;
				if (!tunnelTE.testFlapFilter(side.getOpposite(), inserted))
					return inserted;
				if (!simulate) {
					BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
					tunnelTE.setStackToDistribute(inserted);
				}
				return empty;
			}
		}

		if (getSpeed() == 0)
			return inserted;
		if (getMovementFacing() == side.getOpposite())
			return inserted;
		if (!nextInventory.canInsertAtFromSide(index, side))
			return inserted;
		if (simulate)
			return empty;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = index + .5f - Math.signum(getDirectionAwareBeltMovementSpeed()) / 16f;

		Direction movementFacing = getMovementFacing();
		if (!side.getAxis()
			.isVertical()) {
			if (movementFacing != side) {
				transportedStack.sideOffset = side.getDirection()
					.offset() * .35f;
				if (side.getAxis() == Axis.X)
					transportedStack.sideOffset *= -1;
			} else
				transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index : index + 1;
		}

		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.insertedAt = index;
		transportedStack.insertedFrom = side;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;

		BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);

		nextInventory.addItem(transportedStack);
		nextBeltController.X_();
		nextBeltController.sendData();
		return empty;
	}

	public static ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(CASING_PROPERTY, casing)
			.build();
	}

}

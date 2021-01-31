package com.simibubi.kinetic_api.content.logistics.block.chute;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import afj;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.contraptions.components.fan.AirCurrent;
import com.simibubi.kinetic_api.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.kinetic_api.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.kinetic_api.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.kinetic_api.content.contraptions.particle.AirParticleData;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BrassFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
/*
 * Commented Code: Chutes kinetic_api air streams and act similarly to encased fans
 * (Unfinished)
 */
public class ChuteTileEntity extends SmartTileEntity implements IHaveGoggleInformation { // , IAirCurrentSource {

//	public AirCurrent airCurrent;

	float pull;
	float push;

	ItemCooldownManager item;
	InterpolatedValue itemPosition;
	ChuteItemHandler itemHandler;
	LazyOptional<IItemHandler> lazyHandler;
	boolean canPickUpItems;

	float bottomPullDistance;
	float beltBelowOffset;
	TransportedItemStackHandlerBehaviour beltBelow;
	boolean updateAirFlow;
	int airCurrentUpdateCooldown;
	int entitySearchCooldown;

	LazyOptional<IItemHandler> capAbove;
	LazyOptional<IItemHandler> capBelow;

	public ChuteTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		item = ItemCooldownManager.tick;
		itemPosition = new InterpolatedValue();
		itemHandler = new ChuteItemHandler(this);
		lazyHandler = LazyOptional.of(() -> itemHandler);
		canPickUpItems = false;
		capAbove = LazyOptional.empty();
		capBelow = LazyOptional.empty();
		bottomPullDistance = 0;
//		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen((d) -> canDirectlyInsertCached()));
	}

	// Cached per-tick, useful when a lot of items are waiting on top of it
	public boolean canDirectlyInsertCached() {
		return canPickUpItems;
	}

	private boolean canDirectlyInsert() {
		PistonHandler blockState = p();
		PistonHandler blockStateAbove = d.d_(e.up());
		if (!AllBlocks.CHUTE.has(blockState))
			return false;
		if (AllBlocks.CHUTE.has(blockStateAbove) && blockStateAbove.c(ChuteBlock.FACING) == Direction.DOWN)
			return false;
		if (getItemMotion() > 0 && getInputChutes().isEmpty())
			return false;
		return blockState.c(ChuteBlock.FACING) == Direction.DOWN
			|| blockState.c(ChuteBlock.SHAPE) == Shape.INTERSECTION;
	}

	@Override
	public void initialize() {
		super.initialize();
		onAdded();
	}

	@Override
	public Timer getRenderBoundingBox() {
		return new Timer(e).b(0, -3, 0);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (!d.v)
			canPickUpItems = canDirectlyInsert();

		float itemMotion = getItemMotion();
		if (itemMotion != 0 && d != null && d.v)
			spawnParticles(itemMotion);
		tickAirStreams(itemMotion);

		if (item.a()) {
			if (itemMotion < 0)
				handleInputFromAbove();
			if (itemMotion > 0)
				handleInputFromBelow();
			return;
		}

		float nextOffset = itemPosition.value + itemMotion;

		if (itemMotion < 0) {
			if (nextOffset < .5f) {
				if (!handleDownwardOutput(true))
					nextOffset = .5f;
				else if (nextOffset < 0) {
					handleDownwardOutput(d.v);
					return;
				}
			}
		}

		if (itemMotion > 0) {
			if (nextOffset > .5f) {
				if (!handleUpwardOutput(true))
					nextOffset = .5f;
				else if (nextOffset > 1) {
					handleUpwardOutput(d.v);
					return;
				}
			}
		}

		itemPosition.set(nextOffset);
	}

	private void updateAirFlow(float itemSpeed) {
		updateAirFlow = false;
//		airCurrent.rebuild();
		if (itemSpeed > 0 && d != null && !d.v) {
			float speed = pull - push;
			beltBelow = null;

			float maxPullDistance;
			if (speed >= 128)
				maxPullDistance = 3;
			else if (speed >= 64)
				maxPullDistance = 2;
			else if (speed >= 32)
				maxPullDistance = 1;
			else
				maxPullDistance = afj.g(speed / 32, 0, 1);

			if (AllBlocks.CHUTE.has(d.d_(e.down())))
				maxPullDistance = 0;
			float flowLimit = maxPullDistance;
			if (flowLimit > 0)
				flowLimit = AirCurrent.getFlowLimit(d, e, maxPullDistance, Direction.DOWN);

			for (int i = 1; i <= flowLimit + 1; i++) {
				TransportedItemStackHandlerBehaviour behaviour =
					TileEntityBehaviour.get(d, e.down(i), TransportedItemStackHandlerBehaviour.TYPE);
				if (behaviour == null)
					continue;
				beltBelow = behaviour;
				beltBelowOffset = i - 1;
				break;
			}
			this.bottomPullDistance = flowLimit;
		}
		sendData();
	}

	private void findEntities(float itemSpeed) {
//		if (getSpeed() != 0)
//			airCurrent.findEntities();
		if (bottomPullDistance <= 0 && !getItem().a() || itemSpeed <= 0 || d == null || d.v)
			return;
		EntityHitResult center = VecHelper.getCenterOf(e);
		Timer searchArea =
			new Timer(center.b(0, -bottomPullDistance - 0.5, 0), center.b(0, -0.5, 0)).g(.45f);
		for (PaintingEntity itemEntity : d.a(PaintingEntity.class, searchArea)) {
			setItem(itemEntity.g()
				.i(),
				(float) (itemEntity.cb()
					.f().c - e.getY()));
			itemEntity.ac();
			AllTriggers.triggerForNearbyPlayers(AllTriggers.UPWARD_CHUTE, d, e, 5);
			break;
		}
	}

	private void extractFromBelt(float itemSpeed) {
		if (itemSpeed <= 0 || d == null || d.v)
			return;
		if (getItem().a() && beltBelow != null) {
			beltBelow.handleCenteredProcessingOnAllItems(.5f, ts -> {
				if (getItem().a()) {
					setItem(ts.stack.i(), -beltBelowOffset);
					return TransportedResult.removeItem();
				}
				return TransportedResult.doNothing();
			});
		}
	}

	private void tickAirStreams(float itemSpeed) {
		if (!d.v && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.SERVER.kinetics.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow(itemSpeed);
		}

		if (entitySearchCooldown-- <= 0 && item.a()) {
			entitySearchCooldown = 5;
			findEntities(itemSpeed);
		}

		extractFromBelt(itemSpeed);
//		if (getSpeed() != 0)
//			airCurrent.tick();
	}

	public void blockBelowChanged() {
		updateAirFlow = true;
	}

	private void spawnParticles(float itemMotion) {
		// todo: reduce the amount of particles
		if (d == null)
			return;
		PistonHandler blockState = p();
		boolean up = itemMotion > 0;
		float absMotion = up ? itemMotion : -itemMotion;
		if (blockState == null || !(blockState.b() instanceof ChuteBlock))
			return;
		if (push == 0 && pull == 0)
			return;

		if (up
			&& (blockState.c(ChuteBlock.FACING) == Direction.DOWN
				|| blockState.c(ChuteBlock.SHAPE) == Shape.INTERSECTION)
			&& BlockHelper.noCollisionInSpace(d, e.up()))
			spawnAirFlow(1, 2, absMotion, .5f);

		if (blockState.c(ChuteBlock.FACING) != Direction.DOWN)
			return;

		if (blockState.c(ChuteBlock.SHAPE) == Shape.WINDOW)
			spawnAirFlow(up ? 0 : 1, up ? 1 : 0, absMotion, 1);

		if (!up && BlockHelper.noCollisionInSpace(d, e.down()))
			spawnAirFlow(0, -1, absMotion, .5f);

		if (up && bottomPullDistance > 0) {
			spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
			spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
		}
	}

	private void spawnAirFlow(float verticalStart, float verticalEnd, float motion, float drag) {
		if (d == null)
			return;
		AirParticleData airParticleData = new AirParticleData(drag, motion);
		EntityHitResult origin = EntityHitResult.b(e);
		float xOff = Create.random.nextFloat() * .5f + .25f;
		float zOff = Create.random.nextFloat() * .5f + .25f;
		EntityHitResult v = origin.b(xOff, verticalStart, zOff);
		EntityHitResult d = origin.b(xOff, verticalEnd, zOff)
			.d(v);
		if (Create.random.nextFloat() < 2 * motion)
			d.b(airParticleData, v.entity, v.c, v.d, d.entity, d.c, d.d);
	}

	private void handleInputFromAbove() {
		if (!capAbove.isPresent())
			capAbove = grabCapability(Direction.UP);
		if (capAbove.isPresent())
			item = ItemHelper.extract(capAbove.orElse(null), stack -> true, ExtractionCountMode.UPTO, 16, false);
	}

	private void handleInputFromBelow() {
		if (!capBelow.isPresent())
			capBelow = grabCapability(Direction.DOWN);
		if (capBelow.isPresent())
			item = ItemHelper.extract(capBelow.orElse(null), stack -> true, ExtractionCountMode.UPTO, 16, false);
	}

	private boolean handleDownwardOutput(boolean simulate) {
		PistonHandler blockState = p();
		ChuteTileEntity targetChute = getTargetChute(blockState);
		Direction direction = blockState.c(ChuteBlock.FACING);

		if (targetChute != null) {
			boolean canInsert = targetChute.item.a();
			if (!simulate && canInsert) {
				targetChute.setItem(item, direction == Direction.DOWN ? 1 : .51f);
				setItem(ItemCooldownManager.tick);
			}
			return canInsert;
		}

		// Diagonal chutes can only insert into other chutes
		if (d == null || direction.getAxis()
			.isHorizontal())
			return false;

		PistonHandler stateBelow = d.d_(e.down());
		if (stateBelow.b() instanceof FunnelBlock) {
			if (stateBelow.d(BrassFunnelBlock.POWERED).orElse(false))
				return false;
			if (stateBelow.c(BrassFunnelBlock.SHAPE) != Direction.UP)
				return false;
			ItemCooldownManager remainder = FunnelBlock.tryInsert(d, e.down(), item, simulate);
			if (!simulate)
				setItem(remainder);
			return remainder.a();
		}

		DirectBeltInputBehaviour directInput =
			TileEntityBehaviour.get(d, e.down(), DirectBeltInputBehaviour.TYPE);
		if (directInput != null) {
			if (!directInput.canInsertFromSide(Direction.UP))
				return false;
			ItemCooldownManager remainder = directInput.handleInsertion(item, Direction.UP, simulate);
			if (!simulate)
				setItem(remainder);
			return remainder.a();
		}

		if (!capBelow.isPresent())
			capBelow = grabCapability(Direction.DOWN);
		if (capBelow.isPresent()) {
			ItemCooldownManager remainder = ItemHandlerHelper.insertItemStacked(capBelow.orElse(null), item, simulate);
			if (!simulate)
				setItem(ItemCooldownManager.tick);
			return remainder.a();
		}

		if (BeetrootsBlock.c(d, e.down()))
			return false;

		if (!simulate) {
			EntityHitResult dropVec = VecHelper.getCenterOf(e)
				.b(0, -12 / 16f, 0);
			PaintingEntity dropped = new PaintingEntity(d, dropVec.entity, dropVec.c, dropVec.d, item.i());
			dropped.m();
			dropped.n(0, -.25f, 0);
			d.c(dropped);
			setItem(ItemCooldownManager.tick);
		}

		return true;
	}

	private boolean handleUpwardOutput(boolean simulate) {
		PistonHandler stateAbove = d.d_(e.up());
		if (stateAbove.b() instanceof FunnelBlock) {
			boolean powered = stateAbove.d(BrassFunnelBlock.POWERED).orElse(false);
			if (!powered && stateAbove.c(BrassFunnelBlock.SHAPE) == Direction.DOWN) {
				ItemCooldownManager remainder = FunnelBlock.tryInsert(d, e.up(), item, simulate);
				if (remainder.a()) {
					if (!simulate)
						setItem(remainder);
					return true;
				}
			}
		}

		ChuteTileEntity bestOutput = null;
		List<ChuteTileEntity> inputChutes = getInputChutes();
		for (ChuteTileEntity targetChute : inputChutes) {
			if (!targetChute.item.a())
				continue;
			float itemMotion = targetChute.getItemMotion();
			if (itemMotion < 0)
				continue;
			if (bestOutput == null || bestOutput.getItemMotion() < itemMotion) {
				bestOutput = targetChute;
			}
		}

		if (bestOutput != null) {
			if (!simulate) {
				bestOutput.setItem(item, 0);
				setItem(ItemCooldownManager.tick);
			}
			return true;
		}

		if (!capAbove.isPresent())
			capAbove = grabCapability(Direction.UP);
		if (capAbove.isPresent()) {
			ItemCooldownManager remainder = ItemHandlerHelper.insertItemStacked(capAbove.orElse(null), item, simulate);
			if (!simulate)
				setItem(ItemCooldownManager.tick);
			return remainder.a();
		}

		if (BlockHelper.hasBlockSolidSide(stateAbove, d, e.up(), Direction.DOWN))
			return false;
		if (!inputChutes.isEmpty())
			return false;

		if (!simulate) {
			EntityHitResult dropVec = VecHelper.getCenterOf(e)
				.b(0, 8 / 16f, 0);
			PaintingEntity dropped = new PaintingEntity(d, dropVec.entity, dropVec.c, dropVec.d, item.i());
			dropped.m();
			dropped.n(0, getItemMotion() * 2, 0);
			d.c(dropped);
			setItem(ItemCooldownManager.tick);
		}
		return true;
	}

	private LazyOptional<IItemHandler> grabCapability(Direction side) {
		BlockPos pos = this.e.offset(side);
		if (d == null)
			return LazyOptional.empty();
		BeehiveBlockEntity te = d.c(pos);
		if (te == null || te instanceof ChuteTileEntity)
			return LazyOptional.empty();
		return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
	}

	public void setItem(ItemCooldownManager stack) {
		setItem(stack, getItemMotion() < 0 ? 1 : 0);
	}

	public void setItem(ItemCooldownManager stack, float insertionPos) {
		item = stack;
		itemPosition.lastValue = itemPosition.value = insertionPos;
		X_();
		sendData();
	}

	@Override
	public void al_() {
		super.al_();
		if (lazyHandler != null)
			lazyHandler.invalidate();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Item", item.serializeNBT());
		compound.putFloat("ItemPosition", itemPosition.value);
		compound.putFloat("Pull", pull);
		compound.putFloat("Push", push);
		compound.putFloat("BottomAirFlowDistance", bottomPullDistance);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		ItemCooldownManager previousItem = item;
		item = ItemCooldownManager.a(compound.getCompound("Item"));
		itemPosition.lastValue = itemPosition.value = compound.getFloat("ItemPosition");
		pull = compound.getFloat("Pull");
		push = compound.getFloat("Push");
		bottomPullDistance = compound.getFloat("BottomAirFlowDistance");
		super.fromTag(state, compound, clientPacket);
//		if (clientPacket)
//			airCurrent.rebuild();

		if (n() && d != null && d.v && !previousItem.equals(item, false) && !item.a()) {
			if (d.t.nextInt(3) != 0)
				return;
			EntityHitResult p = VecHelper.getCenterOf(e);
			p = VecHelper.offsetRandomly(p, d.t, .5f);
			EntityHitResult m = EntityHitResult.a;
			d.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, item), p.entity, p.c, p.d, m.entity, m.c, m.d);
		}
	}

	public float getItemMotion() {
		// Chutes per second
		final float fanSpeedModifier = 1 / 64f;
		final float maxItemSpeed = 20f;
		final float gravity = 4f;

		float motion = (push + pull) * fanSpeedModifier;
		return (afj.a(motion, -maxItemSpeed, maxItemSpeed) + (motion <= 0 ? -gravity : 0)) / 20f;
	}

	public void onRemoved(PistonHandler chuteState) {
		ChuteTileEntity targetChute = getTargetChute(chuteState);
		List<ChuteTileEntity> inputChutes = getInputChutes();
		if (!item.a() && d != null)
			Inventory.a(d, e.getX(), e.getY(), e.getZ(), item);
		al_();
		if (targetChute != null) {
			targetChute.updatePull();
			targetChute.propagatePush();
		}
		inputChutes.forEach(c -> c.updatePush(inputChutes.size()));
	}

	public void onAdded() {
		s();
		updatePull();
		ChuteTileEntity targetChute = getTargetChute(p());
		if (targetChute != null)
			targetChute.propagatePush();
		else
			updatePush(1);
	}

	public void updatePull() {
		float totalPull = calculatePull();
		if (pull == totalPull)
			return;
		pull = totalPull;
		updateAirFlow = true;
		sendData();
		ChuteTileEntity targetChute = getTargetChute(p());
		if (targetChute != null)
			targetChute.updatePull();
	}

	public void updatePush(int branchCount) {
		float totalPush = calculatePush(branchCount);
		if (push == totalPush)
			return;
		updateAirFlow = true;
		push = totalPush;
		sendData();
		propagatePush();
	}

	public void propagatePush() {
		List<ChuteTileEntity> inputs = getInputChutes();
		inputs.forEach(c -> c.updatePush(inputs.size()));
	}

	protected float calculatePull() {
		PistonHandler blockStateAbove = d.d_(e.up());
		if (AllBlocks.ENCASED_FAN.has(blockStateAbove)
			&& blockStateAbove.c(EncasedFanBlock.FACING) == Direction.DOWN) {
			BeehiveBlockEntity te = d.c(e.up());
			if (te instanceof EncasedFanTileEntity && !te.q()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return fan.getSpeed();
			}
		}

		float totalPull = 0;
		for (Direction d : Iterate.directions) {
			ChuteTileEntity inputChute = getInputChute(d);
			if (inputChute == null)
				continue;
			totalPull += inputChute.pull;
		}
		return totalPull;
	}

	protected float calculatePush(int branchCount) {
		if (d == null)
			return 0;
		PistonHandler blockStateBelow = d.d_(e.down());
		if (AllBlocks.ENCASED_FAN.has(blockStateBelow) && blockStateBelow.c(EncasedFanBlock.FACING) == Direction.UP) {
			BeehiveBlockEntity te = d.c(e.down());
			if (te instanceof EncasedFanTileEntity && !te.q()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return fan.getSpeed();
			}
		}

		ChuteTileEntity targetChute = getTargetChute(p());
		if (targetChute == null)
			return 0;
		return targetChute.push / branchCount;
	}

	@Nullable
	private ChuteTileEntity getTargetChute(PistonHandler state) {
		if (d == null)
			return null;
		Direction targetDirection = state.c(ChuteBlock.FACING);
		BlockPos chutePos = e.down();
		if (targetDirection.getAxis()
			.isHorizontal())
			chutePos = chutePos.offset(targetDirection.getOpposite());
		PistonHandler chuteState = d.d_(chutePos);
		if (!AllBlocks.CHUTE.has(chuteState))
			return null;
		BeehiveBlockEntity te = d.c(chutePos);
		if (te instanceof ChuteTileEntity)
			return (ChuteTileEntity) te;
		return null;
	}

	private List<ChuteTileEntity> getInputChutes() {
		List<ChuteTileEntity> inputs = new LinkedList<>();
		for (Direction d : Iterate.directions) {
			ChuteTileEntity inputChute = getInputChute(d);
			if (inputChute == null)
				continue;
			inputs.add(inputChute);
		}
		return inputs;
	}

	@Nullable
	private ChuteTileEntity getInputChute(Direction direction) {
		if (d == null || direction == Direction.DOWN)
			return null;
		direction = direction.getOpposite();
		BlockPos chutePos = e.up();
		if (direction.getAxis()
			.isHorizontal())
			chutePos = chutePos.offset(direction);
		PistonHandler chuteState = d.d_(chutePos);
		if (!AllBlocks.CHUTE.has(chuteState) || chuteState.c(ChuteBlock.FACING) != direction)
			return null;
		BeehiveBlockEntity te = d.c(chutePos);
		if (te instanceof ChuteTileEntity && !te.q())
			return (ChuteTileEntity) te;
		return null;
	}

	public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		boolean downward = getItemMotion() < 0;
		tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.chute.header")));
		if (pull == 0 && push == 0)
			tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.chute.no_fans_attached")).formatted(Formatting.GRAY));
		if (pull != 0)
			tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.chute.fans_" + (pull > 0 ? "pull_up" : "push_down")).formatted(Formatting.GRAY)));
		if (push != 0)
			tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.chute.fans_" + (push > 0 ? "push_up" : "pull_down")).formatted(Formatting.GRAY)));
		tooltip.add(componentSpacing.copy().append("-> ").append(Lang.translate("tooltip.chute.items_move_" + (downward ? "down" : "up")).formatted(Formatting.YELLOW)));
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return lazyHandler.cast();
		return super.getCapability(cap, side);
	}

	public ItemCooldownManager getItem() {
		return item;
	}

//	@Override
//	@Nullable
//	public AirCurrent getAirCurrent() {
//		return airCurrent;
//	}
//
//	@Nullable
//	@Override
//	public World getAirCurrentWorld() {
//		return world;
//	}
//
//	@Override
//	public BlockPos getAirCurrentPos() {
//		return pos;
//	}
//
//	@Override
//	public float getSpeed() {
//		if (getBlockState().get(ChuteBlock.SHAPE) == Shape.NORMAL && getBlockState().get(ChuteBlock.FACING) != Direction.DOWN)
//			return 0;
//		return pull + push;
//	}
//
//	@Override
//	@Nullable
//	public Direction getAirFlowDirection() {
//		float speed = getSpeed();
//		if (speed == 0)
//			return null;
//		return speed > 0 ? Direction.UP : Direction.DOWN;
//	}
//
//	@Override
//	public boolean isSourceRemoved() {
//		return removed;
//	}
//
//	@Override
//	public Direction getAirflowOriginSide() {
//		return world != null && !(world.getTileEntity(pos.down()) instanceof IAirCurrentSource)
//			&& getBlockState().get(ChuteBlock.FACING) == Direction.DOWN ? Direction.DOWN : Direction.UP;
//	}
}

package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import afj;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Jukebox;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.InterpolatedAngle;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class ArmTileEntity extends KineticTileEntity {

	// Server
	List<ArmInteractionPoint> inputs;
	List<ArmInteractionPoint> outputs;
	ListTag interactionPointTag;

	// Both
	float chasedPointProgress;
	int chasedPointIndex;
	ItemCooldownManager heldItem;
	Phase phase;

	// Client
	ArmAngleTarget previousTarget;
	InterpolatedAngle lowerArmAngle;
	InterpolatedAngle upperArmAngle;
	InterpolatedAngle baseAngle;
	InterpolatedAngle headAngle;
	InterpolatedAngle clawAngle;
	float previousBaseAngle;
	boolean updateInteractionPoints;

	//
	protected ScrollOptionBehaviour<SelectionMode> selectionMode;
	protected int lastInputIndex = -1;
	protected int lastOutputIndex = -1;
	protected boolean redstoneLocked;

	enum Phase {
		SEARCH_INPUTS, MOVE_TO_INPUT, SEARCH_OUTPUTS, MOVE_TO_OUTPUT, DANCING
	}

	public ArmTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		interactionPointTag = new ListTag();
		heldItem = ItemCooldownManager.tick;
		phase = Phase.SEARCH_INPUTS;
		baseAngle = new InterpolatedAngle();
		lowerArmAngle = new InterpolatedAngle();
		upperArmAngle = new InterpolatedAngle();
		headAngle = new InterpolatedAngle();
		clawAngle = new InterpolatedAngle();
		previousTarget = ArmAngleTarget.NO_TARGET;
		previousBaseAngle = previousTarget.baseAngle;
		updateInteractionPoints = true;
		redstoneLocked = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		selectionMode = new ScrollOptionBehaviour<SelectionMode>(SelectionMode.class,
			Lang.translate("logistics.when_multiple_outputs_available"), this, new SelectionModeValueBox());
		selectionMode.requiresWrench();
		behaviours.add(selectionMode);
	}

	@Override
	public void aj_() {
		super.aj_();
		initInteractionPoints();
		tickMovementProgress();

		if (d.v)
			return;
		if (chasedPointProgress < 1)
			return;
		if (phase == Phase.MOVE_TO_INPUT)
			collectItem();
		if (phase == Phase.MOVE_TO_OUTPUT)
			depositItem();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (d.v)
			return;
		if (chasedPointProgress < .5f)
			return;
		if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING) {
			checkForMusic();
			searchForItem();
		}
		if (phase == Phase.SEARCH_OUTPUTS)
			searchForDestination();
	}

	private void checkForMusic() {
		boolean hasMusic = checkForMusicAmong(inputs) || checkForMusicAmong(outputs);
		if (hasMusic != (phase == Phase.DANCING)) {
			phase = hasMusic ? Phase.DANCING : Phase.SEARCH_INPUTS;
			X_();
			sendData();
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().g(3);
	}

	private boolean checkForMusicAmong(List<ArmInteractionPoint> list) {
		for (ArmInteractionPoint armInteractionPoint : list) {
			if (!(armInteractionPoint instanceof Jukebox))
				continue;
			PistonHandler state = d.d_(armInteractionPoint.pos);
			if (state.d(InfestedBlock.regularBlock).orElse(false))
				return true;
		}
		return false;
	}

	private void tickMovementProgress() {
		chasedPointProgress += Math.min(256, Math.abs(getSpeed())) / 1024f;
		if (chasedPointProgress > 1)
			chasedPointProgress = 1;
		if (!d.v)
			return;

		ArmInteractionPoint targetedInteractionPoint = getTargetedInteractionPoint();
		ArmAngleTarget previousTarget = this.previousTarget;
		ArmAngleTarget target = targetedInteractionPoint == null ? ArmAngleTarget.NO_TARGET
			: targetedInteractionPoint.getTargetAngles(e, isOnCeiling());

		baseAngle.set(AngleHelper.angleLerp(chasedPointProgress, previousBaseAngle,
			target == ArmAngleTarget.NO_TARGET ? previousBaseAngle : target.baseAngle));

		// Arm's angles first backup to resting position and then continue
		if (chasedPointProgress < .5f)
			target = ArmAngleTarget.NO_TARGET;
		else
			previousTarget = ArmAngleTarget.NO_TARGET;
		float progress = chasedPointProgress == 1 ? 1 : (chasedPointProgress % .5f) * 2;

		lowerArmAngle.set(afj.g(progress, previousTarget.lowerArmAngle, target.lowerArmAngle));
		upperArmAngle.set(afj.g(progress, previousTarget.upperArmAngle, target.upperArmAngle));

		headAngle.set(AngleHelper.angleLerp(progress, previousTarget.headAngle % 360, target.headAngle % 360));
	}

	protected boolean isOnCeiling() {
		PistonHandler state = p();
		return n() && state.d(ArmBlock.CEILING).orElse(false);
	}

	@Nullable
	private ArmInteractionPoint getTargetedInteractionPoint() {
		if (chasedPointIndex == -1)
			return null;
		if (phase == Phase.MOVE_TO_INPUT && chasedPointIndex < inputs.size())
			return inputs.get(chasedPointIndex);
		if (phase == Phase.MOVE_TO_OUTPUT && chasedPointIndex < outputs.size())
			return outputs.get(chasedPointIndex);
		return null;
	}

	protected void searchForItem() {
		if (redstoneLocked)
			return;

		boolean foundInput = false;
		// for round robin, we start looking after the last used index, for default we
		// start at 0;
		int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastInputIndex + 1;

		// if we enforce round robin, only look at the next input in the list,
		// otherwise, look at all inputs
		int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastInputIndex + 2 : inputs.size();
		if (scanRange > inputs.size())
			scanRange = inputs.size();

		InteractionPoints: for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = inputs.get(i);
			if (!armInteractionPoint.isStillValid(d))
				continue;
			for (int j = 0; j < armInteractionPoint.getSlotCount(d); j++) {
				if (getDistributableAmount(armInteractionPoint, j) == 0)
					continue;

				selectIndex(true, i);
				foundInput = true;
				break InteractionPoints;
			}
		}
		if (!foundInput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
			// if we didn't find an input, but don't want to enforce round robin, reset the
			// last index
			lastInputIndex = -1;
		}
		if (lastInputIndex == inputs.size() - 1) {
			// if we reached the last input in the list, reset the last index
			lastInputIndex = -1;
		}
	}

	protected void searchForDestination() {
		ItemCooldownManager held = heldItem.i();

		boolean foundOutput = false;
		// for round robin, we start looking after the last used index, for default we
		// start at 0;
		int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastOutputIndex + 1;

		// if we enforce round robin, only look at the next index in the list,
		// otherwise, look at all
		int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastOutputIndex + 2 : outputs.size();
		if (scanRange > outputs.size())
			scanRange = outputs.size();

		for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = outputs.get(i);
			if (!armInteractionPoint.isStillValid(d))
				continue;

			ItemCooldownManager remainder = armInteractionPoint.insert(d, held, true);
			if (remainder.equals(heldItem, false))
				continue;

			selectIndex(false, i);
			foundOutput = true;
			break;
		}

		if (!foundOutput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
			// if we didn't find an input, but don't want to enforce round robin, reset the
			// last index
			lastOutputIndex = -1;
		}
		if (lastOutputIndex == outputs.size() - 1) {
			// if we reached the last input in the list, reset the last index
			lastOutputIndex = -1;
		}
	}

	// input == true => select input, false => select output
	private void selectIndex(boolean input, int index) {
		phase = input ? Phase.MOVE_TO_INPUT : Phase.MOVE_TO_OUTPUT;
		chasedPointIndex = index;
		chasedPointProgress = 0;
		if (input)
			lastInputIndex = index;
		else
			lastOutputIndex = index;
		sendData();
		X_();
	}

	protected int getDistributableAmount(ArmInteractionPoint armInteractionPoint, int i) {
		ItemCooldownManager stack = armInteractionPoint.extract(d, i, true);
		ItemCooldownManager remainder = simulateInsertion(stack);
		return stack.E() - remainder.E();
	}

	protected void depositItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null) {
			ItemCooldownManager toInsert = heldItem.i();
			ItemCooldownManager remainder = armInteractionPoint.insert(d, toInsert, false);
			heldItem = remainder;
		}
		phase = heldItem.a() ? Phase.SEARCH_INPUTS : Phase.SEARCH_OUTPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		X_();

		if (!d.v)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.MECHANICAL_ARM, d, e, 10);
	}

	protected void collectItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null)
			for (int i = 0; i < armInteractionPoint.getSlotCount(d); i++) {
				int amountExtracted = getDistributableAmount(armInteractionPoint, i);
				if (amountExtracted == 0)
					continue;

				heldItem = armInteractionPoint.extract(d, i, amountExtracted, false);
				phase = Phase.SEARCH_OUTPUTS;
				chasedPointProgress = 0;
				chasedPointIndex = -1;
				sendData();
				X_();
				return;
			}

		phase = Phase.SEARCH_INPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		X_();
	}

	private ItemCooldownManager simulateInsertion(ItemCooldownManager stack) {
		for (ArmInteractionPoint armInteractionPoint : outputs) {
			stack = armInteractionPoint.insert(d, stack, true);
			if (stack.a())
				break;
		}
		return stack;
	}

	public void redstoneUpdate() {
		if (d.v)
			return;
		boolean blockPowered = d.r(e);
		if (blockPowered == redstoneLocked)
			return;
		redstoneLocked = blockPowered;
		sendData();
		if (!redstoneLocked)
			searchForItem();
	}

	protected void initInteractionPoints() {
		if (!updateInteractionPoints || interactionPointTag == null)
			return;
		if (!d.isAreaLoaded(e, getRange() + 1))
			return;
		inputs.clear();
		outputs.clear();

		boolean hasBlazeBurner = false;
		for (Tag inbt : interactionPointTag) {
			ArmInteractionPoint point = ArmInteractionPoint.deserialize(d, e, (CompoundTag) inbt);
			if (point == null)
				continue;
			if (point.mode == Mode.DEPOSIT)
				outputs.add(point);
			if (point.mode == Mode.TAKE)
				inputs.add(point);
			hasBlazeBurner |= point instanceof ArmInteractionPoint.BlazeBurner;
		}

		if (!d.v) {
			if (outputs.size() >= 10)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.ARM_MANY_TARGETS, d, e, 5);
			if (hasBlazeBurner)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.ARM_BLAZE_BURNER, d, e, 5);
		}

		updateInteractionPoints = false;
		sendData();
		X_();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		if (updateInteractionPoints) {
			compound.put("InteractionPoints", interactionPointTag);

		} else {
			ListTag pointsNBT = new ListTag();
			inputs.stream()
				.map(aip -> aip.serialize(e))
				.forEach(pointsNBT::add);
			outputs.stream()
				.map(aip -> aip.serialize(e))
				.forEach(pointsNBT::add);
			compound.put("InteractionPoints", pointsNBT);
		}

		NBTHelper.writeEnum(compound, "Phase", phase);
		compound.putBoolean("Powered", redstoneLocked);
		compound.put("HeldItem", heldItem.serializeNBT());
		compound.putInt("TargetPointIndex", chasedPointIndex);
		compound.putFloat("MovementProgress", chasedPointProgress);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		int previousIndex = chasedPointIndex;
		Phase previousPhase = phase;
		ListTag interactionPointTagBefore = interactionPointTag;

		super.fromTag(state, compound, clientPacket);
		heldItem = ItemCooldownManager.a(compound.getCompound("HeldItem"));
		phase = NBTHelper.readEnum(compound, "Phase", Phase.class);
		chasedPointIndex = compound.getInt("TargetPointIndex");
		chasedPointProgress = compound.getFloat("MovementProgress");
		interactionPointTag = compound.getList("InteractionPoints", NBT.TAG_COMPOUND);
		redstoneLocked = compound.getBoolean("Powered");

		if (!clientPacket)
			return;

		boolean ceiling = isOnCeiling();
		if (interactionPointTagBefore == null || interactionPointTagBefore.size() != interactionPointTag.size())
			updateInteractionPoints = true;
		if (previousIndex != chasedPointIndex || (previousPhase != phase)) {
			ArmInteractionPoint previousPoint = null;
			if (previousPhase == Phase.MOVE_TO_INPUT && previousIndex < inputs.size())
				previousPoint = inputs.get(previousIndex);
			if (previousPhase == Phase.MOVE_TO_OUTPUT && previousIndex < outputs.size())
				previousPoint = outputs.get(previousIndex);
			previousTarget =
				previousPoint == null ? ArmAngleTarget.NO_TARGET : previousPoint.getTargetAngles(e, ceiling);
			if (previousPoint != null)
				previousBaseAngle = previousPoint.getTargetAngles(e, ceiling).baseAngle;
		}
	}

	public static int getRange() {
		return AllConfigs.SERVER.logistics.mechanicalArmRange.get();
	}

	@Override
	public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (isPlayerSneaking)
			return false;
		if (!inputs.isEmpty())
			return false;
		if (!outputs.isEmpty())
			return false;

		TooltipHelper.addHint(tooltip, "hint.mechanical_arm_no_targets");
		return true;
	}

	private class SelectionModeValueBox extends CenteredSideValueBoxTransform {

		public SelectionModeValueBox() {
			super((blockState, direction) -> direction != Direction.DOWN && direction != Direction.UP);
		}

		@Override
		protected EntityHitResult getLocalOffset(PistonHandler state) {
			int yPos = state.c(ArmBlock.CEILING) ? 16 - 3 : 3;
			EntityHitResult location = VecHelper.voxelSpace(8, yPos, 14.5);
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
			return location;
		}

		@Override
		protected float getScale() {
			return .3f;
		}

	}

	public enum SelectionMode implements INamedIconOptions {
		ROUND_ROBIN(AllIcons.I_ARM_ROUND_ROBIN),
		FORCED_ROUND_ROBIN(AllIcons.I_ARM_FORCED_ROUND_ROBIN),
		PREFER_FIRST(AllIcons.I_ARM_PREFER_FIRST),

		;

		private final String translationKey;
		private final AllIcons icon;

		SelectionMode(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "mechanical_arm.selection_mode." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}

}

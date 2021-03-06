package com.tropheus_jay.kinetic_api.content.contraptions.base;

import com.tropheus_jay.kinetic_api.KineticAPI;
import com.tropheus_jay.kinetic_api.content.contraptions.KineticNetwork;
import com.tropheus_jay.kinetic_api.content.contraptions.RotationPropagator;
import com.tropheus_jay.kinetic_api.content.contraptions.goggles.IHaveGoggleInformation;
import com.tropheus_jay.kinetic_api.content.contraptions.goggles.IHaveHoveringInformation;
import com.tropheus_jay.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.tropheus_jay.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class KineticTileEntity extends SmartTileEntity
	implements Tickable, IHaveGoggleInformation, IHaveHoveringInformation {

	public @Nullable Long network;
	public @Nullable BlockPos source;
	public boolean networkDirty;
	public boolean updateSpeed;

	protected KineticEffectHandler effects;
	protected float speed;
	protected float capacity;
	protected float stress;
	protected boolean overStressed;
	protected boolean wasMoved;

	private int flickerTally;
	private int networkSize;
	private int validationCountdown;
	private float lastStressApplied;
	private float lastCapacityProvided;

	public KineticTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
		effects = new KineticEffectHandler(this);
		updateSpeed = true;
	}
	
	@Override
	public void initialize() {
		if (hasNetwork()) {
			KineticNetwork network = getOrCreateNetwork();
			if (!network.initialized)
				network.initFromTE(capacity, stress, networkSize);
			network.addSilently(this, lastCapacityProvided, lastStressApplied);
		}

		super.initialize();
	}

	@Override
	public void tick() {
		if (!world.isClient && needsSpeedUpdate())
			attachKinetics();

		super.tick();
		effects.tick();

		if (world.isClient)
			return;

		if (validationCountdown-- <= 0) {
			//validationCountdown = AllConfigs.SERVER.kinetics.kineticValidationFrequency.get(); todo: configs
			validationCountdown = 1;
			validateKinetics();
		}

		if (getFlickerScore() > 0)
			flickerTally = getFlickerScore() - 1;

		if (networkDirty) {
			if (hasNetwork())
				getOrCreateNetwork().updateNetwork();
			networkDirty = false;
		}
	}

	private void validateKinetics() {
		if (hasSource()) {
			if (!hasNetwork()) {
				removeSource();
				return;
			}

			if (!world.canSetBlock(source))
				return;

			BlockEntity tileEntity = world.getBlockEntity(source);
			KineticTileEntity sourceTe =
				tileEntity instanceof KineticTileEntity ? (KineticTileEntity) tileEntity : null;
			if (sourceTe == null || sourceTe.speed == 0) {
				removeSource();
				detachKinetics();
				return;
			}

			return;
		}

		if (speed != 0) {
			if (getGeneratedSpeed() == 0)
				speed = 0;
		}
	}

	public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
		networkDirty = false;
		this.capacity = maxStress;
		this.stress = currentStress;
		this.networkSize = networkSize;
		boolean overStressed = maxStress < currentStress && IRotate.StressImpact.isEnabled();

		if (overStressed != this.overStressed) {
			float prevSpeed = getSpeed();
			this.overStressed = overStressed;
			onSpeedChanged(prevSpeed);
			sendData();
		}
	}

	public float calculateAddedStressCapacity() {
		//float capacity = (float) AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(getStressConfigKey()); todo: configs
		float capacity = 1.0F;
		this.lastCapacityProvided = capacity;
		return capacity;
	}

	protected Block getStressConfigKey() {
		return getCachedState().getBlock();
	}

	public float calculateStressApplied() {
		//float impact = (float) AllConfigs.SERVER.kinetics.stressValues.getImpactOf(p().b()); todo: configs
		float impact = 1.0F;
		this.lastStressApplied = impact;
		return impact;
	}

	public void onSpeedChanged(float previousSpeed) {
		boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
		boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
		if (fromOrToZero || directionSwap)
			flickerTally = getFlickerScore() + 5;

		if (fromOrToZero && previousSpeed == 0 && !world.isClient)
			/*AllTriggers.getPlayersInRange(world, pos, 4)
				.forEach(p -> AllTriggers.KINETIC_BLOCK.trigger(p, p())); todo: advancements */;
	}
//todo: not sure if this is correct method. original was "remove"
	@Override
	public void markRemoved() {
		if (!world.isClient) {
			if (hasNetwork())
				getOrCreateNetwork().remove(this);
			detachKinetics();
		}
		super.markRemoved();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Speed", speed);

		if (needsSpeedUpdate())
			compound.putBoolean("NeedsSpeedUpdate", true);

		if (hasSource())
			compound.put("Source", NbtHelper.fromBlockPos(source));

		if (hasNetwork()) {
			CompoundTag networkTag = new CompoundTag();
			networkTag.putLong("Id", this.network);
			networkTag.putFloat("Stress", stress);
			networkTag.putFloat("Capacity", capacity);
			networkTag.putInt("Size", networkSize);

			if (lastStressApplied != 0)
				networkTag.putFloat("AddedStress", lastStressApplied);
			if (lastCapacityProvided != 0)
				networkTag.putFloat("AddedCapacity", lastCapacityProvided);

			compound.put("Network", networkTag);
		}

		super.write(compound, clientPacket);
	}

	public boolean needsSpeedUpdate() {
		return updateSpeed;
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		boolean overStressedBefore = overStressed;
		clearKineticInformation();

		// DO NOT READ kinetic information when placed after movement
		if (wasMoved) {
			super.fromTag(state, compound, clientPacket);
			return;
		}

		speed = compound.getFloat("Speed");

		if (compound.contains("Source"))
			source = NbtHelper.toBlockPos(compound.getCompound("Source"));

		if (compound.contains("Network")) {
			CompoundTag networkTag = compound.getCompound("Network");
			network = networkTag.getLong("Id");
			stress = networkTag.getFloat("Stress");
			capacity = networkTag.getFloat("Capacity");
			networkSize = networkTag.getInt("Size");
			lastStressApplied = networkTag.getFloat("AddedStress");
			lastCapacityProvided = networkTag.getFloat("AddedCapacity");
			overStressed = capacity < stress && IRotate.StressImpact.isEnabled();
		}

		super.fromTag(state, compound, clientPacket);

		if (clientPacket && overStressedBefore != overStressed && speed != 0)
			effects.triggerOverStressedEffect();
	}

	public float getGeneratedSpeed() {
		return 0;
	}

	public boolean isSource() {
		return getGeneratedSpeed() != 0;
	}

	public float getSpeed() {
		if (overStressed)
			return 0;
		return getTheoreticalSpeed();
	}

	public float getTheoreticalSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean hasSource() {
		return source != null;
	}

	public void setSource(BlockPos source) {
		this.source = source;
		if (world == null || world.isClient)
			return;

		BlockEntity tileEntity = world.getBlockEntity(source);
		if (!(tileEntity instanceof KineticTileEntity)) {
			removeSource();
			return;
		}

		KineticTileEntity sourceTe = (KineticTileEntity) tileEntity;
		setNetwork(sourceTe.network);
	}

	public void removeSource() {
		float prevSpeed = getSpeed();

		speed = 0;
		source = null;
		setNetwork(null);

		onSpeedChanged(prevSpeed);
	}

	public void setNetwork(@Nullable Long networkIn) {
		if (network == networkIn)
			return;
		if (network != null)
			getOrCreateNetwork().remove(this);

		network = networkIn;

		if (networkIn == null)
			return;

		network = networkIn;
		KineticNetwork network = getOrCreateNetwork();
		network.initialized = true;
		network.add(this);
	}

	public KineticNetwork getOrCreateNetwork() {
		return KineticAPI.torquePropagator.getOrCreateNetworkFor(this);
	}

	public boolean hasNetwork() {
		return network != null;
	}

	public void attachKinetics() {
		updateSpeed = false;
		RotationPropagator.handleAdded(world, pos, this);
	}

	public void detachKinetics() {
		RotationPropagator.handleRemoved(world, pos, this);
	}

	public boolean isSpeedRequirementFulfilled() {
		BlockState state = getCachedState();
		if (!(getCachedState().getBlock() instanceof IRotate))
			return true;
		IRotate def = (IRotate) state.getBlock();
		IRotate.SpeedLevel minimumRequiredSpeedLevel = def.getMinimumRequiredSpeedLevel();
		if (minimumRequiredSpeedLevel == null)
			return true;
		/*if (minimumRequiredSpeedLevel == SpeedLevel.MEDIUM)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.mediumSpeed.get();
		if (minimumRequiredSpeedLevel == SpeedLevel.FAST)
			return Math.abs(getSpeed()) >= AllConfigs.SERVER.kinetics.fastSpeed.get(); todo: configs */
		return true;
	}

	public static void switchToBlockState(World world, BlockPos pos, BlockState state) {
		if (world.isClient)
			return;

		BlockEntity tileEntityIn = world.getBlockEntity(pos);
		BlockState currentState = world.getBlockState(pos);
		boolean isKinetic = tileEntityIn instanceof KineticTileEntity;

		if (currentState == state)
			return;
		if (tileEntityIn == null || !isKinetic) {
			world.setBlockState(pos, state, 3);
			return;
		}

		KineticTileEntity tileEntity = (KineticTileEntity) tileEntityIn;
		if (tileEntity.hasNetwork())
			tileEntity.getOrCreateNetwork()
				.remove(tileEntity);
		tileEntity.detachKinetics();
		tileEntity.removeSource();
		world.setBlockState(pos, state, 3);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	/* @Override
	public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		boolean notFastEnough = !isSpeedRequirementFulfilled() && getSpeed() != 0;

		if (overStressed && AllConfigs.CLIENT.enableOverstressedTooltip.get()) {
			tooltip.add(componentSpacing.copy().append(Lang.translate("gui.stressometer.overstressed").formatted(GOLD)));
			Text hint = Lang.translate("gui.contraptions.network_overstressed");
			List<Text> cutString = TooltipHelper.cutTextComponent(hint, GRAY, Formatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add(componentSpacing.copy().append(cutString.get(i)));
			return true;
		}

		if (notFastEnough) {
			tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.speedRequirement").formatted(GOLD)));
			Text hint = Lang.translate("gui.contraptions.not_fast_enough", StatusEffectSpriteManager.a(p().b()
				.i()));
			List<Text> cutString = TooltipHelper.cutTextComponent(hint, GRAY, Formatting.WHITE);
			for (int i = 0; i < cutString.size(); i++)
				tooltip.add(componentSpacing.copy().append(cutString.get(i)));
			return true;
		}

		return false;
	} todo: configs */

	@Override
	public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		boolean added = false;
		float stressAtBase = calculateStressApplied();
/* todo: something with lang
		if (calculateStressApplied() != 0 && StressImpact.isEnabled()) {
			tooltip.add(componentSpacing.copy().append(Lang.translate("gui.goggles.kinetic_stats")));
			tooltip.add(componentSpacing.copy().append(Lang.translate("tooltip.stressImpact").formatted(Formatting.GRAY)));

			float stressTotal = stressAtBase * Math.abs(getTheoreticalSpeed());

			tooltip.add(componentSpacing.copy().append(new LiteralText(" " + IHaveGoggleInformation.format(stressTotal))
				.append(Lang.translate("generic.unit.stress")).append(" ").formatted(Formatting.AQUA)).append(Lang.translate("gui.goggles.at_current_speed").formatted(Formatting.DARK_GRAY)));

			added = true;
		} */

		return added;

	}

	public void clearKineticInformation() {
		speed = 0;
		source = null;
		network = null;
		overStressed = false;
		stress = 0;
		capacity = 0;
		lastStressApplied = 0;
		lastCapacityProvided = 0;
	}

	public void warnOfMovement() {
		wasMoved = true;
	}

	public int getFlickerScore() {
		return flickerTally;
	}

	public static float convertToDirection(float axisSpeed, Direction d) {
		return d.getDirection() == AxisDirection.POSITIVE ? axisSpeed : -axisSpeed;
	}

	public boolean isOverStressed() {
		return overStressed;
	}

}

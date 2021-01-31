package com.simibubi.kinetic_api.content.contraptions.components.mixer;

import java.util.List;
import java.util.Optional;
import afj;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidFX;
import com.simibubi.kinetic_api.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinTileEntity;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.advancement.ITriggerable;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.item.SmartInventory;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MechanicalMixerTileEntity extends BasinOperatingTileEntity {

	private static final Object shapelessOrMixingRecipesKey = new Object();

	public int runningTicks;
	public int processingTicks;
	public boolean running;

	public MechanicalMixerTileEntity(BellBlockEntity<? extends MechanicalMixerTileEntity> type) {
		super(type);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		int localTick;
		float offset = 0;
		if (running) {
			if (runningTicks < 20) {
				localTick = runningTicks;
				float num = (localTick + partialTicks) / 20f;
				num = ((2 - afj.b((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			} else if (runningTicks <= 20) {
				offset = 1;
			} else {
				localTick = 40 - runningTicks;
				float num = (localTick - partialTicks) / 20f;
				num = ((2 - afj.b((float) (num * Math.PI))) / 2);
				offset = num - .5f;
			}
		}
		return offset + 7 / 16f;
	}

	public float getRenderedHeadRotationSpeed(float partialTicks) {
		float speed = getSpeed();
		if (running) {
			if (runningTicks < 15) {
				return speed;
			}
			if (runningTicks <= 20) {
				return speed * 2;
			}
			return speed;
		}
		return speed / 2;
	}

	@Override
	public Timer getRenderBoundingBox() {
		return new Timer(e).b(0, -1.5, 0);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		runningTicks = compound.getInt("Ticks");
		super.fromTag(state, compound, clientPacket);

		if (clientPacket && n())
			getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Ticks", runningTicks);
		super.write(compound, clientPacket);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (runningTicks >= 40) {
			running = false;
			runningTicks = 0;
			return;
		}

		float speed = Math.abs(getSpeed());
		if (running && d != null) {
			if (d.v && runningTicks == 20)
				renderParticles();

			if (!d.v && runningTicks == 20) {
				if (processingTicks < 0) {
					processingTicks = afj.a((afj.f((int) (512 / speed))) * 15 + 1, 1, 512);
				} else {
					processingTicks--;
					if (processingTicks == 0) {
						runningTicks++;
						processingTicks = -1;
						applyBasinRecipe();
						sendData();
					}
				}
			}

			if (runningTicks != 20)
				runningTicks++;
		}
	}

	public void renderParticles() {
		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent() || d == null)
			return;

		for (SmartInventory inv : basin.get()
			.getInvs()) {
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = inv.a(slot);
				if (stackInSlot.a())
					continue;
				ItemStackParticleEffect data = new ItemStackParticleEffect(ParticleTypes.ITEM, stackInSlot);
				spillParticle(data);
			}
		}

		for (SmartFluidTankBehaviour behaviour : basin.get()
			.getTanks()) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.isEmpty(0))
					continue;
				spillParticle(FluidFX.getFluidParticle(tankSegment.getRenderedFluid()));
			}
		}
	}

	protected void spillParticle(ParticleEffect data) {
		float angle = d.t.nextFloat() * 360;
		EntityHitResult offset = new EntityHitResult(0, 0, 0.25f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		EntityHitResult target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y)
			.b(0, .25f, 0);
		EntityHitResult center = offset.e(VecHelper.getCenterOf(e));
		target = VecHelper.offsetRandomly(target.d(offset), d.t, 1 / 128f);
		d.addParticle(data, center.entity, center.c - 1.75f, center.d, target.entity, target.c, target.d);
	}

	@Override
	protected List<Ingredient<?>> getMatchingRecipes() {
		List<Ingredient<?>> matchingRecipes = super.getMatchingRecipes();

		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent())
			return matchingRecipes;
		IItemHandler availableItems = basin.get()
			.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			.orElse(null);
		if (availableItems == null)
			return matchingRecipes;

		for (int i = 0; i < availableItems.getSlots(); i++) {
			ItemCooldownManager stack = availableItems.getStackInSlot(i);
			if (stack.a())
				continue;

			List<MixingRecipe> list = PotionMixingRecipeManager.ALL.get(stack.b());
			if (list == null)
				continue;
			for (MixingRecipe mixingRecipe : list)
				if (matchBasinRecipe(mixingRecipe))
					matchingRecipes.add(mixingRecipe);
		}

		return matchingRecipes;
	}

	@Override
	protected <C extends BossBar> boolean matchStaticFilters(Ingredient<C> r) {
		return ((r.ag_() == MapExtendingRecipe.b
			&& AllConfigs.SERVER.recipes.allowShapelessInMixer.get() && r.a()
				.size() > 1)
			|| r.g() == AllRecipeTypes.MIXING.type);
	}

	@Override
	public void startProcessingBasin() {
		if (running && runningTicks <= 20)
			return;
		super.startProcessingBasin();
		running = true;
		runningTicks = 0;
	}

	@Override
	public boolean continueWithPreviousRecipe() {
		runningTicks = 20;
		return true;
	}

	@Override
	protected void onBasinRemoved() {
		if (!running)
			return;
		runningTicks = 40;
		running = false;
	}

	@Override
	protected Object getRecipeCacheKey() {
		return shapelessOrMixingRecipesKey;
	}

	@Override
	protected boolean isRunning() {
		return running;
	}

	@Override
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.of(AllTriggers.MIXER_MIX);
	}
}

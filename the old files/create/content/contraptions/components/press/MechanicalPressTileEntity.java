package com.simibubi.kinetic_api.content.contraptions.components.press;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import afj;
import apx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.AllSoundEvents;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinTileEntity;
import com.simibubi.kinetic_api.content.logistics.InWorldProcessing;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.advancement.ITriggerable;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.item.SmartInventory;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends BasinOperatingTileEntity {

	private static final Object compressingRecipesKey = new Object();
	public List<ItemCooldownManager> pressedItems = new ArrayList<>();
	public BeltProcessingBehaviour processingBehaviour;

	public int prevRunningTicks;
	public int runningTicks;
	static final int CYCLE = 240;
	static final int ENTITY_SCAN = 10;
	int entityScanCooldown;

	public boolean running;
	public Mode mode;
	public boolean finished;

	public MechanicalPressTileEntity(BellBlockEntity<? extends MechanicalPressTileEntity> type) {
		super(type);
		mode = Mode.WORLD;
		entityScanCooldown = ENTITY_SCAN;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltPressingCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> BeltPressingCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		mode = Mode.values()[compound.getInt("Mode")];
		finished = compound.getBoolean("Finished");
		prevRunningTicks = runningTicks = compound.getInt("Ticks");
		super.fromTag(state, compound, clientPacket);

		if (clientPacket) {
			NBTHelper.iterateCompoundList(compound.getList("ParticleItems", NBT.TAG_COMPOUND),
				c -> pressedItems.add(ItemCooldownManager.a(c)));
			spawnParticles();
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Mode", mode.ordinal());
		compound.putBoolean("Finished", finished);
		compound.putInt("Ticks", runningTicks);
		super.write(compound, clientPacket);

		if (clientPacket) {
			compound.put("ParticleItems", NBTHelper.writeCompoundList(pressedItems, ItemCooldownManager::serializeNBT));
			pressedItems.clear();
		}
	}

	@Override
	public Timer getRenderBoundingBox() {
		return new Timer(e).b(0, -1.5, 0)
			.b(0, 1, 0);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (!running)
			return 0;
		int runningTicks = Math.abs(this.runningTicks);
		float ticks = afj.g(partialTicks, prevRunningTicks, runningTicks);
		if (runningTicks < (CYCLE * 2) / 3)
			return (float) afj.a(Math.pow(ticks / CYCLE * 2, 3), 0, 1) * mode.headOffset;
		return afj.a((CYCLE - ticks) / CYCLE * 3, 0, 1) * mode.headOffset;
	}

	public void start(Mode mode) {
		this.mode = mode;
		running = true;
		runningTicks = 0;
		pressedItems.clear();
		sendData();
	}

	public boolean inWorld() {
		return mode == Mode.WORLD;
	}

	public boolean onBasin() {
		return mode == Mode.BASIN;
	}

	@Override
	public void aj_() {
		super.aj_();

		if (!running || d == null) {
			if (n() && !d.v) {

				if (getSpeed() == 0)
					return;
				if (entityScanCooldown > 0)
					entityScanCooldown--;
				if (entityScanCooldown <= 0) {
					entityScanCooldown = ENTITY_SCAN;
					if (TileEntityBehaviour.get(d, e.down(2), TransportedItemStackHandlerBehaviour.TYPE) != null)
						return;
					if (AllBlocks.BASIN.has(d.d_(e.down(2))))
						return;

					for (PaintingEntity itemEntity : d.a(PaintingEntity.class,
						new Timer(e.down()).h(.125f))) {
						ItemCooldownManager stack = itemEntity.g();
						Optional<PressingRecipe> recipe = getRecipe(stack);
						if (!recipe.isPresent())
							continue;
						start(Mode.WORLD);
						return;
					}
				}

			}
			return;
		}

		if (d.v && runningTicks == -CYCLE / 2) {
			prevRunningTicks = CYCLE / 2;
			return;
		}

		if (runningTicks == CYCLE / 2 && getSpeed() != 0) {
			if (inWorld())
				applyPressingInWorld();
			if (onBasin())
				applyCompactingOnBasin();
			if (!d.v) {
				d.a(null, o(), AllSoundEvents.MECHANICAL_PRESS_ITEM_BREAK.get(), SoundEvent.e,
					.5f, 1f);
				d.a(null, o(), AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.get(), SoundEvent.e,
					.125f, 1f);
			}
		}

		if (!d.v && runningTicks > CYCLE) {
			finished = true;
			running = false;

			if (onBasin() && matchBasinRecipe(currentRecipe))
				startProcessingBasin();

			pressedItems.clear();
			sendData();
			return;
		}

		prevRunningTicks = runningTicks;
		runningTicks += getRunningTickSpeed();
		if (prevRunningTicks < CYCLE / 2 && runningTicks >= CYCLE / 2) {
			runningTicks = CYCLE / 2;
			// Pause the ticks until a packet is received
			if (d.v)
				runningTicks = -(CYCLE / 2);
		}
	}

	protected void applyCompactingOnBasin() {
		if (d.v)
			return;
		pressedItems.clear();
		applyBasinRecipe();
		Optional<BasinTileEntity> basin = getBasin();
		SmartInventory inputs = basin.get()
			.getInputInventory();
		if (basin.isPresent()) {
			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = inputs.a(slot);
				if (stackInSlot.a())
					continue;
				pressedItems.add(stackInSlot);
			}
		}
		sendData();
	}

	protected void applyPressingInWorld() {
		Timer bb = new Timer(e.down(1));
		pressedItems.clear();
		if (d.v)
			return;
		for (apx entity : d.a(null, bb)) {
			if (!(entity instanceof PaintingEntity))
				continue;
			if (!entity.aW())
				continue;
			PaintingEntity itemEntity = (PaintingEntity) entity;
			pressedItems.add(itemEntity.g());
			sendData();
			Optional<PressingRecipe> recipe = getRecipe(itemEntity.g());
			if (!recipe.isPresent())
				continue;
			InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
			AllTriggers.triggerForNearbyPlayers(AllTriggers.BONK, d, e, 4);
		}
	}

	public int getRunningTickSpeed() {
		if (getSpeed() == 0)
			return 0;
		return (int) afj.g(afj.a(Math.abs(getSpeed()) / 512f, 0, 1), 1, 60);
	}

	protected void spawnParticles() {
		if (pressedItems.isEmpty())
			return;

		if (mode == Mode.BASIN)
			pressedItems.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(e.down(2)), stack));
		if (mode == Mode.BELT)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(e.down(2))
				.b(0, 8 / 16f, 0), stack));
		if (mode == Mode.WORLD)
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(e.down(1))
				.b(0, -1 / 4f, 0), stack));

		pressedItems.clear();
	}

	public void makePressingParticleEffect(EntityHitResult pos, ItemCooldownManager stack) {
		if (d == null || !d.v)
			return;
		for (int i = 0; i < 20; i++) {
			EntityHitResult motion = VecHelper.offsetRandomly(EntityHitResult.a, d.t, .125f)
				.d(1, 0, 1);
			d.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), pos.entity, pos.c - .25f, pos.d, motion.entity,
				motion.c + .125f, motion.d);
		}
	}

	public void makeCompactingParticleEffect(EntityHitResult pos, ItemCooldownManager stack) {
		if (d == null || !d.v)
			return;
		for (int i = 0; i < 20; i++) {
			EntityHitResult motion = VecHelper.offsetRandomly(EntityHitResult.a, d.t, .175f)
				.d(1, 0, 1);
			d.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), pos.entity, pos.c, pos.d, motion.entity,
				motion.c + .25f, motion.d);
		}
	}

	private static final RecipeWrapper pressingInv = new RecipeWrapper(new ItemStackHandler(1));

	public Optional<PressingRecipe> getRecipe(ItemCooldownManager item) {
		pressingInv.a(0, item);
		return AllRecipeTypes.PRESSING.find(pressingInv, d);
	}

	public static boolean canCompress(DefaultedList<FireworkRocketRecipe> ingredients) {
		return AllConfigs.SERVER.recipes.allowShapedSquareInPress.get()
			&& (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.condenseIngredients(ingredients)
				.size() == 1;
	}

	@Override
	protected <C extends BossBar> boolean matchStaticFilters(Ingredient<C> recipe) {
		return (recipe instanceof BlastingRecipe && canCompress(recipe.a()))
			|| recipe.g() == AllRecipeTypes.COMPACTING.type;
	}

	@Override
	protected Object getRecipeCacheKey() {
		return compressingRecipesKey;
	}

	@Override
	public void startProcessingBasin() {
		if (running && runningTicks <= CYCLE / 2)
			return;
		super.startProcessingBasin();
		start(Mode.BASIN);
	}

	@Override
	protected void onBasinRemoved() {
		pressedItems.clear();
		running = false;
		runningTicks = 0;
		sendData();
	}

	@Override
	protected boolean isRunning() {
		return running;
	}

	@Override
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.of(AllTriggers.PRESS_COMPACT);
	}

	enum Mode {
		WORLD(1), BELT(19f / 16f), BASIN(22f / 16f)

		;

		float headOffset;

		Mode(float headOffset) {
			this.headOffset = headOffset;
		}
	}

}

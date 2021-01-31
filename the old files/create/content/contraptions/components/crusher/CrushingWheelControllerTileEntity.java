package com.simibubi.kinetic_api.content.contraptions.components.crusher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import afj;
import apx;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingInventory;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class CrushingWheelControllerTileEntity extends SmartTileEntity {

	public apx processingEntity;
	private UUID entityUUID;
	protected boolean searchForEntity;

	public ProcessingInventory inventory;
	protected LazyOptional<IItemHandlerModifiable> handler = LazyOptional.of(() -> inventory);
	private RecipeWrapper wrapper;
	public float crushingspeed;

	public CrushingWheelControllerTileEntity(BellBlockEntity<? extends CrushingWheelControllerTileEntity> type) {
		super(type);
		inventory = new ProcessingInventory(this::itemInserted) {

			@Override
			public boolean isItemValid(int slot, ItemCooldownManager stack) {
				return super.isItemValid(slot, stack) && processingEntity == null;
			}

		};
		wrapper = new RecipeWrapper(inventory);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
	}

	@Override
	public void aj_() {
		super.aj_();
		if (searchForEntity) {
			searchForEntity = false;
			List<apx> search = d.a(null, new Timer(o()),
				e -> entityUUID.equals(e.bR()));
			if (search.isEmpty())
				clear();
			else
				processingEntity = search.get(0);
		}

		if (!isOccupied())
			return;
		if (crushingspeed == 0)
			return;

		float speed = crushingspeed * 4;
		EntityHitResult outPos = VecHelper.getCenterOf(e);

		if (!hasEntity()) {

			float processingSpeed =
				afj.a((speed) / (!inventory.appliedRecipe ? afj.f(inventory.getStackInSlot(0)
					.E()) : 1), .25f, 20);
			inventory.remainingTime -= processingSpeed;
			spawnParticles(inventory.getStackInSlot(0));

			if (d.v)
				return;

			if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
				applyRecipe();
				inventory.appliedRecipe = true;
				d.a(e, p(), p(), 2 | 16);
				return;
			}

			if (inventory.remainingTime <= 0) {
				for (int slot = 0; slot < inventory.getSlots(); slot++) {
					ItemCooldownManager stack = inventory.getStackInSlot(slot);
					if (stack.a())
						continue;
					PaintingEntity entityIn = new PaintingEntity(d, outPos.entity, outPos.c, outPos.d, stack);
					entityIn.f(EntityHitResult.a);
					entityIn.getPersistentData()
						.put("BypassCrushingWheel", NbtHelper.fromBlockPos(e));
					d.c(entityIn);
				}
				inventory.clear();
				d.a(e, p(), p(), 2 | 16);
				return;
			}

			return;
		}

		if (!processingEntity.aW() || !processingEntity.cb()
			.c(new Timer(e).g(.5f))) {
			clear();
			return;
		}

		double xMotion = ((e.getX() + .5f) - processingEntity.cC()) / 2f;
		double zMotion = ((e.getZ() + .5f) - processingEntity.cG()) / 2f;
		if (processingEntity.bt())
			xMotion = zMotion = 0;

		processingEntity.f(new EntityHitResult(xMotion, Math.max(-speed / 4f, -.5f), zMotion));

		if (d.v)
			return;

		if (!(processingEntity instanceof PaintingEntity)) {
			processingEntity.a(CrushingWheelTileEntity.damageSource,
				AllConfigs.SERVER.kinetics.crushingDamage.get());
			if (!processingEntity.aW()) {
				processingEntity.d(outPos.entity, outPos.c - .75f, outPos.d);
			}
			return;
		}

		PaintingEntity itemEntity = (PaintingEntity) processingEntity;
		itemEntity.a(20);
		if (processingEntity.cD() < e.getY() + .25f) {
			inventory.clear();
			inventory.setStackInSlot(0, itemEntity.g()
				.i());
			itemInserted(inventory.getStackInSlot(0));
			itemEntity.ac();
			d.a(e, p(), p(), 2 | 16);
		}

	}

	protected void spawnParticles(ItemCooldownManager stack) {
		if (stack == null || stack.a())
			return;

		ParticleEffect particleData = null;
		if (stack.b() instanceof BannerItem)
			particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BannerItem) stack.b()).e()
				.n());
		else
			particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);

		Random r = d.t;
		for (int i = 0; i < 4; i++)
			d.addParticle(particleData, e.getX() + r.nextFloat(), e.getY() + r.nextFloat(),
				e.getZ() + r.nextFloat(), 0, 0, 0);
	}

	private void applyRecipe() {
		Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();

		List<ItemCooldownManager> list = new ArrayList<>();
		if (recipe.isPresent()) {
			int rolls = inventory.getStackInSlot(0)
				.E();
			inventory.clear();
			for (int roll = 0; roll < rolls; roll++) {
				List<ItemCooldownManager> rolledResults = recipe.get()
					.rollResults();
				for (int i = 0; i < rolledResults.size(); i++) {
					ItemCooldownManager stack = rolledResults.get(i);
					ItemHelper.addToList(stack, list);
				}
			}
			for (int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++)
				inventory.setStackInSlot(slot + 1, list.get(slot));
		} else {
			inventory.clear();
		}

	}

	public Optional<ProcessingRecipe<RecipeWrapper>> findRecipe() {
		Optional<ProcessingRecipe<RecipeWrapper>> crushingRecipe = AllRecipeTypes.CRUSHING.find(wrapper, d);
		if (!crushingRecipe.isPresent())
			crushingRecipe = AllRecipeTypes.MILLING.find(wrapper, d);
		return crushingRecipe;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (hasEntity())
			compound.put("Entity", NbtHelper.fromUuid(entityUUID));
		compound.put("Inventory", inventory.serializeNBT());
		compound.putFloat("Speed", crushingspeed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (compound.contains("Entity") && !isOccupied()) {
			entityUUID = NbtHelper.toUuid(NBTHelper.getINBT(compound, "Entity"));
			this.searchForEntity = true;
		}
		crushingspeed = compound.getFloat("Speed");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
	}

	public void startCrushing(apx entity) {
		processingEntity = entity;
		entityUUID = entity.bR();
	}

	private void itemInserted(ItemCooldownManager stack) {
		Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();
		inventory.remainingTime = recipe.isPresent() ? recipe.get()
			.getProcessingDuration() : 100;
		inventory.appliedRecipe = false;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return handler.cast();
		return super.getCapability(cap, side);
	}

	public void clear() {
		processingEntity = null;
		entityUUID = null;
	}

	public boolean isOccupied() {
		return hasEntity() || !inventory.isEmpty();
	}

	public boolean hasEntity() {
		return processingEntity != null;
	}

}

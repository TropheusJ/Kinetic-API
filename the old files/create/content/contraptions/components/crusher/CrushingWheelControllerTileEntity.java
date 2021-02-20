package com.simibubi.create.content.contraptions.components.crusher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class CrushingWheelControllerTileEntity extends SmartTileEntity {

	public Entity processingEntity;
	private UUID entityUUID;
	protected boolean searchForEntity;

	public ProcessingInventory inventory;
	protected LazyOptional<IItemHandlerModifiable> handler = LazyOptional.of(() -> inventory);
	private RecipeWrapper wrapper;
	public float crushingspeed;

	public CrushingWheelControllerTileEntity(BlockEntityType<? extends CrushingWheelControllerTileEntity> type) {
		super(type);
		inventory = new ProcessingInventory(this::itemInserted) {

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
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
	public void tick() {
		super.tick();
		if (searchForEntity) {
			searchForEntity = false;
			List<Entity> search = world.getOtherEntities(null, new Box(getPos()),
				e -> entityUUID.equals(e.getUuid()));
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
		Vec3d outPos = VecHelper.getCenterOf(pos);

		if (!hasEntity()) {

			float processingSpeed =
				MathHelper.clamp((speed) / (!inventory.appliedRecipe ? MathHelper.log2(inventory.getStackInSlot(0)
					.getCount()) : 1), .25f, 20);
			inventory.remainingTime -= processingSpeed;
			spawnParticles(inventory.getStackInSlot(0));

			if (world.isClient)
				return;

			if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
				applyRecipe();
				inventory.appliedRecipe = true;
				world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);
				return;
			}

			if (inventory.remainingTime <= 0) {
				for (int slot = 0; slot < inventory.getSlots(); slot++) {
					ItemStack stack = inventory.getStackInSlot(slot);
					if (stack.isEmpty())
						continue;
					ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
					entityIn.setVelocity(Vec3d.ZERO);
					entityIn.getPersistentData()
						.put("BypassCrushingWheel", NbtHelper.fromBlockPos(pos));
					world.spawnEntity(entityIn);
				}
				inventory.clear();
				world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);
				return;
			}

			return;
		}

		if (!processingEntity.isAlive() || !processingEntity.getBoundingBox()
			.intersects(new Box(pos).expand(.5f))) {
			clear();
			return;
		}

		double xMotion = ((pos.getX() + .5f) - processingEntity.getX()) / 2f;
		double zMotion = ((pos.getZ() + .5f) - processingEntity.getZ()) / 2f;
		if (processingEntity.isSneaking())
			xMotion = zMotion = 0;

		processingEntity.setVelocity(new Vec3d(xMotion, Math.max(-speed / 4f, -.5f), zMotion));

		if (world.isClient)
			return;

		if (!(processingEntity instanceof ItemEntity)) {
			processingEntity.damage(CrushingWheelTileEntity.damageSource,
				AllConfigs.SERVER.kinetics.crushingDamage.get());
			if (!processingEntity.isAlive()) {
				processingEntity.updatePosition(outPos.x, outPos.y - .75f, outPos.z);
			}
			return;
		}

		ItemEntity itemEntity = (ItemEntity) processingEntity;
		itemEntity.setPickupDelay(20);
		if (processingEntity.getY() < pos.getY() + .25f) {
			inventory.clear();
			inventory.setStackInSlot(0, itemEntity.getStack()
				.copy());
			itemInserted(inventory.getStackInSlot(0));
			itemEntity.remove();
			world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);
		}

	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		ParticleEffect particleData = null;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock()
				.getDefaultState());
		else
			particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);

		Random r = world.random;
		for (int i = 0; i < 4; i++)
			world.addParticle(particleData, pos.getX() + r.nextFloat(), pos.getY() + r.nextFloat(),
				pos.getZ() + r.nextFloat(), 0, 0, 0);
	}

	private void applyRecipe() {
		Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();

		List<ItemStack> list = new ArrayList<>();
		if (recipe.isPresent()) {
			int rolls = inventory.getStackInSlot(0)
				.getCount();
			inventory.clear();
			for (int roll = 0; roll < rolls; roll++) {
				List<ItemStack> rolledResults = recipe.get()
					.rollResults();
				for (int i = 0; i < rolledResults.size(); i++) {
					ItemStack stack = rolledResults.get(i);
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
		Optional<ProcessingRecipe<RecipeWrapper>> crushingRecipe = AllRecipeTypes.CRUSHING.find(wrapper, world);
		if (!crushingRecipe.isPresent())
			crushingRecipe = AllRecipeTypes.MILLING.find(wrapper, world);
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
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (compound.contains("Entity") && !isOccupied()) {
			entityUUID = NbtHelper.toUuid(NBTHelper.getINBT(compound, "Entity"));
			this.searchForEntity = true;
		}
		crushingspeed = compound.getFloat("Speed");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
	}

	public void startCrushing(Entity entity) {
		processingEntity = entity;
		entityUUID = entity.getUuid();
	}

	private void itemInserted(ItemStack stack) {
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

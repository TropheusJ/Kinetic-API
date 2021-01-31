package com.simibubi.kinetic_api.content.contraptions.components.saw;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import afj;
import com.google.common.base.Predicate;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.AllTags;
import com.simibubi.kinetic_api.content.contraptions.components.actors.BlockBreakingKineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingInventory;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter;
import com.simibubi.kinetic_api.foundation.utility.TreeCutter.Tree;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import com.simibubi.kinetic_api.foundation.utility.recipe.RecipeConditions;
import com.simibubi.kinetic_api.foundation.utility.recipe.RecipeFinder;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SawTileEntity extends BlockBreakingKineticTileEntity {

	private static final Object cuttingRecipesKey = new Object();

	public ProcessingInventory inventory;
	private int recipeIndex;
	private LazyOptional<IItemHandler> invProvider = LazyOptional.empty();
	private FilteringBehaviour filtering;

	public SawTileEntity(BellBlockEntity<? extends SawTileEntity> type) {
		super(type);
		inventory = new ProcessingInventory(this::start);
		inventory.remainingTime = -1;
		recipeIndex = 0;
		invProvider = LazyOptional.of(() -> inventory);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new SawFilterSlot()).forRecipes();
		behaviours.add(filtering);
		behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnelsWhen(this::canProcess));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());
		compound.putInt("RecipeIndex", recipeIndex);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		recipeIndex = compound.getInt("RecipeIndex");
	}

	@Override
	public void aj_() {
		if (shouldRun() && ticksUntilNextProgress < 0)
			destroyNextTick();
		super.aj_();

		if (!canProcess())
			return;
		if (getSpeed() == 0)
			return;
		if (inventory.remainingTime == -1) {
			if (!inventory.isEmpty() && !inventory.appliedRecipe)
				start(inventory.getStackInSlot(0));
			return;
		}

		float processingSpeed = afj.a(Math.abs(getSpeed()) / 32, 1, 128);
		inventory.remainingTime -= processingSpeed;

		if (inventory.remainingTime > 0)
			spawnParticles(inventory.getStackInSlot(0));

		if (d.v)
			return;

		if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
			applyRecipe();
			inventory.appliedRecipe = true;
			sendData();
			return;
		}

		EntityHitResult itemMovement = getItemMovementVec();
		Direction itemMovementFacing = Direction.getFacing(itemMovement.entity, itemMovement.c, itemMovement.d);
		if (inventory.remainingTime > 0)
			return;
		inventory.remainingTime = 0;

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemCooldownManager stack = inventory.getStackInSlot(slot);
			if (stack.a())
				continue;
			ItemCooldownManager tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
				.tryExportingToBeltFunnel(stack, itemMovementFacing.getOpposite());
			if (tryExportingToBeltFunnel.E() != stack.E()) {
				inventory.setStackInSlot(slot, tryExportingToBeltFunnel);
				notifyUpdate();
				return;
			}
		}

		BlockPos nextPos = e.add(itemMovement.entity, itemMovement.c, itemMovement.d);
		DirectBeltInputBehaviour behaviour = TileEntityBehaviour.get(d, nextPos, DirectBeltInputBehaviour.TYPE);
		if (behaviour != null) {
			boolean changed = false;
			if (!behaviour.canInsertFromSide(itemMovementFacing))
				return;
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemCooldownManager stack = inventory.getStackInSlot(slot);
				if (stack.a())
					continue;
				ItemCooldownManager remainder = behaviour.handleInsertion(stack, itemMovementFacing, false);
				if (remainder.equals(stack, false))
					continue;
				inventory.setStackInSlot(slot, remainder);
				changed = true;
			}
			if (changed) {
				X_();
				sendData();
			}
			return;
		}

		// Eject Items
		EntityHitResult outPos = VecHelper.getCenterOf(e)
			.e(itemMovement.a(.5f)
				.b(0, .5, 0));
		EntityHitResult outMotion = itemMovement.a(.0625)
			.b(0, .125, 0);
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemCooldownManager stack = inventory.getStackInSlot(slot);
			if (stack.a())
				continue;
			PaintingEntity entityIn = new PaintingEntity(d, outPos.entity, outPos.c, outPos.d, stack);
			entityIn.f(outMotion);
			d.c(entityIn);
		}
		inventory.clear();
		d.c(e, p().b());
		inventory.remainingTime = -1;
		sendData();
	}

	@Override
	public void al_() {
		invProvider.invalidate();
		super.al_();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != Direction.DOWN)
			return invProvider.cast();
		return super.getCapability(cap, side);
	}

	protected void spawnParticles(ItemCooldownManager stack) {
		if (stack == null || stack.a())
			return;

		ParticleEffect particleData = null;
		float speed = 1;
		if (stack.b() instanceof BannerItem)
			particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BannerItem) stack.b()).e()
				.n());
		else {
			particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
			speed = .125f;
		}

		Random r = d.t;
		EntityHitResult vec = getItemMovementVec();
		EntityHitResult pos = VecHelper.getCenterOf(this.e);
		float offset = inventory.recipeDuration != 0 ? (float) (inventory.remainingTime) / inventory.recipeDuration : 0;
		offset -= .5f;
		d.addParticle(particleData, pos.getX() + -vec.entity * offset, pos.getY() + .45f, pos.getZ() + -vec.d * offset,
			-vec.entity * speed, r.nextFloat() * speed, -vec.d * speed);
	}

	public EntityHitResult getItemMovementVec() {
		boolean alongX = !p().c(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		int offset = getSpeed() < 0 ? -1 : 1;
		return new EntityHitResult(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
	}

	private void applyRecipe() {
		List<? extends Ingredient<?>> recipes = getRecipes();
		if (recipes.isEmpty())
			return;
		if (recipeIndex >= recipes.size())
			recipeIndex = 0;

		Ingredient<?> recipe = recipes.get(recipeIndex);

		int rolls = inventory.getStackInSlot(0)
			.E();
		inventory.clear();

		List<ItemCooldownManager> list = new ArrayList<>();
		for (int roll = 0; roll < rolls; roll++) {
			List<ItemCooldownManager> results = new LinkedList<ItemCooldownManager>();
			if (recipe instanceof CuttingRecipe)
				results = ((CuttingRecipe) recipe).rollResults();
			else if (recipe instanceof CuttingRecipe)
				results.add(recipe.c()
					.i());

			for (int i = 0; i < results.size(); i++) {
				ItemCooldownManager stack = results.get(i);
				ItemHelper.addToList(stack, list);
			}
		}
		for (int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++)
			inventory.setStackInSlot(slot + 1, list.get(slot));

	}

	private List<? extends Ingredient<?>> getRecipes() {
		Predicate<Ingredient<?>> types = AllConfigs.SERVER.recipes.allowStonecuttingOnSaw.get()
			? RecipeConditions.isOfType(Recipe.f, AllRecipeTypes.CUTTING.getType())
			: RecipeConditions.isOfType(AllRecipeTypes.CUTTING.getType());
		List<Ingredient<?>> startedSearch = RecipeFinder.get(cuttingRecipesKey, d, types);
		return startedSearch.stream()
			.filter(RecipeConditions.outputMatchesFilter(filtering))
			.filter(RecipeConditions.firstIngredientMatches(inventory.getStackInSlot(0)))
			.collect(Collectors.toList());
	}

	public void insertItem(PaintingEntity entity) {
		if (!canProcess())
			return;
		if (!inventory.isEmpty())
			return;
		if (!entity.aW())
			return;
		if (d.v)
			return;

		inventory.clear();
		inventory.insertItem(0, entity.g()
			.i(), false);
		entity.ac();
	}

	public void start(ItemCooldownManager inserted) {
		if (!canProcess())
			return;
		if (inventory.isEmpty())
			return;
		if (d.v)
			return;

		List<? extends Ingredient<?>> recipes = getRecipes();
		boolean valid = !recipes.isEmpty();
		int time = 100;

		if (recipes.isEmpty()) {
			inventory.remainingTime = inventory.recipeDuration = 10;
			inventory.appliedRecipe = false;
			sendData();
			return;
		}

		if (valid) {
			recipeIndex++;
			if (recipeIndex >= recipes.size())
				recipeIndex = 0;
		}

		Ingredient<?> recipe = recipes.get(recipeIndex);
		if (recipe instanceof com.simibubi.kinetic_api.content.contraptions.components.saw.CuttingRecipe) {
			time = ((com.simibubi.kinetic_api.content.contraptions.components.saw.CuttingRecipe) recipe).getProcessingDuration();
		}

		inventory.remainingTime = time * Math.max(1, (inserted.E() / 5));
		inventory.recipeDuration = inventory.remainingTime;
		inventory.appliedRecipe = false;
		sendData();
	}

	protected boolean canProcess() {
		return p().c(SawBlock.FACING) == Direction.UP;
	}

	// Block Breaker

	@Override
	protected boolean shouldRun() {
		return p().c(SawBlock.FACING)
			.getAxis()
			.isHorizontal();
	}

	@Override
	protected BlockPos getBreakingPos() {
		return o().offset(p().c(SawBlock.FACING));
	}

	@Override
	public void onBlockBroken(PistonHandler stateToBreak) {
		super.onBlockBroken(stateToBreak);
		Tree tree = TreeCutter.cutTree(d, breakingPos);
		if (tree != null) {
			for (BlockPos log : tree.logs)
				BlockHelper.destroyBlock(d, log, 1 / 2f, stack -> dropItemFromCutTree(log, stack));
			for (BlockPos leaf : tree.leaves)
				BlockHelper.destroyBlock(d, leaf, 1 / 8f, stack -> dropItemFromCutTree(leaf, stack));
		}
	}

	public void dropItemFromCutTree(BlockPos pos, ItemCooldownManager stack) {
		float distance = (float) Math.sqrt(pos.getSquaredDistance(breakingPos));
		EntityHitResult dropPos = VecHelper.getCenterOf(pos);
		PaintingEntity entity = new PaintingEntity(d, dropPos.entity, dropPos.c, dropPos.d, stack);
		entity.f(EntityHitResult.b(breakingPos.subtract(this.e)).a(distance / 20f));
		d.c(entity);
	}

	@Override
	public boolean canBreak(PistonHandler stateToBreak, float blockHardness) {
		boolean sawable = isSawable(stateToBreak);
		return super.canBreak(stateToBreak, blockHardness) && sawable;
	}

	public static boolean isSawable(PistonHandler stateToBreak) {
		if (stateToBreak.a(StatHandler.s) || AllTags.AllBlockTags.SLIMY_LOGS.matches(stateToBreak) || stateToBreak.a(StatHandler.I))
			return true;
		BeetrootsBlock block = stateToBreak.b();
		if (block instanceof AirBlock)
			return true;
		if (block instanceof StairsBlock)
			return true;
		if (block instanceof FluidDrainable)
			return true;
		if (block instanceof StonecutterBlock)
			return true;
		if (block instanceof JigsawBlock)
			return true;
		if (block instanceof PaneBlock)
			return true;
		if (block instanceof ChainBlock)
			return true;
		return false;
	}

}

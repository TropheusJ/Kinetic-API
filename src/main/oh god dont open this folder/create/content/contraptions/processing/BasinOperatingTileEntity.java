package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.recipe.Ingredient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.ITriggerable;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

public abstract class BasinOperatingTileEntity extends KineticTileEntity {

	public DeferralBehaviour basinChecker;
	public boolean basinRemoved;
	protected Ingredient<?> currentRecipe;

	public BasinOperatingTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		basinChecker = new DeferralBehaviour(this, this::updateBasin);
		behaviours.add(basinChecker);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (getSpeed() == 0)
			basinRemoved = true;
		basinRemoved = false;
		basinChecker.scheduleUpdate();
	}

	@Override
	public void aj_() {
		if (basinRemoved) {
			basinRemoved = false;
			onBasinRemoved();
			sendData();
			return;
		}

		super.aj_();
	}

	protected boolean updateBasin() {
		if (!isSpeedRequirementFulfilled())
			return true;
		if (getSpeed() == 0)
			return true;
		if (isRunning())
			return false;
		if (d == null || d.v)
			return true;

		List<Ingredient<?>> recipes = getMatchingRecipes();
		if (recipes.isEmpty())
			return true;
		currentRecipe = recipes.get(0);
		startProcessingBasin();
		sendData();
		return true;
	}

	protected abstract boolean isRunning();

	public void startProcessingBasin() {}

	public boolean continueWithPreviousRecipe() {
		return true;
	}

	protected <C extends BossBar> boolean matchBasinRecipe(Ingredient<C> recipe) {
		if (recipe == null)
			return false;
		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent())
			return false;
		return BasinRecipe.match(basin.get(), recipe);
	}
	
	protected void applyBasinRecipe() {
		if (currentRecipe == null)
			return;
		
		Optional<BasinTileEntity> optionalBasin = getBasin();
		if (!optionalBasin.isPresent())
			return;
		BasinTileEntity basin = optionalBasin.get();
		if (!BasinRecipe.apply(basin, currentRecipe))
			return;
		Optional<ITriggerable> processedRecipeTrigger = getProcessedRecipeTrigger();
		if (d != null && !d.v && processedRecipeTrigger.isPresent()) 
			AllTriggers.triggerForNearbyPlayers(processedRecipeTrigger.get(), d, e, 4);
		basin.inputTank.sendDataImmediately();
	
		// Continue mixing
		if (matchBasinRecipe(currentRecipe)) {
			continueWithPreviousRecipe();
			sendData();
		}

		basin.notifyChangeOfContents();
	}

	protected List<Ingredient<?>> getMatchingRecipes() {
		List<Ingredient<?>> list = RecipeFinder.get(getRecipeCacheKey(), d, this::matchStaticFilters);
		return list.stream()
			.filter(this::matchBasinRecipe)
			.sorted((r1, r2) -> r2.a()
				.size()
				- r1.a()
					.size())
			.collect(Collectors.toList());
	}

	protected abstract void onBasinRemoved();

	protected Optional<BasinTileEntity> getBasin() {
		if (d == null)
			return Optional.empty();
		BeehiveBlockEntity basinTE = d.c(e.down(2));
		if (!(basinTE instanceof BasinTileEntity))
			return Optional.empty();
		return Optional.of((BasinTileEntity) basinTE);
	}
	
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.empty();
	}

	protected abstract <C extends BossBar> boolean matchStaticFilters(Ingredient<C> recipe);

	protected abstract Object getRecipeCacheKey();

}

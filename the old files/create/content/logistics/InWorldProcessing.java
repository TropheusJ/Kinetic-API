package com.simibubi.kinetic_api.content.logistics;

import static com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllRecipeTypes;
import com.simibubi.kinetic_api.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InWorldProcessing {

	public static class SplashingInv extends RecipeWrapper {
		public SplashingInv() {
			super(new ItemStackHandler(1));
		}
	}

	public static SplashingInv splashingInv = new SplashingInv();

	public enum Type {
		SMOKING, BLASTING, SPLASHING

		;

		public static Type byBlock(MobSpawnerLogic reader, BlockPos pos) {
			PistonHandler blockState = reader.d_(pos);
			EmptyFluid fluidState = reader.b(pos);
			if (fluidState.a() == FlowableFluid.c || fluidState.a() == FlowableFluid.LEVEL)
				return Type.SPLASHING;
			BeetrootsBlock block = blockState.b();
			if (block == BellBlock.bN || AllBlocks.LIT_BLAZE_BURNER.has(blockState)
				|| (block == BellBlock.me && blockState.c(AbstractButtonBlock.CEILING_X_SHAPE))
				|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING)
				return Type.SMOKING;
			if (block == BellBlock.B || getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
				return Type.BLASTING;
			return null;
		}
	}

	public static boolean canProcess(PaintingEntity entity, Type type) {
		if (entity.getPersistentData()
			.contains("CreateData")) {
			CompoundTag compound = entity.getPersistentData()
				.getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundTag processing = compound.getCompound("Processing");

				if (Type.valueOf(processing.getString("Type")) != type) {
					boolean canProcess = canProcess(entity.g(), type, entity.l);
					processing.putString("Type", type.name());
					if (!canProcess)
						processing.putInt("Time", -1);
					return canProcess;
				} else if (processing.getInt("Time") >= 0)
					return true;
				else if (processing.getInt("Time") == -1)
					return false;
			}
		}
		return canProcess(entity.g(), type, entity.l);
	}

	private static boolean canProcess(ItemCooldownManager stack, Type type, GameMode world) {
		if (type == Type.BLASTING) {
			return true;
		}

		if (type == Type.SMOKING) {
			// FIXME this does not need to be a TE
			ShulkerBoxBlockEntity smoker = new ShulkerBoxBlockEntity();
			smoker.a(world, BlockPos.ORIGIN);
			smoker.a(0, stack);
			Optional<SpecialRecipeSerializer> recipe = world.o()
				.a(Recipe.d, smoker, world);
			return recipe.isPresent();
		}

		if (type == Type.SPLASHING)
			return isWashable(stack, world);

		return false;
	}

	public static boolean isWashable(ItemCooldownManager stack, GameMode world) {
		splashingInv.a(0, stack);
		Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(splashingInv, world);
		return recipe.isPresent();
	}

	public static void applyProcessing(PaintingEntity entity, Type type) {
		if (decrementProcessingTime(entity, type) != 0)
			return;
		List<ItemCooldownManager> stacks = process(entity.g(), type, entity.l);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.ac();
			return;
		}
		entity.b(stacks.remove(0));
		for (ItemCooldownManager additional : stacks) {
			PaintingEntity entityIn = new PaintingEntity(entity.l, entity.cC(), entity.cD(), entity.cG(), additional);
			entityIn.f(entity.cB());
			entity.l.c(entityIn);
		}
	}

	public static TransportedResult applyProcessing(TransportedItemStack transported, GameMode world, Type type) {
		TransportedResult ignore = TransportedResult.doNothing();
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.E() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!canProcess(transported.stack, type, world))
				transported.processingTime = -1;
			return ignore;
		}
		if (transported.processingTime == -1)
			return ignore;
		if (transported.processingTime-- > 0)
			return ignore;

		List<ItemCooldownManager> stacks = process(transported.stack, type, world);
		if (stacks == null)
			return ignore;

		List<TransportedItemStack> transportedStacks = new ArrayList<>();
		for (ItemCooldownManager additional : stacks) {
			TransportedItemStack newTransported = transported.getSimilar();
			newTransported.stack = additional.i();
			transportedStacks.add(newTransported);
		}
		return TransportedResult.convertTo(transportedStacks);
	}

	private static List<ItemCooldownManager> process(ItemCooldownManager stack, Type type, GameMode world) {
		if (type == Type.SPLASHING) {
			splashingInv.a(0, stack);
			Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(splashingInv, world);
			if (recipe.isPresent())
				return applyRecipeOn(stack, recipe.get());
			return null;
		}

		// FIXME this does not need to be a TE
		ShulkerBoxBlockEntity smoker = new ShulkerBoxBlockEntity();
		smoker.a(world, BlockPos.ORIGIN);
		smoker.a(0, stack);
		Optional<SpecialRecipeSerializer> smokingRecipe = world.o()
			.a(Recipe.d, smoker, world);

		if (type == Type.BLASTING) {
			// FIXME this does not need to be a TE
			DropperBlockEntity furnace = new DropperBlockEntity();
			furnace.a(world, BlockPos.ORIGIN);
			furnace.a(0, stack);
			Optional<CookingRecipeSerializer> smeltingRecipe = world.o()
				.a(Recipe.b, furnace, world);

			if (!smokingRecipe.isPresent()) {
				if (smeltingRecipe.isPresent())
					return applyRecipeOn(stack, smeltingRecipe.get());

				// FIXME this does not need to be a TE
				BedBlockEntity blastFurnace = new BedBlockEntity();
				blastFurnace.a(world, BlockPos.ORIGIN);
				blastFurnace.a(0, stack);
				Optional<AbstractCookingRecipe> blastingRecipe = world.o()
					.a(Recipe.c, blastFurnace, world);

				if (blastingRecipe.isPresent())
					return applyRecipeOn(stack, blastingRecipe.get());
			}

			return Collections.emptyList();
		}

		if (type == Type.SMOKING && smokingRecipe.isPresent())
			return applyRecipeOn(stack, smokingRecipe.get());

		return null;
	}

	private static int decrementProcessingTime(PaintingEntity entity, Type type) {
		CompoundTag nbt = entity.getPersistentData();

		if (!nbt.contains("CreateData"))
			nbt.put("CreateData", new CompoundTag());
		CompoundTag createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			createData.put("Processing", new CompoundTag());
		CompoundTag processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || Type.valueOf(processing.getString("Type")) != type) {
			processing.putString("Type", type.name());
			int timeModifierForStackSize = ((entity.g()
				.E() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			processing.putInt("Time", processingTime);
		}

		int value = processing.getInt("Time") - 1;
		processing.putInt("Time", value);
		return value;
	}

	public static void applyRecipeOn(PaintingEntity entity, Ingredient<?> recipe) {
		List<ItemCooldownManager> stacks = applyRecipeOn(entity.g(), recipe);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.ac();
			return;
		}
		entity.b(stacks.remove(0));
		for (ItemCooldownManager additional : stacks) {
			PaintingEntity entityIn = new PaintingEntity(entity.l, entity.cC(), entity.cD(), entity.cG(), additional);
			entityIn.f(entity.cB());
			entity.l.c(entityIn);
		}
	}

	private static List<ItemCooldownManager> applyRecipeOn(ItemCooldownManager stackIn, Ingredient<?> recipe) {
		List<ItemCooldownManager> stacks;

		if (recipe instanceof ProcessingRecipe) {
			stacks = new ArrayList<>();
			for (int i = 0; i < stackIn.E(); i++) {
				List<ItemCooldownManager> rollResults = ((ProcessingRecipe<?>) recipe).rollResults();
				for (ItemCooldownManager stack : rollResults) {
					for (ItemCooldownManager previouslyRolled : stacks) {
						if (stack.a())
							continue;
						if (!ItemHandlerHelper.canItemStacksStack(stack, previouslyRolled))
							continue;
						int amount = Math.min(previouslyRolled.c() - previouslyRolled.E(),
							stack.E());
						previouslyRolled.f(amount);
						stack.g(amount);
					}

					if (stack.a())
						continue;

					stacks.add(stack);
				}
			}
		} else {
			ItemCooldownManager out = recipe.c()
				.i();
			stacks = ItemHelper.multipliedOutput(stackIn, out);
		}

		return stacks;
	}
	public static void spawnParticlesForProcessing(@Nullable GameMode world, EntityHitResult vec, Type type) {
		if (world == null || !world.v)
			return;
		if (world.t.nextInt(8) != 0)
			return;

		switch (type) {
		case BLASTING:
			world.addParticle(ParticleTypes.LARGE_SMOKE, vec.entity, vec.c + .25f, vec.d, 0, 1 / 16f, 0);
			break;
		case SMOKING:
			world.addParticle(ParticleTypes.POOF, vec.entity, vec.c + .25f, vec.d, 0, 1 / 16f, 0);
			break;
		case SPLASHING:
			EntityHitResult color = ColorHelper.getRGB(0x0055FF);
			world.addParticle(new DustParticleEffect((float) color.entity, (float) color.c, (float) color.d, 1),
				vec.entity + (world.t.nextFloat() - .5f) * .5f, vec.c + .5f, vec.d + (world.t.nextFloat() - .5f) * .5f,
				0, 1 / 8f, 0);
			world.addParticle(ParticleTypes.SPIT, vec.entity + (world.t.nextFloat() - .5f) * .5f, vec.c + .5f,
				vec.d + (world.t.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
			break;
		default:
			break;
		}
	}

}

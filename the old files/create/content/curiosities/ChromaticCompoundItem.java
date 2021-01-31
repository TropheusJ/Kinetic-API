package com.simibubi.kinetic_api.content.curiosities;

import java.util.List;
import java.util.Random;
import afj;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.config.CRecipes;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.decorator.Decoratable;
import net.minecraft.world.timer.Timer;

public class ChromaticCompoundItem extends HoeItem {

	public ChromaticCompoundItem(a properties) {
		super(properties);
	}

	@Override
	public boolean n() {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemCooldownManager stack) {
		int light = stack.p()
			.getInt("CollectingLight");
		return 1 - light / (float) AllConfigs.SERVER.recipes.lightSourceCountForRefinedRadiance.get();
	}

	@Override
	public boolean showDurabilityBar(ItemCooldownManager stack) {
		int light = stack.p()
			.getInt("CollectingLight");
		return light > 0;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemCooldownManager stack) {
		return ColorHelper.mixColors(0x413c69, 0xFFFFFF, (float) (1 - getDurabilityForDisplay(stack)));
	}

	@Override
	public int getItemStackLimit(ItemCooldownManager stack) {
		return showDurabilityBar(stack) ? 1 : 16;
	}

	@Override
	public boolean onEntityItemUpdate(ItemCooldownManager stack, PaintingEntity entity) {
		double y = entity.cD();
		double yMotion = entity.cB().c;
		GameMode world = entity.l;
		CompoundTag data = entity.getPersistentData();
		CompoundTag itemData = entity.g()
			.p();

		EntityHitResult positionVec = entity.cz();
		CRecipes config = AllConfigs.SERVER.recipes;
		if (world.v) {
			int light = itemData.getInt("CollectingLight");
			if (h.nextInt(config.lightSourceCountForRefinedRadiance.get() + 20) < light) {
				EntityHitResult start = VecHelper.offsetRandomly(positionVec, h, 3);
				EntityHitResult motion = positionVec.d(start)
					.d()
					.a(.2f);
				world.addParticle(ParticleTypes.END_ROD, start.entity, start.c, start.d, motion.entity, motion.c, motion.d);
			}
			return false;
		}

		// Convert to Shadow steel if in void
		if (y < 0 && y - yMotion < -10 && config.enableShadowSteelRecipe.get()) {
			ItemCooldownManager newStack = AllItems.SHADOW_STEEL.asStack();
			newStack.e(stack.E());
			data.putBoolean("FromVoid", true);
			entity.b(newStack);
		}

		if (!config.enableRefinedRadianceRecipe.get())
			return false;

		// Convert to Refined Radiance if eaten enough light sources
		if (itemData.getInt("CollectingLight") >= config.lightSourceCountForRefinedRadiance.get()) {
			ItemCooldownManager newStack = AllItems.REFINED_RADIANCE.asStack();
			PaintingEntity newEntity = new PaintingEntity(world, entity.cC(), entity.cD(), entity.cG(), newStack);
			newEntity.f(entity.cB());
			newEntity.getPersistentData()
				.putBoolean("FromLight", true);
			itemData.remove("CollectingLight");
			world.c(newEntity);

			stack.a(1);
			entity.b(stack);
			if (stack.a())
				entity.ac();
			return false;
		}

		// Is inside beacon beam?
		boolean isOverBeacon = false;
		int entityX = afj.c(entity.cC());
		int entityZ = afj.c(entity.cG());
		int localWorldHeight = world.a(Decoratable.a.b, entityX, entityZ);

		BlockPos.Mutable testPos = new BlockPos.Mutable(
				entityX,
				Math.min(afj.c(entity.cD()), localWorldHeight),
				entityZ);

		while (testPos.getY() > 0) {
			testPos.move(Direction.DOWN);
			PistonHandler state = world.d_(testPos);
			if (state.b(world, testPos) >= 15 && state.b() != BellBlock.z)
				break;
			if (state.b() == BellBlock.es) {
				BeehiveBlockEntity te = world.c(testPos);

				if (!(te instanceof BannerPattern)) break;

				BannerPattern bte = (BannerPattern) te;

				if (bte.h() != 0 && !bte.field_11806.isEmpty()) isOverBeacon = true;

				break;
			}
		}

		if (isOverBeacon) {
			ItemCooldownManager newStack = AllItems.REFINED_RADIANCE.asStack();
			newStack.e(stack.E());
			data.putBoolean("FromLight", true);
			entity.b(newStack);

			List<ServerPlayerEntity> players =
				world.a(ServerPlayerEntity.class, new Timer(entity.cA()).g(8));
			players.forEach(AllTriggers.ABSORBED_LIGHT::trigger);

			return false;
		}

		// Find a light source and eat it.
		Random r = world.t;
		int range = 3;
		float rate = 1 / 2f;
		if (r.nextFloat() > rate)
			return false;

		BlockPos randomOffset = new BlockPos(VecHelper.offsetRandomly(positionVec, r, range));
		PistonHandler state = world.d_(randomOffset);
		if (state.getLightValue(world, randomOffset) == 0)
			return false;
		if (state.h(world, randomOffset) == -1)
			return false;
		if (state.b() == BellBlock.es)
			return false;

		BlockView context = new BlockView(positionVec, VecHelper.getCenterOf(randomOffset),
			net.minecraft.world.BlockView.a.a, b.a, entity);
		if (!randomOffset.equals(world.a(context)
			.a()))
			return false;

		world.b(randomOffset, false);

		ItemCooldownManager newStack = stack.a(1);
		newStack.p()
			.putInt("CollectingLight", itemData.getInt("CollectingLight") + 1);
		PaintingEntity newEntity = new PaintingEntity(world, entity.cC(), entity.cD(), entity.cG(), newStack);
		newEntity.f(entity.cB());
		newEntity.m();
		world.c(newEntity);
		entity.lifespan = 6000;
		if (stack.a())
			entity.ac();

		return false;
	}

}

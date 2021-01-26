package com.simibubi.create.content.curiosities;

import afj;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.HoeItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;

public class RefinedRadianceItem extends HoeItem {

	public RefinedRadianceItem(a properties) {
		super(properties);
	}
	
	@Override
	public boolean e(ItemCooldownManager stack) {
		return true;
	}

	@Override
	public boolean onEntityItemUpdate(ItemCooldownManager stack, PaintingEntity entity) {
		GameMode world = entity.l;
		EntityHitResult pos = entity.cz();

		if (world.v && entity.aA()) {
			if (world.t.nextFloat() < afj.a(entity.g().E() - 10, 1, 100) / 64f) {
				EntityHitResult ppos = VecHelper.offsetRandomly(pos, world.t, .5f);
				world.addParticle(ParticleTypes.END_ROD, ppos.entity, pos.c, ppos.d, 0, -.1f, 0);
			}

			if (!entity.getPersistentData().contains("ClientAnimationPlayed")) {
				EntityHitResult basemotion = new EntityHitResult(0, 1, 0);
				world.addParticle(ParticleTypes.FLASH, pos.entity, pos.c, pos.d, 0, 0, 0);
				for (int i = 0; i < 20; i++) {
					EntityHitResult motion = VecHelper.offsetRandomly(basemotion, world.t, 1);
					world.addParticle(ParticleTypes.WITCH, pos.entity, pos.c, pos.d, motion.entity, motion.c, motion.d);
					world.addParticle(ParticleTypes.END_ROD, pos.entity, pos.c, pos.d, motion.entity, motion.c, motion.d);
				}
				entity.getPersistentData().putBoolean("ClientAnimationPlayed", true);
			}

			return false;
		}

		if (!entity.getPersistentData().contains("FromLight"))
			return false;

		entity.lifespan = 6000;
		entity.e(true);
		entity.f(entity.cB().b(0, .15f, 0));
		entity.getPersistentData().remove("FromLight");
		return false;
	}

}

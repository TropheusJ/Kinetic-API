package com.simibubi.create.content.curiosities;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;

public class BuildersTeaItem extends HoeItem {

	public BuildersTeaItem(a p_i48487_1_) {
		super(p_i48487_1_);
	}

	public ItemCooldownManager a(ItemCooldownManager stack, GameMode world, SaddledComponent entity) {
		PlayerAbilities playerentity = entity instanceof PlayerAbilities ? (PlayerAbilities) entity : null;
		if (playerentity instanceof ServerPlayerEntity)
			Criteria.CONSUME_ITEM.a((ServerPlayerEntity) playerentity, stack);

		if (!world.v) 
			entity.c(new InstantStatusEffect(StatusEffectType.field_18273, 3 * 60 * 20, 0, false, false, false));

		if (playerentity != null) {
			playerentity.b(StatFormatter.DIVIDE_BY_TEN.b(this));
			playerentity.eH().a(1, .6F);
			if (!playerentity.bC.spawnDelay)
				stack.g(1);
		}

		if (playerentity == null || !playerentity.bC.spawnDelay) {
			if (stack.a()) 
				return new ItemCooldownManager(AliasedBlockItem.nw);
			if (playerentity != null) 
				playerentity.bm.e(new ItemCooldownManager(AliasedBlockItem.nw));
		}

		return stack;
	}

	public int e_(ItemCooldownManager p_77626_1_) {
		return 42;
	}

	public TippedArrowItem d_(ItemCooldownManager p_77661_1_) {
		return TippedArrowItem.c;
	}

	public LocalDifficulty<ItemCooldownManager> a(GameMode p_77659_1_, PlayerAbilities p_77659_2_, ItemScatterer p_77659_3_) {
		p_77659_2_.c(p_77659_3_);
		return LocalDifficulty.a(p_77659_2_.b(p_77659_3_));
	}

}

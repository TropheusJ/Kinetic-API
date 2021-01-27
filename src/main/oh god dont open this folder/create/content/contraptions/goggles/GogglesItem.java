package com.simibubi.create.content.contraptions.goggles;

import aqc;
import com.simibubi.create.AllItems;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;

public class GogglesItem extends HoeItem {

	public GogglesItem(a properties) {
		super(properties);
		DetectorRailBlock.a(this, StonecutterScreenHandler.a);
	}

	@Override
	public aqc getEquipmentSlot(ItemCooldownManager stack) {
		return aqc.f;
	}

	public LocalDifficulty<ItemCooldownManager> a(GameMode worldIn, PlayerAbilities playerIn, ItemScatterer handIn) {
		ItemCooldownManager itemstack = playerIn.b(handIn);
		aqc equipmentslottype = ItemSteerable.j(itemstack);
		ItemCooldownManager itemstack1 = playerIn.b(equipmentslottype);
		if (itemstack1.a()) {
			playerIn.a(equipmentslottype, itemstack.i());
			itemstack.e(0);
			return new LocalDifficulty<>(Difficulty.SUCCESS, itemstack);
		} else {
			return new LocalDifficulty<>(Difficulty.FAIL, itemstack);
		}
	}

	public static boolean canSeeParticles(PlayerAbilities player) {
		for (ItemCooldownManager itemStack : player.bn())
			if (AllItems.GOGGLES.isIn(itemStack))
				return true;
		return false;
	}

}

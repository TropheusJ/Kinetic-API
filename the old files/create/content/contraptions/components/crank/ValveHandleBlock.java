package com.simibubi.kinetic_api.content.contraptions.components.crank;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.utility.DyeHelper;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.DebugStickItem;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
public class ValveHandleBlock extends HandCrankBlock {
	private final boolean inCreativeTab;

	public static ValveHandleBlock copper(c properties) {
		return new ValveHandleBlock(properties, true);
	}

	public static ValveHandleBlock dyed(c properties) {
		return new ValveHandleBlock(properties, false);
	}

	private ValveHandleBlock(c properties, boolean inCreativeTab) {
		super(properties);
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager heldItem = player.b(handIn);
		for (DebugStickItem color : DebugStickItem.values()) {
			if (!heldItem.b()
				.a(DyeHelper.getTagOfDye(color)))
				continue;
			if (worldIn.v)
				return Difficulty.SUCCESS;

			PistonHandler newState = AllBlocks.DYED_VALVE_HANDLES[color.ordinal()].getDefaultState()
				.a(FACING, state.c(FACING));
			if (newState != state)
				worldIn.a(pos, newState);
			return Difficulty.SUCCESS;
		}

		return super.a(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> p_149666_2_) {
		if (group != ChorusFruitItem.g && !inCreativeTab)
			return;
		super.a(group, p_149666_2_);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public AllBlockPartials getRenderedHandle() {
		return null;
	}

	@Override
	public int getRotationSpeed() {
		return 16;
	}

}

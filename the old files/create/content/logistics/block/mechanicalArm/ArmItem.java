package com.simibubi.kinetic_api.content.logistics.block.mechanicalArm;

import bnx;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ArmItem extends BannerItem {

	public ArmItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public Difficulty a(bnx ctx) {
		GameMode world = ctx.p();
		BlockPos pos = ctx.a();
		if (ArmInteractionPoint.isInteractable(world, pos, world.d_(pos)))
			return Difficulty.SUCCESS;
		return super.a(ctx);
	}

	@Override
	protected boolean a(BlockPos pos, GameMode world, PlayerAbilities p_195943_3_, ItemCooldownManager p_195943_4_,
		PistonHandler p_195943_5_) {
		if (world.v)
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ArmInteractionPointHandler.flushSettings(pos));
		return super.a(pos, world, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	@Override
	public boolean a(PistonHandler state, GameMode world, BlockPos pos,
		PlayerAbilities p_195938_4_) {
		return !ArmInteractionPoint.isInteractable(world, pos, state);
	}

}

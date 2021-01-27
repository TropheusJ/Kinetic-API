package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.advancement.AllTriggers;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.world.GameMode;

public class BeltTunnelItem extends BannerItem {

	public BeltTunnelItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	protected boolean b(PotionUtil ctx, PistonHandler state) {
		PlayerAbilities playerentity = ctx.n();
		ArrayVoxelShape iselectioncontext =
			playerentity == null ? ArrayVoxelShape.a() : ArrayVoxelShape.a(playerentity);
		GameMode world = ctx.p();
		BlockPos pos = ctx.a();
		return (!this.d() || AllBlocks.ANDESITE_TUNNEL.get()
			.isValidPositionForPlacement(state, world, pos)) && world.a(state, pos, iselectioncontext);
	}

	@Override
	protected boolean a(BlockPos pos, GameMode world, PlayerAbilities p_195943_3_, ItemCooldownManager p_195943_4_,
		PistonHandler state) {
		boolean flag = super.a(pos, world, p_195943_3_, p_195943_4_, state);
		if (!world.v) {
			BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos.down());
			if (belt != null) {
				AllTriggers.triggerFor(AllTriggers.PLACE_TUNNEL, p_195943_3_);
				if (belt.casing == CasingType.NONE)
					belt.setCasingType(AllBlocks.ANDESITE_TUNNEL.has(state) ? CasingType.ANDESITE : CasingType.BRASS);
			}
		}
		return flag;
	}

}

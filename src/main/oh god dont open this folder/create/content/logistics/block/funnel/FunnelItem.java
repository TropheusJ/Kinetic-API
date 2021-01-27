package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BannerItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FunnelItem extends BannerItem {

	public FunnelItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@SubscribeEvent
	public static void funnelItemAlwaysPlacesWhenUsed(PlayerInteractEvent.RightClickBlock event) {
		if (event.getItemStack()
			.b() instanceof FunnelItem)
			event.setUseBlock(Result.DENY);
	}

	@Override
	protected PistonHandler c(PotionUtil ctx) {
		GameMode world = ctx.p();
		BlockPos pos = ctx.a();
		PistonHandler state = super.c(ctx);
		if (state == null)
			return state;
		if (!(state.b() instanceof FunnelBlock))
			return state;
		Direction direction = state.c(FunnelBlock.SHAPE);
		if (!direction.getAxis()
			.isHorizontal()) {
			BeehiveBlockEntity tileEntity = world.c(pos.offset(direction.getOpposite()));
			if (tileEntity instanceof ChuteTileEntity && ((ChuteTileEntity) tileEntity).getItemMotion() > 0)
				state = state.a(FunnelBlock.SHAPE, direction.getOpposite());
			return state;
		}

		FunnelBlock block = (FunnelBlock) e();
		BeetrootsBlock beltFunnelBlock = block.getEquivalentBeltFunnel(world, pos, state)
			.b();
		PistonHandler equivalentBeltFunnel = beltFunnelBlock.a(ctx)
			.a(BeltFunnelBlock.aq, direction);
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos)) {
			AllTriggers.triggerFor(AllTriggers.BELT_FUNNEL, ctx.n());
			return equivalentBeltFunnel;
		}

		return state;
	}

}

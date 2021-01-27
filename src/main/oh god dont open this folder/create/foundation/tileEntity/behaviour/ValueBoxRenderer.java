package com.simibubi.create.foundation.tileEntity.behaviour;

import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import elg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.stat.StatHandler;

public class ValueBoxRenderer {

	public static void renderItemIntoValueBox(ItemCooldownManager filter, BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		HorseEntityRenderer itemRenderer = KeyBinding.B().ac();
		elg modelWithOverrides = itemRenderer.a(filter, KeyBinding.B().r, null);
		boolean blockItem = modelWithOverrides.b();
		float scale = (!blockItem ? .5f : 1f) - 1 / 64f;
		float zOffset = (!blockItem ? -.225f : 0) + customZOffset(filter.b());
		ms.a(scale, scale, scale);
		ms.a(0, 0, zOffset);
		itemRenderer.a(filter, b.i, light, overlay, ms, buffer);
	}

	private static float customZOffset(HoeItem item) {
		float NUDGE = -.1f;
		if (item instanceof FilterItem)
			return NUDGE;
		if (item instanceof BannerItem) {
			BeetrootsBlock block = ((BannerItem) item).e();
			if (block instanceof AbstractShaftBlock)
				return NUDGE;
			if (block instanceof WallMountedBlock)
				return NUDGE;
			if (block.a(StatHandler.f))
				return NUDGE;
			if (block == BellBlock.iw)
				return NUDGE;
		}
		return 0;
	}

}

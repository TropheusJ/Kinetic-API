package com.simibubi.kinetic_api.content.contraptions.wrench;

import com.simibubi.kinetic_api.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.kinetic_api.foundation.item.PartialItemModelRenderer;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;

public class WrenchItemRenderer extends CustomRenderedItemModelRenderer<WrenchModel> {

	@Override
	protected void render(ItemCooldownManager stack, WrenchModel model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		renderer.render(model.getBakedModel(), light);

		float worldTime = AnimationTickHolder.getRenderTick();
		float angle = worldTime * -.5f % 360;
		float xOffset = -1/16f;
		ms.a(-xOffset, 0, 0);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		ms.a(xOffset, 0, 0);
		
		renderer.render(model.getPartial("gear"), light);
	}
	
}

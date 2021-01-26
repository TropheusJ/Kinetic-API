package com.simibubi.create.content.curiosities.tools;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;

public class DeforesterItemRenderer extends CustomRenderedItemModelRenderer<DeforesterModel> {

	@Override
	protected void render(ItemCooldownManager stack, DeforesterModel model, PartialItemModelRenderer renderer,
		BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		int maxLight = 0xF000F0;
		float worldTime = AnimationTickHolder.getRenderTick();
		
		renderer.renderSolid(model.getBakedModel(), light);
		renderer.renderSolidGlowing(model.getPartial("core"), maxLight);
		renderer.renderGlowing(model.getPartial("core_glow"), maxLight);
		
		float angle = worldTime * -.5f % 360;
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		renderer.renderSolid(model.getPartial("gear"), light);
	}
	

}

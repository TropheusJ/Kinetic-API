package com.simibubi.kinetic_api.content.curiosities.symmetry.client;

import afj;
import com.simibubi.kinetic_api.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.kinetic_api.foundation.item.PartialItemModelRenderer;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;

public class SymmetryWandItemRenderer extends CustomRenderedItemModelRenderer<SymmetryWandModel> {

	@Override
	protected void render(ItemCooldownManager stack, SymmetryWandModel model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		float worldTime = AnimationTickHolder.getRenderTick() / 20;
		int maxLight = 0xF000F0;

		renderer.render(model.getBakedModel(), light);
		renderer.renderSolidGlowing(model.getPartial("core"), maxLight);
		renderer.renderGlowing(model.getPartial("core_glow"), maxLight);

		float floating = afj.a(worldTime) * .05f;
		float angle = worldTime * -10 % 360;
		
		ms.a(0, floating, 0);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		
		renderer.renderGlowing(model.getPartial("bits"), maxLight);
	}

}

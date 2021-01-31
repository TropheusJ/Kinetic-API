package com.simibubi.kinetic_api.content.curiosities.zapper.terrainzapper;

import static java.lang.Math.max;
import static afj.a;

import afj;
import com.simibubi.kinetic_api.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.kinetic_api.foundation.item.PartialItemModelRenderer;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.CubeFace;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.ItemCooldownManager;

public class WorldshaperItemRenderer extends ZapperItemRenderer<WorldshaperModel> {

	@Override
	protected void render(ItemCooldownManager stack, WorldshaperModel model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		super.render(stack, model, renderer, ms, buffer, light, overlay);

		float pt = KeyBinding.B()
			.ai();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		renderer.renderSolid(model.getBakedModel(), light);

		FishingParticle player = KeyBinding.B().s;
		boolean leftHanded = player.dU() == EquipmentSlot.LEFT;
		boolean mainHand = player.dC() == stack;
		boolean offHand = player.dD() == stack;
		float animation = getAnimationProgress(pt, leftHanded, mainHand);

		// Core glows
		float multiplier = afj.a(worldTime * 5);
		if (mainHand || offHand) 
			multiplier = animation;

		int lightItensity = (int) (15 * a(multiplier, 0, 1));
		int glowLight = CubeFace.a(lightItensity, max(lightItensity, 4));
		renderer.renderSolidGlowing(model.getPartial("core"), glowLight);
		renderer.renderGlowing(model.getPartial("core_glow"), glowLight);

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.a(0, offset, 0);
		ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.a(0, -offset, 0);
		renderer.render(model.getPartial("accelerator"), light);
	}

}

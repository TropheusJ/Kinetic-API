package com.simibubi.create.content.curiosities.zapper.blockzapper;

import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Accelerator;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Amplifier;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Body;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Retriever;
import static com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components.Scope;
import static java.lang.Math.max;
import static afj.a;

import afj;
import com.simibubi.create.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import elg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.CubeFace;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.ItemCooldownManager;

public class BlockzapperItemRenderer extends ZapperItemRenderer<BlockzapperModel> {

	@Override
	protected void render(ItemCooldownManager stack, BlockzapperModel model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		super.render(stack, model, renderer, ms, buffer, light, overlay);

		float pt = KeyBinding.B()
			.ai();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		renderer.render(model.getBakedModel(), light);
		renderComponent(stack, model, Body, renderer, light);
		renderComponent(stack, model, Amplifier, renderer, light);
		renderComponent(stack, model, Retriever, renderer, light);
		renderComponent(stack, model, Scope, renderer, light);

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

		if (BlockzapperItem.getTier(Amplifier, stack) != ComponentTier.None) {
			renderer.renderSolidGlowing(model.getPartial("amplifier_core"), glowLight);
			renderer.renderGlowing(model.getPartial("amplifier_core_glow"), glowLight);
		}

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.a(0, offset, 0);
		ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.a(0, -offset, 0);
		renderComponent(stack, model, Accelerator, renderer, light);
	}

	public void renderComponent(ItemCooldownManager stack, BlockzapperModel model, Components component,
		PartialItemModelRenderer renderer, int light) {
		ComponentTier tier = BlockzapperItem.getTier(component, stack);
		elg partial = model.getComponentPartial(tier, component);
		if (partial != null)
			renderer.render(partial, light);
	}

}

package com.simibubi.create.content.curiosities.tools;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;

public class ExtendoGripItemRenderer extends CustomRenderedItemModelRenderer<ExtendoGripModel> {

	private static final EntityHitResult rotationOffset = new EntityHitResult(0, 1 / 2f, 1 / 2f);
	private static final EntityHitResult cogRotationOffset = new EntityHitResult(0, 1 / 16f, 0);

	@Override
	protected void render(ItemCooldownManager stack, ExtendoGripModel model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		MatrixStacker stacker = MatrixStacker.of(ms);
		float animation = 0.25f;
		b perspective = model.getCurrentPerspective();
		boolean leftHand = perspective == b.d;
		boolean rightHand = perspective == b.e;
		if (leftHand || rightHand)
			animation = afj.g(KeyBinding.B()
				.ai(), ExtendoGripRenderHandler.lastMainHandAnimation,
				ExtendoGripRenderHandler.mainHandAnimation);

		animation = animation * animation * animation;
		float extensionAngle = afj.g(animation, 24f, 156f);
		float halfAngle = extensionAngle / 2;
		float oppositeAngle = 180 - extensionAngle;

		// grip
		renderer.renderSolid(model.getBakedModel(), light);

		// bits
		ms.a();
		ms.a(0, 1 / 16f, -7 / 16f);
		ms.a(1, 1, 1 + animation);
		ms.a();
		stacker.rotateX(-halfAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("thin_short"), light);
		stacker.translateBack(rotationOffset);

		ms.a(0, 5.5f / 16f, 0);
		stacker.rotateX(-oppositeAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("wide_long"), light);
		stacker.translateBack(rotationOffset);

		ms.a(0, 11 / 16f, 0);
		stacker.rotateX(oppositeAngle)
			.translate(rotationOffset);
		ms.a(0, 0.5f / 16f, 0);
		renderer.renderSolid(model.getPartial("thin_short"), light);
		stacker.translateBack(rotationOffset);

		ms.b();
		ms.a();

		stacker.rotateX(-180 + halfAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("wide_short"), light);
		stacker.translateBack(rotationOffset);

		ms.a(0, 5.5f / 16f, 0);
		stacker.rotateX(oppositeAngle)
			.translate(rotationOffset);
		renderer.renderSolid(model.getPartial("thin_long"), light);
		stacker.translateBack(rotationOffset);

		ms.a(0, 11 / 16f, 0);
		stacker.rotateX(-oppositeAngle)
			.translate(rotationOffset);
		ms.a(0, 0.5f / 16f, 0);
		renderer.renderSolid(model.getPartial("wide_short"), light);
		stacker.translateBack(rotationOffset);

		// hand
		ms.a(0, 5.5f / 16f, 0);
		stacker.rotateX(180 - halfAngle)
			.rotateY(180);
		ms.a(0, 0, -4 / 16f);
		ms.a(1, 1, 1 / (1 + animation));
		renderer.renderSolid((leftHand || rightHand) ? ExtendoGripRenderHandler.pose.get()
			: AllBlockPartials.DEPLOYER_HAND_POINTING.get(), light);
		ms.b();

		ms.b();

		// cog
		ms.a();
		float angle = AnimationTickHolder.getRenderTick() * -2;
		if (leftHand || rightHand)
			angle += 360 * animation;
		angle %= 360;
		stacker.translate(cogRotationOffset)
			.rotateZ(angle)
			.translateBack(cogRotationOffset);
		renderer.renderSolid(model.getPartial("cog"), light);
		ms.b();
	}

}

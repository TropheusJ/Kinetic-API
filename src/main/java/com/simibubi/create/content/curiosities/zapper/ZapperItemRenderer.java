package com.simibubi.create.content.curiosities.zapper;

import afj;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.PartialItemModelRenderer;
import elg;
import net.minecraft.block.CoralWallFanBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.NbtHelper;

public abstract class ZapperItemRenderer<M extends CustomRenderedItemModel> extends CustomRenderedItemModelRenderer<M> {

	@Override
	protected void render(ItemCooldownManager stack, M model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		// Block indicator
		if (model.getCurrentPerspective() == b.g && stack.n() && stack.o()
			.contains("BlockUsed"))
			renderBlockUsed(stack, ms, buffer, light, overlay);
	}

	private void renderBlockUsed(ItemCooldownManager stack, BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		PistonHandler state = NbtHelper.c(stack.o()
			.getCompound("BlockUsed"));

		ms.a();
		ms.a(-0.3F, -0.45F, -0.0F);
		ms.a(0.25F, 0.25F, 0.25F);
		elg modelForState = KeyBinding.B()
			.aa()
			.a(state);

		if (state.b() instanceof CoralWallFanBlock)
			modelForState = KeyBinding.B()
				.ac()
				.a(new ItemCooldownManager(state.b()), KeyBinding.B().r, null);

		KeyBinding.B()
			.ac()
			.a(new ItemCooldownManager(state.b()), b.a, false, ms, buffer, light, overlay,
				modelForState);
		ms.b();
	}

	protected float getAnimationProgress(float pt, boolean leftHanded, boolean mainHand) {
		float last = mainHand ^ leftHanded ? ZapperRenderHandler.lastRightHandAnimation
			: ZapperRenderHandler.lastLeftHandAnimation;
		float current =
			mainHand ^ leftHanded ? ZapperRenderHandler.rightHandAnimation : ZapperRenderHandler.leftHandAnimation;
		float animation = afj.a(afj.g(pt, last, current) * 5, 0, 1);
		return animation;
	}

}

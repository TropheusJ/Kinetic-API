package com.simibubi.kinetic_api.content.curiosities.tools;

import afj;
import com.simibubi.kinetic_api.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import elg;
import net.minecraft.client.input.Input;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;

public class SandPaperItemRenderer extends Input {

	@Override
	public void a(ItemCooldownManager stack, ModelElementTexture.b p_239207_2_, BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		HorseEntityRenderer itemRenderer = KeyBinding.B().ac();
		FishingParticle player = KeyBinding.B().s;
		SandPaperModel mainModel = (SandPaperModel) itemRenderer.a(stack, KeyBinding.B().r, null);
		b perspective = mainModel.getCurrentPerspective();
		float partialTicks = KeyBinding.B().ai();

		boolean leftHand = perspective == b.d;
		boolean firstPerson = leftHand || perspective == b.e;

		ms.a();
		ms.a(.5f, .5f, .5f);

		CompoundTag tag = stack.p();
		boolean jeiMode = tag.contains("JEI");

		if (tag.contains("Polishing")) {
			ms.a();

			if (perspective == b.g) {
				ms.a(0.0F, .2f, 1.0F);
				ms.a(.75f, .75f, .75f);
			} else {
				int modifier = leftHand ? -1 : 1;
				ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(modifier * 40));
			}

			// Reverse bobbing
			float time = (float) (!jeiMode ? player.dY()
					: (-AnimationTickHolder.ticks) % stack.k()) - partialTicks + 1.0F;
			if (time / (float) stack.k() < 0.8F) {
				float bobbing = -afj.e(afj.b(time / 4.0F * (float) Math.PI) * 0.1F);

				if (perspective == b.g)
					ms.a(bobbing, bobbing, 0.0F);
				else
					ms.a(0.0f, bobbing, 0.0F);
			}

			ItemCooldownManager toPolish = ItemCooldownManager.a(tag.getCompound("Polishing"));
			itemRenderer.a(toPolish, b.a, light, overlay, ms, buffer);

			ms.b();
		}

		if (firstPerson) {
			int itemInUseCount = player.dY();
			if (itemInUseCount > 0) {
				int modifier = leftHand ? -1 : 1;
				ms.a(modifier * .5f, 0, -.25f);
				ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(modifier * 40));
				ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(modifier * 10));
				ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(modifier * 90));
			}
		}

		itemRenderer.a(stack, b.a, false, ms, buffer, light, overlay, mainModel.getBakedModel());

		ms.b();
	}

	public static class SandPaperModel extends CustomRenderedItemModel {

		public SandPaperModel(elg template) {
			super(template, "");
		}

		@Override
		public Input createRenderer() {
			return new SandPaperItemRenderer();
		}

	}

}

package com.simibubi.kinetic_api.foundation.block.render;

import com.simibubi.kinetic_api.foundation.item.PartialItemModelRenderer;
import net.minecraft.client.input.Input;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.entity.player.ItemCooldownManager;

public class CustomRenderedItemModelRenderer<M extends CustomRenderedItemModel> extends Input {

	@Override
	@SuppressWarnings("unchecked")
	public void a(ItemCooldownManager stack, ModelElementTexture.b p_239207_2_, BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay) {
		M mainModel = ((M) KeyBinding.B()
			.ac()
			.a(stack, null, null));
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, p_239207_2_, ms, buffer, overlay);

		ms.a();
		ms.a(0.5F, 0.5F, 0.5F);
		render(stack, mainModel, renderer, ms, buffer, light, overlay);
		ms.b();
	}

	protected void render(ItemCooldownManager stack, M model, PartialItemModelRenderer renderer, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {

	}

}

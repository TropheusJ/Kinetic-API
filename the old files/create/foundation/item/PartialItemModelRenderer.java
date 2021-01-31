package com.simibubi.kinetic_api.foundation.item;

import java.util.Random;
import com.simibubi.kinetic_api.foundation.renderState.RenderTypes;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import elg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class PartialItemModelRenderer {

	static PartialItemModelRenderer instance;

	ItemCooldownManager stack;
	int overlay;
	BufferVertexConsumer ms;
	ModelElementTexture.b transformType;
	BackgroundRenderer buffer;

	static PartialItemModelRenderer get() {
		if (instance == null)
			instance = new PartialItemModelRenderer();
		return instance;
	}

	public static PartialItemModelRenderer of(ItemCooldownManager stack, ModelElementTexture.b transformType, BufferVertexConsumer ms, BackgroundRenderer buffer, int overlay) {
		PartialItemModelRenderer instance = get();
		instance.stack = stack;
		instance.buffer = buffer;
		instance.ms = ms;
		instance.transformType = transformType;
		instance.overlay = overlay;
		return instance;
	}

	public void render(elg model, int light) {
		render(model, RenderTypes.getItemPartialTranslucent(), light);
	}
	
	public void renderSolid(elg model, int light) {
		render(model, RenderTypes.getItemPartialSolid(), light);
	}
	
	public void renderSolidGlowing(elg model, int light) {
		render(model, RenderTypes.getGlowingSolid(), light);
	}
	
	public void renderGlowing(elg model, int light) {
		render(model, RenderTypes.getGlowingTranslucent(), light);
	}

	public void render(elg model, VertexConsumerProvider type, int light) {
		if (stack.a())
			return;

		ms.a();
		ms.a(-0.5D, -0.5D, -0.5D);

		if (!model.d())
			renderBakedItemModel(model, light, ms,
				HorseEntityRenderer.a(buffer, type, true, stack.u()));
		else
			stack.b()
				.getItemStackTileEntityRenderer()
				.a(stack, transformType, ms, buffer, light, overlay);

		ms.b();
	}

	private void renderBakedItemModel(elg model, int light, BufferVertexConsumer ms, OverlayVertexConsumer p_229114_6_) {
		HorseEntityRenderer ir = KeyBinding.B()
			.ac();
		Random random = new Random();
		IModelData data = EmptyModelData.INSTANCE;

		for (Direction direction : Iterate.directions) {
			random.setSeed(42L);
			ir.a(ms, p_229114_6_, model.getQuads(null, direction, random, data), stack,
				light, overlay);
		}

		random.setSeed(42L);
		ir.a(ms, p_229114_6_, model.getQuads(null, null, random, data),
			stack, light, overlay);
	}

}

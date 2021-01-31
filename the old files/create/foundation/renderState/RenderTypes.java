package com.simibubi.kinetic_api.foundation.renderState;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.kinetic_api.AllSpecialTextures;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;

public class RenderTypes extends LightmapTextureManager {

	protected static final LightmapTextureManager.c A = new NoCullState();

	public static VertexConsumerProvider getOutlineTranslucent(Identifier texture, boolean cull) {
		VertexConsumerProvider.b rendertype$state = VertexConsumerProvider.b.a()
			.a(new LightmapTextureManager.o(texture, false, false))
			.a(client)
			.a(x)
			.a(i)
			.a(cull ? z : A)
			.a(t)
			.a(v)
			.a(true);
		return VertexConsumerProvider.a("outline_translucent" + (cull ? "_cull" : ""),
			BufferBuilder.parameters, 7, 256, true, true, rendertype$state);
	}

	private static final VertexConsumerProvider OUTLINE_SOLID =
		VertexConsumerProvider.a("outline_solid", BufferBuilder.parameters, 7, 256, true,
			false, VertexConsumerProvider.b.a()
				.a(new LightmapTextureManager.o(AllSpecialTextures.BLANK.getLocation(), false, false))
				.a(image)
				.a(x)
				.a(t)
				.a(v)
				.a(true));

	public static VertexConsumerProvider getGlowingSolid(Identifier texture) {
		VertexConsumerProvider.b rendertype$state = VertexConsumerProvider.b.a()
			.a(new LightmapTextureManager.o(texture, false, false))
			.a(image)
			.a(y)
			.a(t)
			.a(v)
			.a(true);
		return VertexConsumerProvider.a("glowing_solid", BufferBuilder.parameters, 7, 256,
			true, false, rendertype$state);
	}

	public static VertexConsumerProvider getGlowingTranslucent(Identifier texture) {
		VertexConsumerProvider.b rendertype$state = VertexConsumerProvider.b.a()
			.a(new LightmapTextureManager.o(texture, false, false))
			.a(client)
			.a(y)
			.a(i)
			.a(A)
			.a(t)
			.a(v)
			.a(true);
		return VertexConsumerProvider.a("glowing_translucent", BufferBuilder.parameters, 7,
			256, true, true, rendertype$state);
	}

	private static final VertexConsumerProvider GLOWING_SOLID = RenderTypes.getGlowingSolid(GrindstoneScreenHandler.result);
	private static final VertexConsumerProvider GLOWING_TRANSLUCENT =
		RenderTypes.getGlowingTranslucent(GrindstoneScreenHandler.result);

	private static final VertexConsumerProvider ITEM_PARTIAL_SOLID =
		VertexConsumerProvider.a("item_solid", BufferBuilder.parameters, 7, 256, true,
			false, VertexConsumerProvider.b.a()
				.a(new LightmapTextureManager.o(GrindstoneScreenHandler.result, false, false))
				.a(image)
				.a(x)
				.a(t)
				.a(v)
				.a(true));

	private static final VertexConsumerProvider ITEM_PARTIAL_TRANSLUCENT = VertexConsumerProvider.a("entity_translucent",
		BufferBuilder.parameters, 7, 256, true, true, VertexConsumerProvider.b.a()
			.a(new LightmapTextureManager.o(GrindstoneScreenHandler.result, false, false))
			.a(client)
			.a(x)
			.a(i)
			.a(A)
			.a(t)
			.a(v)
			.a(true));

	public static VertexConsumerProvider getItemPartialSolid() {
		return ITEM_PARTIAL_SOLID;
	}
	
	public static VertexConsumerProvider getItemPartialTranslucent() {
		return ITEM_PARTIAL_TRANSLUCENT;
	}

	public static VertexConsumerProvider getOutlineSolid() {
		return OUTLINE_SOLID;
	}

	public static VertexConsumerProvider getGlowingSolid() {
		return GLOWING_SOLID;
	}

	public static VertexConsumerProvider getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT;
	}

	protected static class NoCullState extends LightmapTextureManager.c {
		public NoCullState() {
			super(false);
		}

		@Override
		public void a() {
			RenderSystem.disableCull();
		}
	}

	// Mmm gimme those protected fields
	public RenderTypes() {
		super(null, null, null);
	}
}

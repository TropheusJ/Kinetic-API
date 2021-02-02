package com.tropheus_jay.kinetic_api.foundation.renderState;

import java.util.SortedMap;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.Util;

public class SuperRenderTypeBuffer implements VertexConsumerProvider {

	static SuperRenderTypeBuffer instance;

	public static SuperRenderTypeBuffer getInstance() {
		if (instance == null)
			instance = new SuperRenderTypeBuffer();
		return instance;
	}

	SuperRenderTypeBufferPhase earlyBuffer;
	SuperRenderTypeBufferPhase defaultBuffer;
	SuperRenderTypeBufferPhase lateBuffer;

	public SuperRenderTypeBuffer() {
		earlyBuffer = new SuperRenderTypeBufferPhase();
		defaultBuffer = new SuperRenderTypeBufferPhase();
		lateBuffer = new SuperRenderTypeBufferPhase();
	}

	public VertexConsumer getEarlyBuffer(RenderLayer type) {
		return earlyBuffer.getBuffer(type);
	}

	@Override
	public VertexConsumer getBuffer(RenderLayer type) {
		return defaultBuffer.getBuffer(type);
	}

	public VertexConsumer getLateBuffer(RenderLayer type) {
		return lateBuffer.getBuffer(type);
	}

	public void draw() {
		RenderSystem.disableCull();
		earlyBuffer.draw();
		defaultBuffer.draw();
		lateBuffer.draw();
	}

	public void draw(RenderLayer type) {
		RenderSystem.disableCull();
		earlyBuffer.draw(type);
		defaultBuffer.draw(type);
		lateBuffer.draw(type);
	}

	private static class SuperRenderTypeBufferPhase extends VertexConsumerProvider.Immediate {

		// Visible clones from net.minecraft.client.renderer.RenderTypeBuffers
		static final BlockBufferBuilderStorage blockBuilders = new BlockBufferBuilderStorage();
		static final SortedMap<RenderLayer, BufferBuilder> createEntityBuilders() {
			return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
				map.put(TexturedRenderLayers.getEntitySolid(), blockBuilders.get(RenderLayer.getSolid()));
				assign(map, RenderTypes.getOutlineSolid());
				map.put(TexturedRenderLayers.h(), blockBuilders.a(VertexConsumerProvider.e()));
				map.put(TexturedRenderLayers.a(), blockBuilders.a(VertexConsumerProvider.d()));
				map.put(TexturedRenderLayers.j(), blockBuilders.a(VertexConsumerProvider.f())); // FIXME new equivalent of getEntityTranslucent() ?
				assign(map, TexturedRenderLayers.b());
				assign(map, TexturedRenderLayers.c());
				assign(map, TexturedRenderLayers.d());
				assign(map, TexturedRenderLayers.e());
				assign(map, TexturedRenderLayers.f());
				assign(map, VertexConsumerProvider.h());
				assign(map, VertexConsumerProvider.n());
				assign(map, VertexConsumerProvider.p());
				assign(map, VertexConsumerProvider.j());
				elk.k.forEach((p_228488_1_) -> {
					assign(map, p_228488_1_);
				});
			});
		}
			

		private static void assign(Object2ObjectLinkedOpenHashMap<VertexConsumerProvider, GlShader> map, VertexConsumerProvider type) {
			map.put(type, new GlShader(type.v()));
		}

		protected SuperRenderTypeBufferPhase() {
			super(new GlShader(256), createEntityBuilders());
		}

	}

}

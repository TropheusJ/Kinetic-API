package com.simibubi.create.foundation.renderState;

import java.util.SortedMap;

import com.mojang.blaze3d.systems.RenderSystem;
import elk;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Util;

public class SuperRenderTypeBuffer implements BackgroundRenderer {

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

	public OverlayVertexConsumer getEarlyBuffer(VertexConsumerProvider type) {
		return earlyBuffer.getBuffer(type);
	}

	@Override
	public OverlayVertexConsumer getBuffer(VertexConsumerProvider type) {
		return defaultBuffer.getBuffer(type);
	}

	public OverlayVertexConsumer getLateBuffer(VertexConsumerProvider type) {
		return lateBuffer.getBuffer(type);
	}

	public void draw() {
		RenderSystem.disableCull();
		earlyBuffer.method_23792();
		defaultBuffer.method_23792();
		lateBuffer.method_23792();
	}

	public void draw(VertexConsumerProvider type) {
		RenderSystem.disableCull();
		earlyBuffer.a(type);
		defaultBuffer.a(type);
		lateBuffer.a(type);
	}

	private static class SuperRenderTypeBufferPhase extends BackgroundRenderer.FogType {

		// Visible clones from net.minecraft.client.renderer.RenderTypeBuffers
		static final KeyboardInput blockBuilders = new KeyboardInput();
		static final SortedMap<VertexConsumerProvider, GlShader> createEntityBuilders() {
			return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
				map.put(ShaderEffect.g(), blockBuilders.a(VertexConsumerProvider.c()));
				assign(map, RenderTypes.getOutlineSolid());
				map.put(ShaderEffect.h(), blockBuilders.a(VertexConsumerProvider.e()));
				map.put(ShaderEffect.a(), blockBuilders.a(VertexConsumerProvider.d()));
				map.put(ShaderEffect.j(), blockBuilders.a(VertexConsumerProvider.f())); // FIXME new equivalent of getEntityTranslucent() ?
				assign(map, ShaderEffect.b());
				assign(map, ShaderEffect.c());
				assign(map, ShaderEffect.d());
				assign(map, ShaderEffect.e());
				assign(map, ShaderEffect.f());
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

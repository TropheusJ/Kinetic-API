package com.simibubi.kinetic_api.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.kinetic_api.AllBlockPartials;
import ejo;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SuperByteBufferCache {

	public static class Compartment<T> {
	}

	public static final Compartment<PistonHandler> GENERIC_TILE = new Compartment<>();
	public static final Compartment<AllBlockPartials> PARTIAL = new Compartment<>();
	public static final Compartment<Pair<Direction, AllBlockPartials>> DIRECTIONAL_PARTIAL = new Compartment<>();

	Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

	public SuperByteBufferCache() {
		cache = new HashMap<>();
		registerCompartment(GENERIC_TILE);
		registerCompartment(PARTIAL);
		registerCompartment(DIRECTIONAL_PARTIAL);
	}

	public SuperByteBuffer renderBlock(PistonHandler toRender) {
		return getGeneric(toRender, () -> standardBlockRender(toRender));
	}

	public SuperByteBuffer renderPartial(AllBlockPartials partial, PistonHandler referenceState) {
		return get(PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderPartial(AllBlockPartials partial, PistonHandler referenceState,
		BufferVertexConsumer modelTransform) {
		return get(PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState, modelTransform));
	}

	public SuperByteBuffer renderDirectionalPartial(AllBlockPartials partial, PistonHandler referenceState,
		Direction dir) {
		return get(DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderDirectionalPartial(AllBlockPartials partial, PistonHandler referenceState, Direction dir,
		BufferVertexConsumer modelTransform) {
		return get(DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
			() -> standardModelRender(partial.get(), referenceState, modelTransform));
	}

	public SuperByteBuffer renderBlockIn(Compartment<PistonHandler> compartment, PistonHandler toRender) {
		return get(compartment, toRender, () -> standardBlockRender(toRender));
	}

	SuperByteBuffer getGeneric(PistonHandler key, Supplier<SuperByteBuffer> supplier) {
		return get(GENERIC_TILE, key, supplier);
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Supplier<SuperByteBuffer> supplier) {
		Cache<Object, SuperByteBuffer> compartmentCache = this.cache.get(compartment);
		try {
			return compartmentCache.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void registerCompartment(Compartment<?> instance) {
		cache.put(instance, CacheBuilder.newBuilder()
			.build());
	}

	public void registerCompartment(Compartment<?> instance, long ticksUntilExpired) {
		cache.put(instance, CacheBuilder.newBuilder()
			.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
			.build());
	}

	private SuperByteBuffer standardBlockRender(PistonHandler renderedState) {
		FpsSmoother dispatcher = KeyBinding.B()
			.aa();
		return standardModelRender(dispatcher.a(renderedState), renderedState);
	}

	private SuperByteBuffer standardModelRender(elg model, PistonHandler referenceState) {
		return standardModelRender(model, referenceState, new BufferVertexConsumer());
	}

	private SuperByteBuffer standardModelRender(elg model, PistonHandler referenceState, BufferVertexConsumer ms) {
		KeyBinding mc = KeyBinding.B();
		FpsSmoother dispatcher = mc.aa();
		TexturedRenderLayers blockRenderer = dispatcher.b();
		GlShader builder = new GlShader(512);

		builder.a(GL11.GL_QUADS, BufferBuilder.buffer);
		blockRenderer.renderModel(mc.r, model, referenceState, BlockPos.ORIGIN.up(255), ms, builder, true,
			mc.r.t, 42, ejo.a, VirtualEmptyModelData.INSTANCE);
		builder.markStateDirty();

		return new SuperByteBuffer(builder);
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> cache.invalidateAll());
	}

}

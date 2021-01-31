package com.simibubi.kinetic_api.content.schematics.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import com.simibubi.kinetic_api.content.schematics.SchematicWorld;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import com.simibubi.kinetic_api.foundation.utility.TileEntityRenderHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicRenderer {

	private final Map<VertexConsumerProvider, SuperByteBuffer> bufferCache = new HashMap<>(getLayerCount());
	private final Set<VertexConsumerProvider> usedBlockRenderLayers = new HashSet<>(getLayerCount());
	private final Set<VertexConsumerProvider> startedBufferBuilders = new HashSet<>(getLayerCount());
	private boolean active;
	private boolean changed;
	private SchematicWorld schematic;
	private BlockPos anchor;

	public SchematicRenderer() {
		changed = false;
	}

	public void display(SchematicWorld world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.active = true;
		this.changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void tick() {
		if (!active)
			return;
		KeyBinding mc = KeyBinding.B();
		if (mc.r == null || mc.s == null || !changed)
			return;

		redraw(mc);
		changed = false;
	}

	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		if (!active)
			return;
		buffer.getBuffer(VertexConsumerProvider.c());
		for (VertexConsumerProvider layer : VertexConsumerProvider.u()) {
			if (!usedBlockRenderLayers.contains(layer))
				continue;
			SuperByteBuffer superByteBuffer = bufferCache.get(layer);
			superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
		}
		TileEntityRenderHelper.renderTileEntities(schematic, schematic.getRenderedTileEntities(), ms, new BufferVertexConsumer(),
			buffer);
	}

	private void redraw(KeyBinding minecraft) {
		usedBlockRenderLayers.clear();
		startedBufferBuilders.clear();

		final SchematicWorld blockAccess = schematic;
		final FpsSmoother blockRendererDispatcher = minecraft.aa();

		List<PistonHandler> blockstates = new LinkedList<>();
		Map<VertexConsumerProvider, GlShader> buffers = new HashMap<>();
		BufferVertexConsumer ms = new BufferVertexConsumer();

		BlockPos.a(blockAccess.getBounds())
			.forEach(localPos -> {
				ms.a();
				MatrixStacker.of(ms)
					.translate(localPos);
				BlockPos pos = localPos.add(anchor);
				PistonHandler state = blockAccess.d_(pos);

				for (VertexConsumerProvider blockRenderLayer : VertexConsumerProvider.u()) {
					if (!BlockBufferBuilderStorage.canRenderInLayer(state, blockRenderLayer))
						continue;
					ForgeHooksClient.setRenderLayer(blockRenderLayer);
					if (!buffers.containsKey(blockRenderLayer))
						buffers.put(blockRenderLayer, new GlShader(BufferBuilder.buffer.a()));

					GlShader bufferBuilder = buffers.get(blockRenderLayer);
					if (startedBufferBuilders.add(blockRenderLayer))
						bufferBuilder.a(GL11.GL_QUADS, BufferBuilder.buffer);
					if (blockRendererDispatcher.renderModel(state, pos, blockAccess, ms, bufferBuilder, true,
						minecraft.r.t, EmptyModelData.INSTANCE)) {
						usedBlockRenderLayers.add(blockRenderLayer);
					}
					blockstates.add(state);
				}

				ForgeHooksClient.setRenderLayer(null);
				ms.b();
			});

		// finishDrawing
		for (VertexConsumerProvider layer : VertexConsumerProvider.u()) {
			if (!startedBufferBuilders.contains(layer))
				continue;
			GlShader buf = buffers.get(layer);
			buf.markStateDirty();
			bufferCache.put(layer, new SuperByteBuffer(buf));
		}
	}

	private static int getLayerCount() {
		return VertexConsumerProvider.u()
			.size();
	}

}

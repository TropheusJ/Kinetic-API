package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;
import ejo;
import elg;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameMode;
import net.minecraft.world.TestableWorld;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ContraptionRenderer {

	public static final Compartment<Pair<Contraption, Integer>> CONTRAPTION = new Compartment<>();
	protected static PlacementSimulationWorld renderWorld;

	public static void render(GameMode world, Contraption c, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		renderTileEntities(world, c, ms, msLocal, buffer);
		if (buffer instanceof BackgroundRenderer.FogType)
			((BackgroundRenderer.FogType) buffer).method_23792();
		renderStructure(world, c, ms, msLocal, buffer);
		renderActors(world, c, ms, msLocal, buffer);
	}

	protected static void renderStructure(GameMode world, Contraption c, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		List<VertexConsumerProvider> blockLayers = VertexConsumerProvider.u();

		buffer.getBuffer(VertexConsumerProvider.c());
		for (int i = 0; i < blockLayers.size(); i++) {
			VertexConsumerProvider layer = blockLayers.get(i);
			Pair<Contraption, Integer> key = Pair.of(c, i);
			SuperByteBuffer contraptionBuffer = bufferCache.get(CONTRAPTION, key, () -> buildStructureBuffer(c, layer));
			if (contraptionBuffer.isEmpty())
				continue;
			Matrix4f model = msLocal.c()
				.a();
			contraptionBuffer.light(model)
				.renderInto(ms, buffer.getBuffer(layer));
		}
	}

	private static void renderTileEntities(GameMode world, Contraption c, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		TileEntityRenderHelper.renderTileEntities(world, c.renderedTileEntities, ms, msLocal, buffer);
	}

	private static SuperByteBuffer buildStructureBuffer(Contraption c, VertexConsumerProvider layer) {
		if (renderWorld == null || renderWorld.getWrappedWorld() != KeyBinding.B().r)
			renderWorld = new PlacementSimulationWorld(KeyBinding.B().r);

		ForgeHooksClient.setRenderLayer(layer);
		BufferVertexConsumer ms = new BufferVertexConsumer();
		FpsSmoother dispatcher = KeyBinding.B()
			.aa();
		TexturedRenderLayers blockRenderer = dispatcher.b();
		Random random = new Random();
		GlShader builder = new GlShader(BufferBuilder.buffer.a());
		builder.a(GL11.GL_QUADS, BufferBuilder.buffer);
		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (c info : c.getBlocks()
			.values())
			renderWorld.a(info.a, info.b);
		for (c info : c.getBlocks()
			.values()) {
			PistonHandler state = info.b;

			if (state.h() == RedstoneLampBlock.b)
				continue;
			if (!BlockBufferBuilderStorage.canRenderInLayer(state, layer))
				continue;

			elg originalModel = dispatcher.a(state);
			ms.a();
			ms.a(info.a.getX(), info.a.getY(), info.a.getZ());
			blockRenderer.renderModel(renderWorld, originalModel, state, info.a, ms, builder, true, random, 42,
				ejo.a, EmptyModelData.INSTANCE);
			ms.b();
		}

		builder.markStateDirty();
		renderWorld.clear();
		return new SuperByteBuffer(builder);
	}

	private static void renderActors(GameMode world, Contraption c, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		BufferVertexConsumer[] matrixStacks = new BufferVertexConsumer[] { ms, msLocal };
		for (Pair<c, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;
			c blockInfo = actor.getLeft();
			for (BufferVertexConsumer m : matrixStacks) {
				m.a();
				MatrixStacker.of(m)
					.translate(blockInfo.a);
			}

			MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.b);
			if (movementBehaviour != null)
				movementBehaviour.renderInContraption(context, ms, msLocal, buffer);

			for (BufferVertexConsumer m : matrixStacks)
				m.b();
		}
	}

	public static int getLight(GameMode world, float lx, float ly, float lz) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float sky = 0, block = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.set(lx + xOffset, ly + yOffset, lz + zOffset);
					sky += world.a(TestableWorld.a, pos) / 8f;
					block += world.a(TestableWorld.b, pos) / 8f;
				}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

}

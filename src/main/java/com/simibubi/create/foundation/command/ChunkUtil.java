package com.simibubi.create.foundation.command;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.gen.decorator.Decoratable;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkUtil {
	private static final Logger LOGGER = LogManager.getLogger("Create/ChunkUtil");
	final EnumSet<Decoratable.a> POST_FEATURES = EnumSet.of(Decoratable.a.d, Decoratable.a.b, Decoratable.a.e, Decoratable.a.f);

	private final List<Long> markedChunks;
	private final List<Long> interestingChunks;

	public ChunkUtil() {
		LOGGER.debug("Chunk Util constructed");
		markedChunks = new LinkedList<>();
		interestingChunks = new LinkedList<>();
	}

	public void init() {
		BiomeArray.m = new BiomeArray("full", BiomeArray.l, 0, POST_FEATURES, BiomeArray.a.b,
				(_0, _1, _2, _3, _4, future, _6, chunk) -> future.apply(chunk),
				(_0, _1, _2, _3, future, chunk) -> {
					if (markedChunks.contains(chunk.g().a())) {
						LOGGER.debug("trying to load unforced chunk " + chunk.g().toString() + ", returning chunk loading error");
						//this.reloadChunk(world.getChunkProvider(), chunk.getPos());
						return ChunkHolder.UNLOADED_CHUNK_FUTURE;
					} else {
						//LOGGER.debug("regular, chunkStatus: " + chunk.getStatus().toString());
						return future.apply(chunk);
					}
				});

	}

	public boolean reloadChunk(ServerChunkManager provider, BlockRenderView pos) {
		ChunkHolder holder = provider.threadedAnvilChunkStorage.currentChunkHolders.remove(pos.a());
		provider.threadedAnvilChunkStorage.chunkHolderListDirty = true;
		if (holder != null) {
			provider.threadedAnvilChunkStorage.chunksToUnload.put(pos.a(), holder);
			provider.threadedAnvilChunkStorage.tryUnloadChunk(pos.a(), holder);
			return true;
		} else {
			return false;
		}
	}

	public boolean unloadChunk(ServerChunkManager provider, BlockRenderView pos) {
		this.interestingChunks.add(pos.a());
		this.markedChunks.add(pos.a());

		return this.reloadChunk(provider, pos);
	}

	public int clear(ServerChunkManager provider) {
		LinkedList<Long> copy = new LinkedList<>(this.markedChunks);

		int size = this.markedChunks.size();
		this.markedChunks.clear();

		copy.forEach(l -> reForce(provider, new BlockRenderView(l)));

		return size;
	}

	public void reForce(ServerChunkManager provider, BlockRenderView pos) {
		provider.a(pos, true);
		provider.a(pos, false);
	}

	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		//LOGGER.debug("Chunk Unload: " + event.getChunk().getPos().toString());
		if (interestingChunks.contains(event.getChunk().g().a())) {
			LOGGER.info("Interesting Chunk Unload: " + event.getChunk().g().toString());
		}
	}

	@SubscribeEvent
	public void chunkLoad(ChunkEvent.Load event) {
		//LOGGER.debug("Chunk Load: " + event.getChunk().getPos().toString());

		BlockRenderView pos = event.getChunk().g();
		if (interestingChunks.contains(pos.a())) {
			LOGGER.info("Interesting Chunk Load: " + pos.toString());
			if (!markedChunks.contains(pos.a()))
				interestingChunks.remove(pos.a());
		}


	}

}

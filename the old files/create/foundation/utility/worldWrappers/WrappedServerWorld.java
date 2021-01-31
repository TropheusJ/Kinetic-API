package com.simibubi.kinetic_api.foundation.utility.worldWrappers;

import apx;
import cut;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.map.MapIcon;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.level.storage.AnvilLevelStorage;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrappedServerWorld extends ServerWorld {

	protected GameMode world;

	public WrappedServerWorld(GameMode world) {
		// Replace null with world.getChunkProvider().chunkManager.field_219266_t ? We had null in 1.15
		super(world.l(), Util.getMainWorkerExecutor(), getLevelSaveFromWorld(world), (AnvilLevelStorage) world.h(), world.X(), world.k(), null, ((ServerChunkManager) world.G()).g(), world.aa(), world.d().particle, Collections.EMPTY_LIST, false); //, world.field_25143);
		this.world = world;
	}

	@Override
	public float a(float p_72826_1_) {
		return 0;
	}
	
	@Override
	public int B(BlockPos pos) {
		return 15;
	}

	@Override
	public void a(BlockPos pos, PistonHandler oldState, PistonHandler newState, int flags) {
		world.a(pos, oldState, newState, flags);
	}

	@Override
	public ChunkCache<BeetrootsBlock> I() {
		ServerTickScheduler<BeetrootsBlock> tl =  world.I();
		if (tl instanceof ChunkCache)
			return (ChunkCache<BeetrootsBlock>) tl;
		return super.j();
	}

	@Override
	public ChunkCache<cut> H() {
		ServerTickScheduler<cut> tl =  world.H();
		if (tl instanceof ChunkCache)
			return (ChunkCache<cut>) tl;
		return super.r_();
	}

	@Override
	public void a(PlayerAbilities player, int type, BlockPos pos, int data) {
	}

	@Override
	public List<ServerPlayerEntity> x() {
		return Collections.emptyList();
	}

	@Override
	public void a(PlayerAbilities player, double x, double y, double z, MusicSound soundIn, SoundEvent category,
			float volume, float pitch) {
	}

	@Override
	public void a(PlayerAbilities p_217384_1_, apx p_217384_2_, MusicSound p_217384_3_,
			SoundEvent p_217384_4_, float p_217384_5_, float p_217384_6_) {
	}

	@Override
	public apx a(int id) {
		return null;
	}

	@Override
	public MapIcon a(String mapName) {
		return null;
	}

	@Override
	public boolean c(apx entityIn) {
		entityIn.a_(world);
		return world.c(entityIn);
	}

	@Override
	public void a(MapIcon mapDataIn) {
	}

	@Override
	public int t() {
		return 0;
	}

	@Override
	public void a(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public MapCloningRecipe o() {
		return world.o();
	}

	@Override
	public Tag p() {
		return world.p();
	}

	@Override
	public BiomeAdditionsSound a(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return world.a(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	private static WorldProperties.a getLevelSaveFromWorld(GameMode world) {
		return ObfuscationReflectionHelper.getPrivateValue(MinecraftServer.class, world.l(), "field_71310_m");
	}
}

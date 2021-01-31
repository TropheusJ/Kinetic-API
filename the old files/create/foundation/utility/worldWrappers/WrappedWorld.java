package com.simibubi.kinetic_api.foundation.utility.worldWrappers;

import apx;
import cut;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.map.MapIcon;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.LevelProperties;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrappedWorld extends GameMode {

	protected GameMode world;

	public WrappedWorld(GameMode world) {
		super((LevelProperties) world.h(), world.X(), world.k(),
			world::Y, world.v, world.aa(), 0);
		this.world = world;
	}

	// FIXME
	// @Override
	public GameMode getWrappedWorld() {
		return world;
	}

	@Override
	public PistonHandler d_(@Nullable BlockPos pos) {
		return world.d_(pos);
	}

	@Override
	public boolean a(@Nullable BlockPos p_217375_1_, @Nullable Predicate<PistonHandler> p_217375_2_) {
		return world.a(p_217375_1_, p_217375_2_);
	}

	@Override
	public BeehiveBlockEntity c(@Nullable BlockPos pos) {
		return world.c(pos);
	}

	@Override
	public boolean a(@Nullable BlockPos pos, @Nullable PistonHandler newState, int flags) {
		return world.a(pos, newState, flags);
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
	public ServerTickScheduler<BeetrootsBlock> I() {
		return world.I();
	}

	@Override
	public ServerTickScheduler<cut> H() {
		return world.H();
	}

	@Override
	public Chunk G() {
		return world.G(); // fixme
	}

	@Override
	public void a(@Nullable PlayerAbilities player, int type, BlockPos pos, int data) {}

	@Override
	public List<? extends PlayerAbilities> x() {
		return Collections.emptyList();
	}

	@Override
	public void a(@Nullable PlayerAbilities player, double x, double y, double z, MusicSound soundIn, SoundEvent category,
			float volume, float pitch) {}

	@Override
	public void a(@Nullable PlayerAbilities p_217384_1_, apx p_217384_2_, MusicSound p_217384_3_,
			SoundEvent p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public apx a(int id) {
		return null;
	}

	@Override
	public MapIcon a(String mapName) {
		return null;
	}

	@Override
	public boolean c(@Nullable apx entityIn) {
		if(entityIn == null)
			return false;
		entityIn.a_(world);
		return world.c(entityIn);
	}

	@Override
	public void a(MapIcon mapDataIn) {}

	@Override
	public int t() {
		return world.t();
	}

	@Override
	public void a(int breakerId, BlockPos pos, int progress) {}

	@Override
	public ScoreboardObjective F() {
		return world.F();
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

	@Override
	public DynamicRegistryManager r() {
		return world.r();
	}

	@Override
	public float a(Direction p_230487_1_, boolean p_230487_2_) {
		return world.a(p_230487_1_, p_230487_2_);
	}
}

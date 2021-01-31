package com.simibubi.kinetic_api.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import apx;
import bck;
import bcm;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.worldWrappers.WrappedWorld;
import cqx;
import cut;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.timer.Timer;

public class SchematicWorld extends WrappedWorld implements VerticalBlockSample {

	private Map<BlockPos, PistonHandler> blocks;
	private Map<BlockPos, BeehiveBlockEntity> tileEntities;
	private List<BeehiveBlockEntity> renderedTileEntities;
	private List<apx> entities;
	private cqx bounds;
	public BlockPos anchor;
	public boolean renderMode;

	public SchematicWorld(GameMode original) {
		this(BlockPos.ORIGIN, original);
	}
	
	public SchematicWorld(BlockPos anchor, GameMode original) {
		super(original);
		this.blocks = new HashMap<>();
		this.tileEntities = new HashMap<>();
		this.bounds = new cqx();
		this.anchor = anchor;
		this.entities = new ArrayList<>();
		this.renderedTileEntities = new ArrayList<>();
	}

	public Set<BlockPos> getAllPositions() {
		return blocks.keySet();
	}

	@Override
	public boolean c(apx entityIn) {
		if (entityIn instanceof bcm)
			((bcm) entityIn).o()
				.c(null);
		if (entityIn instanceof bck) {
			bck armorStandEntity = (bck) entityIn;
			armorStandEntity.bo()
				.forEach(stack -> stack.c(null));
		}

		return entities.add(entityIn);
	}

	public Stream<apx> getEntities() {
		return entities.stream();
	}

	@Override
	public BeehiveBlockEntity c(BlockPos pos) {
		if (m(pos))
			return null;
		if (tileEntities.containsKey(pos))
			return tileEntities.get(pos);
		if (!blocks.containsKey(pos.subtract(anchor)))
			return null;

		PistonHandler blockState = d_(pos);
		if (blockState.hasTileEntity()) {
			try {
				BeehiveBlockEntity tileEntity = blockState.createTileEntity(this);
				if (tileEntity != null) {
					tileEntity.a(this, pos);
					tileEntities.put(pos, tileEntity);
					renderedTileEntities.add(tileEntity);
				}
				return tileEntity;
			} catch (Exception e) {
				Create.logger.debug("Could not kinetic_api TE of block " + blockState + ": " + e);
			}
		}
		return null;
	}

	@Override
	public PistonHandler d_(BlockPos globalPos) {
		BlockPos pos = globalPos.subtract(anchor);

		if (pos.getY() - bounds.b == -1 && !renderMode)
			return BellBlock.NORTH_SOUTH_WALLS_SHAPE.n();
		if (getBounds().b(pos) && blocks.containsKey(pos)) {
			PistonHandler blockState = blocks.get(pos);
			if (BlockHelper.hasBlockStateProperty(blockState, BambooLeaves.r))
				blockState = blockState.a(BambooLeaves.r, false);
			return blockState;
		}
		return BellBlock.FACING.n();
	}

	public Map<BlockPos, PistonHandler> getBlockMap() {
		return blocks;
	}

	@Override
	public EmptyFluid b(BlockPos pos) {
		return d_(pos).m();
	}

	@Override
	public BiomeAdditionsSound v(BlockPos pos) {
		return BuiltinBiomes.THE_VOID;
	}

	@Override
	public int a(TestableWorld p_226658_1_, BlockPos p_226658_2_) {
		return 10;
	}

	@Override
	public List<apx> a(apx arg0, Timer arg1, Predicate<? super apx> arg2) {
		return Collections.emptyList();
	}

	@Override
	public <T extends apx> List<T> a(Class<? extends T> arg0, Timer arg1,
		Predicate<? super T> arg2) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends PlayerAbilities> x() {
		return Collections.emptyList();
	}

	@Override
	public int getAmbientDarkness() {
		return 0;
	}

	@Override
	public boolean a(BlockPos pos, Predicate<PistonHandler> predicate) {
		return predicate.test(d_(pos));
	}

	@Override
	public boolean b(BlockPos arg0, boolean arg1) {
		return a(arg0, BellBlock.FACING.n(), 3);
	}

	@Override
	public boolean a(BlockPos arg0, boolean arg1) {
		return a(arg0, BellBlock.FACING.n(), 3);
	}

	@Override
	public boolean a(BlockPos pos, PistonHandler arg1, int arg2) {
		pos = pos.subtract(anchor);
		bounds.c(new cqx(pos, pos.add(1, 1, 1)));
		blocks.put(pos, arg1);
		return true;
	}

	@Override
	public ServerTickScheduler<BeetrootsBlock> I() {
		return Spawner.b();
	}

	@Override
	public ServerTickScheduler<cut> H() {
		return Spawner.b();
	}

	public cqx getBounds() {
		return bounds;
	}

	public Iterable<BeehiveBlockEntity> getRenderedTileEntities() {
		return renderedTileEntities;
	}

	@Override
	public ServerWorld E() {
		if (this.world instanceof ServerWorld) {
			return (ServerWorld) this.world;
		}
		throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
	}
}

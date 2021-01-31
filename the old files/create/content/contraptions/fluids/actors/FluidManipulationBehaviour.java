package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.simibubi.kinetic_api.AllTags.AllFluidTags;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import cqx;
import cut;
import net.minecraft.client.sound.MusicType;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidManipulationBehaviour extends TileEntityBehaviour {

	protected static class BlockPosEntry {
		public BlockPos pos;
		public int distance;

		public BlockPosEntry(BlockPos pos, int distance) {
			this.pos = pos;
			this.distance = distance;
		}
	}

	cqx affectedArea;
	BlockPos rootPos;
	boolean infinite;
	protected boolean counterpartActed;

	// Search
	static final int searchedPerTick = 256;
	List<BlockPosEntry> frontier;
	Set<BlockPos> visited;

	static final int validationTimer = 160;
	int revalidateIn;

	public FluidManipulationBehaviour(SmartTileEntity te) {
		super(te);
		setValidationTimer();
		infinite = false;
		visited = new HashSet<>();
		frontier = new ArrayList<>();
	}

	public void counterpartActed() {
		counterpartActed = true;
	}

	protected int setValidationTimer() {
		return revalidateIn = validationTimer;
	}

	protected int setLongValidationTimer() {
		return revalidateIn = validationTimer * 2;
	}

	protected int maxRange() {
		return AllConfigs.SERVER.fluids.hosePulleyRange.get();
	}

	protected int maxBlocks() {
		return AllConfigs.SERVER.fluids.hosePulleyBlockThreshold.get();
	}

	public void reset() {
		if (affectedArea != null)
			scheduleUpdatesInAffectedArea();
		affectedArea = null;
		setValidationTimer();
		frontier.clear();
		visited.clear();
		infinite = false;
	}

	@Override
	public void destroy() {
		reset();
		super.destroy();
	}

	protected void scheduleUpdatesInAffectedArea() {
		GameMode world = getWorld();
		BlockPos.stream(new BlockPos(affectedArea.a - 1, affectedArea.b - 1, affectedArea.c - 1), new BlockPos(affectedArea.d + 1, affectedArea.e + 1, affectedArea.f + 1))
			.forEach(pos -> {
				EmptyFluid nextFluidState = world.b(pos);
				if (nextFluidState.c())
					return;
				world.H()
					.a(pos, nextFluidState.a(), world.getRandom()
						.nextInt(5));
			});
	}

	protected int comparePositions(BlockPosEntry e1, BlockPosEntry e2) {
		EntityHitResult centerOfRoot = VecHelper.getCenterOf(rootPos);
		BlockPos pos2 = e2.pos;
		BlockPos pos1 = e1.pos;
		if (pos1.getY() != pos2.getY())
			return Integer.compare(pos2.getY(), pos1.getY());
		int compareDistance = Integer.compare(e2.distance, e1.distance);
		if (compareDistance != 0)
			return compareDistance;
		return Double.compare(VecHelper.getCenterOf(pos2)
			.g(centerOfRoot),
			VecHelper.getCenterOf(pos1)
				.g(centerOfRoot));
	}

	protected cut search(cut fluid, List<BlockPosEntry> frontier, Set<BlockPos> visited,
		BiConsumer<BlockPos, Integer> add, boolean searchDownward) {
		GameMode world = getWorld();
		int maxBlocks = maxBlocks();
		int maxRange = canDrainInfinitely(fluid) ? maxRange() : maxRange() / 2;
		int maxRangeSq = maxRange * maxRange;
		int i;

		for (i = 0; i < searchedPerTick && !frontier.isEmpty()
			&& (visited.size() <= maxBlocks || !canDrainInfinitely(fluid)); i++) {
			BlockPosEntry entry = frontier.remove(0);
			BlockPos currentPos = entry.pos;
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			EmptyFluid fluidState = world.b(currentPos);
			if (fluidState.c())
				continue;

			cut currentFluid = FluidHelper.convertToStill(fluidState.a());
			if (fluid == null)
				fluid = currentFluid;
			if (!currentFluid.a(fluid))
				continue;

			add.accept(currentPos, entry.distance);

			for (Direction side : Iterate.directions) {
				if (!searchDownward && side == Direction.DOWN)
					continue;

				BlockPos offsetPos = currentPos.offset(side);
				if (visited.contains(offsetPos))
					continue;
				if (offsetPos.getSquaredDistance(rootPos) > maxRangeSq)
					continue;

				EmptyFluid nextFluidState = world.b(offsetPos);
				if (nextFluidState.c())
					continue;
				cut nextFluid = nextFluidState.a();
				if (nextFluid == FluidHelper.convertToFlowing(nextFluid) && side == Direction.UP
					&& !VecHelper.onSameAxis(rootPos, offsetPos, Axis.Y))
					continue;

				frontier.add(new BlockPosEntry(offsetPos, entry.distance + 1));
			}
		}
		
		return fluid;
	}

	protected void playEffect(GameMode world, BlockPos pos, cut fluid, boolean fillSound) {
		BlockPos splooshPos = pos == null ? tileEntity.o() : pos;

		MusicSound soundevent = fillSound ? fluid.getAttributes()
			.getFillSound()
			: fluid.getAttributes()
				.getEmptySound();
		if (soundevent == null)
			soundevent = fluid.a(BlockTags.field_15471)
				? fillSound ? MusicType.bo : MusicType.bl
				: fillSound ? MusicType.bm : MusicType.bj;

		world.a(null, splooshPos, soundevent, SoundEvent.e, 0.3F, 1.0F);
		if (world instanceof ServerWorld)
			AllPackets.sendToNear(world, splooshPos, 10, new FluidSplashPacket(splooshPos, new FluidStack(fluid, 1)));
	}
	
	protected boolean canDrainInfinitely(cut fluid) {
		return maxBlocks() != -1 && !AllFluidTags.NO_INFINITE_DRAINING.matches(fluid);
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		if (rootPos != null)
			nbt.put("LastPos", NbtHelper.fromBlockPos(rootPos));
		if (affectedArea != null) {
			nbt.put("AffectedAreaFrom",
				NbtHelper.fromBlockPos(new BlockPos(affectedArea.a, affectedArea.b, affectedArea.c)));
			nbt.put("AffectedAreaTo",
				NbtHelper.fromBlockPos(new BlockPos(affectedArea.d, affectedArea.e, affectedArea.f)));
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		if (nbt.contains("LastPos"))
			rootPos = NbtHelper.toBlockPos(nbt.getCompound("LastPos"));
		if (nbt.contains("AffectedAreaFrom") && nbt.contains("AffectedAreaTo"))
			affectedArea = new cqx(NbtHelper.toBlockPos(nbt.getCompound("AffectedAreaFrom")),
				NbtHelper.toBlockPos(nbt.getCompound("AffectedAreaTo")));
		super.read(nbt, clientPacket);
	}

}

package com.simibubi.create.foundation.tileEntity;

import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public abstract class TileEntityBehaviour {

	public SmartTileEntity tileEntity;
	private int lazyTickRate;
	private int lazyTickCounter;

	public TileEntityBehaviour(SmartTileEntity te) {
		tileEntity = te;
		setLazyTickRate(10);
	}

	public abstract BehaviourType<?> getType();

	public void initialize() {

	}

	public void tick() {
		if (lazyTickCounter-- <= 0) {
			lazyTickCounter = lazyTickRate;
			lazyTick();
		}

	}

	public void read(CompoundTag nbt, boolean clientPacket) {

	}

	public void write(CompoundTag nbt, boolean clientPacket) {

	}

	public void onBlockChanged(PistonHandler oldState) {

	}

	public void onNeighborChanged(Direction direction) {

	}

	public void remove() {

	}

	public void destroy() {

	}

	public void setLazyTickRate(int slowTickRate) {
		this.lazyTickRate = slowTickRate;
		this.lazyTickCounter = slowTickRate;
	}

	public void lazyTick() {

	}

	public BlockPos getPos() {
		return tileEntity.o();
	}

	public GameMode getWorld() {
		return tileEntity.v();
	}

	public static <T extends TileEntityBehaviour> T get(MobSpawnerLogic reader, BlockPos pos,
			BehaviourType<T> type) {
		return get(reader.c(pos), type);
	}
	
	public static <T extends TileEntityBehaviour> void destroy(MobSpawnerLogic reader, BlockPos pos,
			BehaviourType<T> type) {
		T behaviour = get(reader.c(pos), type);
		if (behaviour != null)
			behaviour.destroy();
	}

	public static <T extends TileEntityBehaviour> T get(BeehiveBlockEntity te, BehaviourType<T> type) {
		if (te == null)
			return null;
		if (!(te instanceof SmartTileEntity))
			return null;
		SmartTileEntity ste = (SmartTileEntity) te;
		return ste.getBehaviour(type);
	}

}

package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.kinetic_api.AllTags.AllBlockTags;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public class BearingContraption extends Contraption {

	protected int sailBlocks;
	protected Direction facing;
	
	private boolean isWindmill;

	public BearingContraption() {}

	public BearingContraption(boolean isWindmill, Direction facing) {
		this.isWindmill = isWindmill;
		this.facing = facing;
	}

	@Override
	public boolean assemble(GameMode world, BlockPos pos) {
		BlockPos offset = pos.offset(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		expandBoundsAroundAxis(facing.getAxis());
		if (isWindmill && sailBlocks == 0)
			return false;
		if (blocks.isEmpty())
			return false;
		return true;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.BEARING;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor.offset(facing.getOpposite()));
	}

	@Override
	public void addBlock(BlockPos pos, Pair<c, BeehiveBlockEntity> capture) {
		BlockPos localPos = pos.subtract(anchor);
		if (!getBlocks().containsKey(localPos) && AllBlockTags.WINDMILL_SAILS.matches(capture.getKey().b))
			sailBlocks++;
		super.addBlock(pos, capture);
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("Sails", sailBlocks);
		tag.putInt("Facing", facing.getId());
		return tag;
	}

	@Override
	public void readNBT(GameMode world, CompoundTag tag, boolean spawnData) {
		sailBlocks = tag.getInt("Sails");
		facing = Direction.byId(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}

	public int getSailBlocks() {
		return sailBlocks;
	}

	public Direction getFacing() {
		return facing;
	}

	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return axis == facing.getAxis();
	}

}

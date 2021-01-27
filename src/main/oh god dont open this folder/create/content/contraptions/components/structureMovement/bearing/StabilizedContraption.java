package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public class StabilizedContraption extends Contraption {

	private Direction facing;

	public StabilizedContraption() {}

	public StabilizedContraption(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean assemble(GameMode world, BlockPos pos) {
		BlockPos offset = pos.offset(facing);
		if (!searchMovedStructure(world, offset, null))
			return false;
		startMoving(world);
		expandBoundsAroundAxis(Axis.Y);
		if (blocks.isEmpty())
			return false;
		return true;
	}
	
	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return false;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.STABILIZED;
	}
	
	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("Facing", facing.getId());
		return tag;
	}

	@Override
	public void readNBT(GameMode world, CompoundTag tag, boolean spawnData) {
		facing = Direction.byId(tag.getInt("Facing"));
		super.readNBT(world, tag, spawnData);
	}
	
	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return false;
	}
	
	public Direction getFacing() {
		return facing;
	}

}

package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.TranslatingContraption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class PulleyContraption extends TranslatingContraption {

	int initialOffset;

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.PULLEY;
	}

	public PulleyContraption() {}

	public PulleyContraption(int initialOffset) {
		this.initialOffset = initialOffset;
	}

	@Override
	public boolean assemble(GameMode world, BlockPos pos) {
		if (!searchMovedStructure(world, pos, null))
			return false;
		startMoving(world);
		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		if (pos.getX() != anchor.getX() || pos.getZ() != anchor.getZ())
			return false;
		int y = pos.getY();
		if (y <= anchor.getY() || y > anchor.getY() + initialOffset + 1)
			return false;
		return true;
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialOffset", initialOffset);
		return tag;
	}

	@Override
	public void readNBT(GameMode world, CompoundTag nbt, boolean spawnData) {
		initialOffset = nbt.getInt("InitialOffset");
		super.readNBT(world, nbt, spawnData);
	}

}

package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public abstract class TranslatingContraption extends Contraption {

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;

	public Set<BlockPos> getColliders(GameMode world, Direction movementDirection) {
		if (getBlocks() == null)
			return Collections.EMPTY_SET;
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliders = new HashSet<>();
			cachedColliderDirection = movementDirection;

			for (c info : getBlocks().values()) {
				BlockPos offsetPos = info.a.offset(movementDirection);
				if (info.b.k(world, offsetPos)
					.b())
					continue;
				if (getBlocks().containsKey(offsetPos)
					&& !getBlocks().get(offsetPos).b.k(world, offsetPos)
						.b())
					continue;
				cachedColliders.add(info.a);
			}

		}
		return cachedColliders;
	}

	@Override
	public void removeBlocksFromWorld(GameMode world, BlockPos offset) {
		int count = blocks.size();
		super.removeBlocksFromWorld(world, offset);
		if (count != blocks.size()) {
			cachedColliders = null;
		}
	}

	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return false;
	}
	
}

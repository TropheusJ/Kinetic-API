package com.tropheus_jay.kinetic_api;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.HashSet;

import static com.tropheus_jay.kinetic_api.AllBlocks.CREATIVE_MOTOR;

public class AllEntityTypes {
		// I have no idea if this will work, I really hope it does
		public static final BlockEntityType CREATIVE_MOTOR_TILE_ENTITY_TYPE = new BlockEntityType(null, new HashSet<Block>((Collection<? extends Block>) CREATIVE_MOTOR), null);
	
}

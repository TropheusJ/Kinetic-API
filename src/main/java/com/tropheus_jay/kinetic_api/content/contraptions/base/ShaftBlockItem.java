package com.tropheus_jay.kinetic_api.content.contraptions.base;

import com.tropheus_jay.kinetic_api.content.contraptions.relays.elementary.ShaftBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class ShaftBlockItem extends BlockItem {
	
	public ShaftBlockItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	public ShaftBlockItem(ShaftBlock block, Settings settings) {
		super(block, settings);
	}
	
	
	
}

package com.tropheus_jay.kinetic_api.content.contraptions.goggles;

import net.minecraft.text.Text;

import java.util.List;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveHoveringInformation {

	default boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking){
		return false;
	}

}

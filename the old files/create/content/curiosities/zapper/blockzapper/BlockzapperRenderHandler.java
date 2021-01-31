package com.simibubi.kinetic_api.content.curiosities.zapper.blockzapper;

import java.util.Collections;
import java.util.List;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.CreateClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class BlockzapperRenderHandler {

	private static List<BlockPos> renderedShape;

	public static void tick() {
		gatherSelectedBlocks();
		if (renderedShape.isEmpty())
			return;

		CreateClient.outliner.showCluster("blockzapper", renderedShape)
			.colored(0xbfbfbf)
			.lineWidth(1 / 32f)
			.withFaceTexture(AllSpecialTextures.CHECKERED);
	}

	protected static void gatherSelectedBlocks() {
		FishingParticle player = KeyBinding.B().s;
		ItemCooldownManager heldMain = player.dC();
		ItemCooldownManager heldOff = player.dD();
		boolean zapperInMain = AllItems.BLOCKZAPPER.isIn(heldMain);
		boolean zapperInOff = AllItems.BLOCKZAPPER.isIn(heldOff);

		if (zapperInMain) {
			CompoundTag tag = heldMain.p();
			if (!tag.contains("_Swap") || !zapperInOff) {
				createOutline(player, heldMain);
				return;
			}
		}

		if (zapperInOff) {
			createOutline(player, heldOff);
			return;
		}

		renderedShape = Collections.emptyList();
	}

	private static void createOutline(FishingParticle player, ItemCooldownManager held) {
		if (!held.p().contains("BlockUsed")) {
			renderedShape = Collections.emptyList();
			return;
		}
		renderedShape = BlockzapperItem.getSelectedBlocks(held, player.l, player);
	}

}

package com.simibubi.kinetic_api.content.curiosities.zapper.terrainzapper;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;

public class WorldshaperRenderHandler {

	private static List<BlockPos> renderedShape;
	private static BlockPos renderedPosition;

	public static void tick() {
		gatherSelectedBlocks();
		if (renderedPosition == null)
			return;

		CreateClient.outliner.showCluster("terrainZapper", renderedShape.stream()
			.map(pos -> pos.add(renderedPosition))
			.collect(Collectors.toList()))
			.colored(0xbfbfbf)
			.lineWidth(1 / 32f)
			.withFaceTexture(AllSpecialTextures.CHECKERED);
	}

	protected static void gatherSelectedBlocks() {
		FishingParticle player = KeyBinding.B().s;
		ItemCooldownManager heldMain = player.dC();
		ItemCooldownManager heldOff = player.dD();
		boolean zapperInMain = AllItems.WORLDSHAPER.isIn(heldMain);
		boolean zapperInOff = AllItems.WORLDSHAPER.isIn(heldOff);

		if (zapperInMain) {
			CompoundTag tag = heldMain.p();
			if (!tag.contains("_Swap") || !zapperInOff) {
				createBrushOutline(tag, player, heldMain);
				return;
			}
		}

		if (zapperInOff) {
			CompoundTag tag = heldOff.p();
			createBrushOutline(tag, player, heldOff);
			return;
		}

		renderedPosition = null;
	}

	public static void createBrushOutline(CompoundTag tag, FishingParticle player, ItemCooldownManager zapper) {
		if (!tag.contains("BrushParams")) {
			renderedPosition = null;
			return;
		}

		Brush brush = NBTHelper.readEnum(tag, "Brush", TerrainBrushes.class)
			.get();
		PlacementOptions placement = NBTHelper.readEnum(tag, "Placement", PlacementOptions.class);
		BlockPos params = NbtHelper.toBlockPos(tag.getCompound("BrushParams"));
		brush.set(params.getX(), params.getY(), params.getZ());
		renderedShape = brush.getIncludedPositions();

		EntityHitResult start = player.cz()
			.b(0, player.cd(), 0);
		EntityHitResult range = player.bg()
			.a(128);
		dcg raytrace = player.l
			.a(new BlockView(start, start.e(range), a.b, b.a, player));
		if (raytrace == null || raytrace.c() == net.minecraft.util.math.Box.a.a) {
			renderedPosition = null;
			return;
		}

		BlockPos pos = raytrace.a();
		renderedPosition = pos.add(brush.getOffset(player.bg(), raytrace.b(), placement));
	}

}

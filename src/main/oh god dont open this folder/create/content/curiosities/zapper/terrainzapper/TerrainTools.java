package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

public enum TerrainTools {

	Fill(AllIcons.I_FILL),
	Place(AllIcons.I_PLACE),
	Replace(AllIcons.I_REPLACE),
	Clear(AllIcons.I_CLEAR),
	Overlay(AllIcons.I_OVERLAY),
	Flatten(AllIcons.I_FLATTEN);

	public String translationKey;
	public AllIcons icon;

	private TerrainTools(AllIcons icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	public boolean requiresSelectedBlock() {
		return this != Clear && this != Flatten;
	}

	public void run(GameMode world, List<BlockPos> targetPositions, Direction facing, @Nullable PistonHandler paintedState, @Nullable CompoundTag data) {
		switch (this) {
		case Clear:
			targetPositions.forEach(p -> world.a(p, BellBlock.FACING.n()));
			break;
		case Fill:
			targetPositions.forEach(p -> {
				PistonHandler toReplace = world.d_(p);
				if (!isReplaceable(toReplace))
					return;
				world.a(p, paintedState);
				ZapperItem.setTileData(world, p, paintedState, data);
			});
			break;
		case Flatten:
			FlattenTool.apply(world, targetPositions, facing);
			break;
		case Overlay:
			targetPositions.forEach(p -> {
				PistonHandler toOverlay = world.d_(p);
				if (isReplaceable(toOverlay))
					return;
				if (toOverlay == paintedState)
					return;

				p = p.up();

				PistonHandler toReplace = world.d_(p);
				if (!isReplaceable(toReplace))
					return;
				world.a(p, paintedState);
				ZapperItem.setTileData(world, p, paintedState, data);
			});
			break;
		case Place:
			targetPositions.forEach(p -> {
				world.a(p, paintedState);
				ZapperItem.setTileData(world, p, paintedState, data);
			});
			break;
		case Replace:
			targetPositions.forEach(p -> {
				PistonHandler toReplace = world.d_(p);
				if (isReplaceable(toReplace))
					return;
				world.a(p, paintedState);
				ZapperItem.setTileData(world, p, paintedState, data);
			});
			break;
		}
	}

	public static boolean isReplaceable(PistonHandler toReplace) {
		return toReplace.c().e();
	}

}

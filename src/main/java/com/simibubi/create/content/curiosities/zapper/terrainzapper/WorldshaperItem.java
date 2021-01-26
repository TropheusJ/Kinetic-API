package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.curiosities.zapper.PlacementPatterns;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldshaperItem extends ZapperItem {

	public WorldshaperItem(a properties) {
		super(properties);
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void openHandgunGUI(ItemCooldownManager item, boolean b) {
		ScreenOpener.open(new WorldshaperScreen(item, b));
	}

	@Override
	protected int getZappingRange(ItemCooldownManager stack) {
		return 128;
	}

	@Override
	protected int getCooldownDelay(ItemCooldownManager item) {
		return 2;
	}

	@Override
	public Text validateUsage(ItemCooldownManager item) {
		if (!item.p()
			.contains("BrushParams"))
			return Lang.createTranslationTextComponent("terrainzapper.shiftRightClickToSet");
		return super.validateUsage(item);
	}

	@Override
	protected boolean canActivateWithoutSelectedBlock(ItemCooldownManager stack) {
		CompoundTag tag = stack.p();
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);
		return !tool.requiresSelectedBlock();
	}

	@Override
	protected boolean activate(GameMode world, PlayerAbilities player, ItemCooldownManager stack, PistonHandler stateToUse,
		dcg raytrace, CompoundTag data) {

		BlockPos targetPos = raytrace.a();
		List<BlockPos> affectedPositions = new ArrayList<>();

		CompoundTag tag = stack.p();
		Brush brush = NBTHelper.readEnum(tag, "Brush", TerrainBrushes.class)
			.get();
		BlockPos params = NbtHelper.toBlockPos(tag.getCompound("BrushParams"));
		PlacementOptions option = NBTHelper.readEnum(tag, "Placement", PlacementOptions.class);
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);

		brush.set(params.getX(), params.getY(), params.getZ());
		targetPos = targetPos.add(brush.getOffset(player.bg(), raytrace.b(), option));
		for (BlockPos blockPos : brush.getIncludedPositions())
			affectedPositions.add(targetPos.add(blockPos));
		PlacementPatterns.applyPattern(affectedPositions, stack);
		tool.run(world, affectedPositions, raytrace.b(), stateToUse, data);

		return true;
	}

}

package com.simibubi.create.content.schematics.item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import bnx;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.client.SchematicEditScreen;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public class SchematicItem extends HoeItem {

	public SchematicItem(a properties) {
		super(properties.a(1));
	}

	public static ItemCooldownManager create(String schematic, String owner) {
		ItemCooldownManager blueprint = AllItems.SCHEMATIC.asStack();

		CompoundTag tag = new CompoundTag();
		tag.putBoolean("Deployed", false);
		tag.putString("Owner", owner);
		tag.putString("File", schematic);
		tag.put("Anchor", NbtHelper.fromBlockPos(BlockPos.ORIGIN));
		tag.putString("Rotation", RespawnAnchorBlock.CHARGES.name());
		tag.putString("Mirror", LoomBlock.TITLE.name());
		blueprint.c(tag);

		writeSize(blueprint);
		return blueprint;
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(ItemCooldownManager stack, GameMode worldIn, List<Text> tooltip, ToolItem flagIn) {
		if (stack.n()) {
			if (stack.o()
				.contains("File"))
				tooltip.add(new LiteralText(Formatting.GOLD + stack.o()
					.getString("File")));
		} else {
			tooltip.add(Lang.translate("schematic.invalid").formatted(Formatting.RED));
		}
		super.a(stack, worldIn, tooltip, flagIn);
	}

	public static void writeSize(ItemCooldownManager blueprint) {
		CompoundTag tag = blueprint.o();
		StructureProcessor t = loadSchematic(blueprint);
		tag.put("Bounds", NbtHelper.fromBlockPos(t.a()));
		blueprint.c(tag);
	}

	public static RuleTest getSettings(ItemCooldownManager blueprint) {
		CompoundTag tag = blueprint.o();
		RuleTest settings = new RuleTest();
		settings.a(RespawnAnchorBlock.valueOf(tag.getString("Rotation")));
		settings.a(LoomBlock.valueOf(tag.getString("Mirror")));
		return settings;
	}

	public static StructureProcessor loadSchematic(ItemCooldownManager blueprint) {
		StructureProcessor t = new StructureProcessor();
		String owner = blueprint.o()
			.getString("Owner");
		String schematic = blueprint.o()
			.getString("File");

		String filepath = "";

		if (Thread.currentThread()
			.getThreadGroup() == SidedThreadGroups.SERVER)
			filepath = "schematics/uploaded/" + owner + "/" + schematic;
		else
			filepath = "schematics/" + schematic;

		InputStream stream = null;
		try {
			stream = Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ);
			CompoundTag nbt = NbtIo.readCompressed(stream);
			t.b(nbt);

		} catch (IOException e) {
			// Player/Server doesnt have schematic saved
		} finally {
			if (stream != null)
				IOUtils.closeQuietly(stream);
		}

		return t;
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		if (context.n() != null && !onItemUse(context.n(), context.o()))
			return super.a(context);
		return Difficulty.SUCCESS;
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode worldIn, PlayerAbilities playerIn, ItemScatterer handIn) {
		if (!onItemUse(playerIn, handIn))
			return super.a(worldIn, playerIn, handIn);
		return new LocalDifficulty<ItemCooldownManager>(Difficulty.SUCCESS, playerIn.b(handIn));
	}

	private boolean onItemUse(PlayerAbilities player, ItemScatterer hand) {
		if (!player.bt() || hand != ItemScatterer.RANDOM)
			return false;
		if (!player.b(hand)
			.n())
			return false;
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this::displayBlueprintScreen);
		return true;
	}

	@Environment(EnvType.CLIENT)
	protected void displayBlueprintScreen() {
		ScreenOpener.open(new SchematicEditScreen());
	}

}

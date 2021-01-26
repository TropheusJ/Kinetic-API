package com.simibubi.create.content.curiosities.zapper.blockzapper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import apx;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.curiosities.zapper.PlacementPatterns;
import com.simibubi.create.content.curiosities.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockzapperItem extends ZapperItem {

	public BlockzapperItem(a properties) {
		super(properties);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void a(ItemCooldownManager stack, GameMode worldIn, List<Text> tooltip, ToolItem flagIn) {
		super.a(stack, worldIn, tooltip, flagIn);
		Palette palette = Palette.Purple;
		if (PresetsScreen.y()) {
			ItemDescription.add(tooltip, Lang.translate("blockzapper.componentUpgrades").formatted(palette.color));

			for (Components c : Components.values()) {
				ComponentTier tier = getTier(c, stack);
				Text componentName =
					Lang.translate("blockzapper.component." + Lang.asId(c.name())).formatted(Formatting.GRAY);
				Text tierName = Lang.translate("blockzapper.componentTier." + Lang.asId(tier.name())).formatted(tier.color);
				ItemDescription.add(tooltip, new LiteralText("> ").append(componentName).append(": ").append(tierName));
			}
		}
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {
		if (group != Create.baseCreativeTab && group != ChorusFruitItem.g)
			return;
		
		ItemCooldownManager gunWithoutStuff = new ItemCooldownManager(this);
		items.add(gunWithoutStuff);

		ItemCooldownManager gunWithGoldStuff = new ItemCooldownManager(this);
		for (Components c : Components.values())
			setTier(c, ComponentTier.Brass, gunWithGoldStuff);
		items.add(gunWithGoldStuff);

		ItemCooldownManager gunWithPurpurStuff = new ItemCooldownManager(this);
		for (Components c : Components.values())
			setTier(c, ComponentTier.Chromatic, gunWithPurpurStuff);
		items.add(gunWithPurpurStuff);
	}

	@Override
	protected boolean activate(GameMode world, PlayerAbilities player, ItemCooldownManager stack, PistonHandler selectedState,
		dcg raytrace, CompoundTag data) {
		CompoundTag nbt = stack.p();
		boolean replace = nbt.contains("Replace") && nbt.getBoolean("Replace");

		List<BlockPos> selectedBlocks = getSelectedBlocks(stack, world, player);
		PlacementPatterns.applyPattern(selectedBlocks, stack);
		Direction face = raytrace.b();

		for (BlockPos placed : selectedBlocks) {
			if (world.d_(placed) == selectedState)
				continue;
			if (!selectedState.a(world, placed))
				continue;
			if (!player.b_() && !canBreak(stack, world.d_(placed), world, placed))
				continue;
			if (!player.b_() && BlockHelper.findAndRemoveInInventory(selectedState, player, 1) == 0) {
				player.eS()
					.a(stack.b(), 20);
				player.a( Lang.translate("blockzapper.empty").formatted(Formatting.RED), true);
				return false;
			}

			if (!player.b_() && replace)
				dropBlocks(world, player, stack, face, placed);

			PistonHandler state = selectedState;
			for (Direction updateDirection : Iterate.directions)
				state = state.a(updateDirection,
					world.d_(placed.offset(updateDirection)), world, placed, placed.offset(updateDirection));

			BlockSnapshot blocksnapshot = BlockSnapshot.create(world.X(), world, placed);
			EmptyFluid FluidState = world.b(placed);
			world.a(placed, FluidState.g(), BlockFlags.UPDATE_NEIGHBORS);
			world.a(placed, state);

			if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, Direction.UP)) {
				blocksnapshot.restore(true, false);
				return false;
			}
			setTileData(world, placed, state, data);

			if (player instanceof ServerPlayerEntity && world instanceof ServerWorld) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				Criteria.PLACED_BLOCK.a(serverPlayer, placed, new ItemCooldownManager(state.b()));

				boolean fullyUpgraded = true;
				for (Components c : Components.values()) {
					if (getTier(c, stack) != ComponentTier.Chromatic) {
						fullyUpgraded = false;
						break;
					}
				}
				if (fullyUpgraded)
					AllTriggers.UPGRADED_ZAPPER.trigger(serverPlayer);
			}
		}
		for (BlockPos placed : selectedBlocks) {
			world.a(placed, selectedState.b(), placed);
		}

		return true;
	}

	@Override
	public void a(ItemCooldownManager stack, GameMode worldIn, apx entityIn, int itemSlot, boolean isSelected) {
		if (AllItems.BLOCKZAPPER.isIn(stack)) {
			CompoundTag nbt = stack.p();
			if (!nbt.contains("Replace"))
				nbt.putBoolean("Replace", false);
			if (!nbt.contains("Pattern"))
				nbt.putString("Pattern", PlacementPatterns.Solid.name());
			if (!nbt.contains("SearchDiagonal"))
				nbt.putBoolean("SearchDiagonal", false);
			if (!nbt.contains("SearchMaterial"))
				nbt.putBoolean("SearchMaterial", false);
			if (!nbt.contains("SearchDistance"))
				nbt.putInt("SearchDistance", 1);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void openHandgunGUI(ItemCooldownManager handgun, boolean offhand) {
		ScreenOpener.open(new BlockzapperScreen(handgun, offhand));
	}

	public static List<BlockPos> getSelectedBlocks(ItemCooldownManager stack, GameMode worldIn, PlayerAbilities player) {
		List<BlockPos> list = new LinkedList<>();
		CompoundTag tag = stack.o();
		if (tag == null)
			return list;

		boolean searchDiagonals = tag.contains("SearchDiagonal") && tag.getBoolean("SearchDiagonal");
		boolean searchAcrossMaterials = tag.contains("SearchFuzzy") && tag.getBoolean("SearchFuzzy");
		boolean replace = tag.contains("Replace") && tag.getBoolean("Replace");
		int searchRange = tag.contains("SearchDistance") ? tag.getInt("SearchDistance") : 0;

		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		EntityHitResult start = player.cz()
			.b(0, player.cd(), 0);
		EntityHitResult range = player.bg()
			.a(ZapperInteractionHandler.getRange(stack));
		dcg raytrace = player.l
			.a(new BlockView(start, start.e(range), net.minecraft.world.BlockView.a.a, b.a, player));
		BlockPos pos = raytrace.a()
			.toImmutable();

		if (pos == null)
			return list;

		PistonHandler state = worldIn.d_(pos);
		Direction face = raytrace.b();
		List<BlockPos> offsets = new LinkedList<>();

		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (Math.abs(x) + Math.abs(y) + Math.abs(z) < 2 || searchDiagonals)
						if (face.getAxis()
							.choose(x, y, z) == 0)
							offsets.add(new BlockPos(x, y, z));

		BlockPos startPos = replace ? pos : pos.offset(face);
		frontier.add(startPos);

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			if (!currentPos.isWithinDistance(startPos, searchRange))
				continue;

			// Replace Mode
			if (replace) {
				PistonHandler stateToReplace = worldIn.d_(currentPos);
				PistonHandler stateAboveStateToReplace = worldIn.d_(currentPos.offset(face));

				// Criteria
				if (stateToReplace.h(worldIn, currentPos) == -1)
					continue;
				if (stateToReplace.b() != state.b() && !searchAcrossMaterials)
					continue;
				if (stateToReplace.c()
					.e())
					continue;
				if (stateAboveStateToReplace.l())
					continue;
				list.add(currentPos);

				// Search adjacent spaces
				for (BlockPos offset : offsets)
					frontier.add(currentPos.add(offset));
				continue;
			}

			// Place Mode
			PistonHandler stateToPlaceAt = worldIn.d_(currentPos);
			PistonHandler stateToPlaceOn = worldIn.d_(currentPos.offset(face.getOpposite()));

			// Criteria
			if (stateToPlaceOn.c()
				.e())
				continue;
			if (stateToPlaceOn.b() != state.b() && !searchAcrossMaterials)
				continue;
			if (!stateToPlaceAt.c()
				.e())
				continue;
			list.add(currentPos);

			// Search adjacent spaces
			for (BlockPos offset : offsets)
				frontier.add(currentPos.add(offset));
			continue;
		}

		return list;
	}

	public static boolean canBreak(ItemCooldownManager stack, PistonHandler state, GameMode world, BlockPos pos) {
		ComponentTier tier = getTier(Components.Body, stack);
		float blockHardness = state.h(world, pos);

		if (blockHardness == -1)
			return false;
		if (tier == ComponentTier.None)
			return blockHardness < 3;
		if (tier == ComponentTier.Brass)
			return blockHardness < 6;
		if (tier == ComponentTier.Chromatic)
			return true;

		return false;
	}

	public static int getMaxAoe(ItemCooldownManager stack) {
		ComponentTier tier = getTier(Components.Amplifier, stack);
		if (tier == ComponentTier.None)
			return 2;
		if (tier == ComponentTier.Brass)
			return 4;
		if (tier == ComponentTier.Chromatic)
			return 8;

		return 0;
	}

	@Override
	protected int getCooldownDelay(ItemCooldownManager stack) {
		return getCooldown(stack);
	}

	public static int getCooldown(ItemCooldownManager stack) {
		ComponentTier tier = getTier(Components.Accelerator, stack);
		if (tier == ComponentTier.None)
			return 10;
		if (tier == ComponentTier.Brass)
			return 6;
		if (tier == ComponentTier.Chromatic)
			return 2;

		return 20;
	}

	@Override
	protected int getZappingRange(ItemCooldownManager stack) {
		ComponentTier tier = getTier(Components.Scope, stack);
		if (tier == ComponentTier.None)
			return 15;
		if (tier == ComponentTier.Brass)
			return 30;
		if (tier == ComponentTier.Chromatic)
			return 100;

		return 0;
	}

	protected static void dropBlocks(GameMode worldIn, PlayerAbilities playerIn, ItemCooldownManager item, Direction face,
		BlockPos placed) {
		BeehiveBlockEntity tileentity = worldIn.d_(placed)
			.hasTileEntity() ? worldIn.c(placed) : null;

		if (getTier(Components.Retriever, item) == ComponentTier.None) {
			BeetrootsBlock.a(worldIn.d_(placed), worldIn, placed.offset(face), tileentity);
		}

		if (getTier(Components.Retriever, item) == ComponentTier.Brass)
			BeetrootsBlock.a(worldIn.d_(placed), worldIn, playerIn.cA(), tileentity);

		if (getTier(Components.Retriever, item) == ComponentTier.Chromatic)
			for (ItemCooldownManager stack : BeetrootsBlock.a(worldIn.d_(placed), (ServerWorld) worldIn, placed,
				tileentity))
				if (!playerIn.bm.e(stack))
					BeetrootsBlock.a(worldIn, placed, stack);
	}

	public static ComponentTier getTier(Components component, ItemCooldownManager stack) {
		if (!stack.n() || !stack.o()
			.contains(component.name()))
			stack.p()
				.putString(component.name(), ComponentTier.None.name());
		return NBTHelper.readEnum(stack.o(), component.name(), ComponentTier.class);
	}

	public static void setTier(Components component, ComponentTier tier, ItemCooldownManager stack) {
		NBTHelper.writeEnum(stack.p(), component.name(), tier);
	}

	public static enum ComponentTier {
		None(Formatting.DARK_GRAY), Brass(Formatting.GOLD), Chromatic(Formatting.LIGHT_PURPLE);

		public Formatting color;

		private ComponentTier(Formatting color) {
			this.color = color;
		}

	}

	public static enum Components {
		Body, Amplifier, Accelerator, Retriever, Scope
	}

}

package com.simibubi.create.content.schematics.block;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import apx;
import bfs;
import cdy;
import ces;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.MaterialChecklist;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CSchematics;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import cqx;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class SchematicannonTileEntity extends SmartTileEntity implements ActionResult {

	public static final int NEIGHBOUR_CHECKING = 100;
	public static final int MAX_ANCHOR_DISTANCE = 256;

	public enum State {
		STOPPED, PAUSED, RUNNING;
	}

	// Inventory
	public SchematicannonInventory inventory;

	public boolean sendUpdate;
	// Sync
	public boolean dontUpdateChecklist;
	public int neighbourCheckCooldown;

	// Printer
	private SchematicWorld blockReader;
	public BlockPos currentPos;
	public BlockPos schematicAnchor;
	public boolean schematicLoaded;
	public ItemCooldownManager missingItem;
	public boolean positionNotLoaded;
	public boolean hasCreativeCrate;
	private int printerCooldown;
	private int skipsLeft;
	private boolean blockSkipped;
	private int printingEntityIndex;

	public BlockPos target;
	public BlockPos previousTarget;
	public List<IItemHandler> attachedInventories;
	public List<LaunchedItem> flyingBlocks;
	public MaterialChecklist checklist;

	// Gui information
	public float fuelLevel;
	public float bookPrintingProgress;
	public float schematicProgress;
	public String statusMsg;
	public State state;
	public int blocksPlaced;
	public int blocksToPlace;

	// Settings
	public int replaceMode;
	public boolean skipMissing;
	public boolean replaceTileEntities;

	// Render
	public boolean firstRenderTick;

	@Override
	public Timer getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double i() {
		return super.i() * 16;
	}

	public SchematicannonTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		setLazyTickRate(30);
		attachedInventories = new LinkedList<>();
		flyingBlocks = new LinkedList<>();
		inventory = new SchematicannonInventory(this);
		statusMsg = "idle";
		state = State.STOPPED;
		printingEntityIndex = -1;
		replaceMode = 2;
		neighbourCheckCooldown = NEIGHBOUR_CHECKING;
		checklist = new MaterialChecklist();
	}

	public void findInventories() {
		hasCreativeCrate = false;
		attachedInventories.clear();
		for (Direction facing : Iterate.directions) {

			if (!d.p(e.offset(facing)))
				continue;

			if (AllBlocks.CREATIVE_CRATE.has(d.d_(e.offset(facing))))
				hasCreativeCrate = true;

			BeehiveBlockEntity tileEntity = d.c(e.offset(facing));
			if (tileEntity != null) {
				LazyOptional<IItemHandler> capability =
					tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				if (capability.isPresent()) {
					attachedInventories.add(capability.orElse(null));
				}
			}
		}
	}

	@Override
	protected void fromTag(PistonHandler blockState, CompoundTag compound, boolean clientPacket) {
		if (!clientPacket) {
			inventory.deserializeNBT(compound.getCompound("Inventory"));
			if (compound.contains("CurrentPos"))
				currentPos = NbtHelper.toBlockPos(compound.getCompound("CurrentPos"));
		}
		
		// Gui information
		statusMsg = compound.getString("Status");
		schematicProgress = compound.getFloat("Progress");
		bookPrintingProgress = compound.getFloat("PaperProgress");
		fuelLevel = compound.getFloat("Fuel");
		state = State.valueOf(compound.getString("State"));
		blocksPlaced = compound.getInt("AmountPlaced");
		blocksToPlace = compound.getInt("AmountToPlace");
		printingEntityIndex = compound.getInt("EntityProgress");
		
		missingItem = null;
		if (compound.contains("MissingItem"))
			missingItem = ItemCooldownManager.a(compound.getCompound("MissingItem"));
		
		// Settings
		CompoundTag options = compound.getCompound("Options");
		replaceMode = options.getInt("ReplaceMode");
		skipMissing = options.getBoolean("SkipMissing");
		replaceTileEntities = options.getBoolean("ReplaceTileEntities");
		
		// Printer & Flying Blocks
		if (compound.contains("Target"))
			target = NbtHelper.toBlockPos(compound.getCompound("Target"));
		if (compound.contains("FlyingBlocks"))
			readFlyingBlocks(compound);

		super.fromTag(blockState, compound, clientPacket);
	}

	protected void readFlyingBlocks(CompoundTag compound) {
		ListTag tagBlocks = compound.getList("FlyingBlocks", 10);
		if (tagBlocks.isEmpty())
			flyingBlocks.clear();

		boolean pastDead = false;

		for (int i = 0; i < tagBlocks.size(); i++) {
			CompoundTag c = tagBlocks.getCompound(i);
			LaunchedItem launched = LaunchedItem.fromNBT(c);
			BlockPos readBlockPos = launched.target;

			// Always write to Server tile
			if (d == null || !d.v) {
				flyingBlocks.add(launched);
				continue;
			}

			// Delete all Client side blocks that are now missing on the server
			while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.get(0).target.equals(readBlockPos)) {
				flyingBlocks.remove(0);
			}

			pastDead = true;

			// Add new server side blocks
			if (i >= flyingBlocks.size()) {
				flyingBlocks.add(launched);
				continue;
			}

			// Don't do anything with existing
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (!clientPacket) {
			compound.put("Inventory", inventory.serializeNBT());
			if (state == State.RUNNING) {
				compound.putBoolean("Running", true);
				if (currentPos != null)
					compound.put("CurrentPos", NbtHelper.fromBlockPos(currentPos));
			}
		}
		
		// Gui information
		compound.putFloat("Progress", schematicProgress);
		compound.putFloat("PaperProgress", bookPrintingProgress);
		compound.putFloat("Fuel", fuelLevel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());
		compound.putInt("AmountPlaced", blocksPlaced);
		compound.putInt("AmountToPlace", blocksToPlace);
		compound.putInt("EntityProgress", printingEntityIndex);
		
		if (missingItem != null)
			compound.put("MissingItem", missingItem.serializeNBT());
		
		// Settings
		CompoundTag options = new CompoundTag();
		options.putInt("ReplaceMode", replaceMode);
		options.putBoolean("SkipMissing", skipMissing);
		options.putBoolean("ReplaceTileEntities", replaceTileEntities);
		compound.put("Options", options);
		
		// Printer & Flying Blocks
		if (target != null)
			compound.put("Target", NbtHelper.fromBlockPos(target));
		ListTag tagBlocks = new ListTag();
		for (LaunchedItem b : flyingBlocks)
			tagBlocks.add(b.serializeNBT());
		compound.put("FlyingBlocks", tagBlocks);

		super.write(compound, clientPacket);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (neighbourCheckCooldown-- <= 0) {
			neighbourCheckCooldown = NEIGHBOUR_CHECKING;
			findInventories();
		}

		firstRenderTick = true;
		previousTarget = target;
		tickFlyingBlocks();

		if (d.v)
			return;

		// Update Fuel and Paper
		tickPaperPrinter();
		refillFuelIfPossible();

		// Update Printer
		skipsLeft = config().schematicannonSkips.get();
		blockSkipped = true;

		while (blockSkipped && skipsLeft-- > 0)
			tickPrinter();

		schematicProgress = 0;
		if (blocksToPlace > 0)
			schematicProgress = (float) blocksPlaced / blocksToPlace;

		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			d.a(e, p(), p(), 6);
		}
	}

	public CSchematics config() {
		return AllConfigs.SERVER.schematics;
	}

	protected void tickPrinter() {
		ItemCooldownManager blueprint = inventory.getStackInSlot(0);
		blockSkipped = false;

		// Skip if not Active
		if (state == State.STOPPED) {
			if (schematicLoaded)
				resetPrinter();
			return;
		}

		if (blueprint.a()) {
			state = State.STOPPED;
			statusMsg = "idle";
			sendUpdate = true;
			return;
		}

		if (state == State.PAUSED && !positionNotLoaded && missingItem == null && fuelLevel > getFuelUsageRate())
			return;

		// Initialize Printer
		if (!schematicLoaded) {
			initializePrinter(blueprint);
			return;
		}

		// Cooldown from last shot
		if (printerCooldown > 0) {
			printerCooldown--;
			return;
		}

		// Check Fuel
		if (fuelLevel <= 0 && !hasCreativeCrate) {
			fuelLevel = 0;
			state = State.PAUSED;
			statusMsg = "noGunpowder";
			sendUpdate = true;
			return;
		}

		// Update Target
		if (hasCreativeCrate) {
			if (missingItem != null) {
				missingItem = null;
				state = State.RUNNING;
			}
		}

		if (missingItem == null && !positionNotLoaded) {
			advanceCurrentPos();

			// End reached
			if (state == State.STOPPED)
				return;

			sendUpdate = true;
			target = schematicAnchor.add(currentPos);
		}

		boolean entityMode = printingEntityIndex >= 0;

		// Check block
		if (!v().isAreaLoaded(target, 0)) {
			positionNotLoaded = true;
			statusMsg = "targetNotLoaded";
			state = State.PAUSED;
			return;
		} else {
			if (positionNotLoaded) {
				positionNotLoaded = false;
				state = State.RUNNING;
			}
		}

		boolean shouldSkip = false;
		PistonHandler blockState = BellBlock.FACING.n();
		ItemRequirement requirement;

		if (entityMode) {
			requirement = ItemRequirement.of(blockReader.getEntities().collect(Collectors.toList())
				.get(printingEntityIndex));

		} else {
			blockState = BlockHelper.setZeroAge(blockReader.d_(target));
			requirement = ItemRequirement.of(blockState);
			shouldSkip = !shouldPlace(target, blockState);
		}

		if (shouldSkip || requirement.isInvalid()) {
			statusMsg = "searching";
			blockSkipped = true;
			return;
		}

		// Find item
		List<ItemCooldownManager> requiredItems = requirement.getRequiredItems();
		if (!requirement.isEmpty()) {
			for (ItemCooldownManager required : requiredItems) {
				if (!grabItemsFromAttachedInventories(required, requirement.getUsage(), true)) {
					if (skipMissing) {
						statusMsg = "skipping";
						blockSkipped = true;
						if (missingItem != null) {
							missingItem = null;
							state = State.RUNNING;
						}
						return;
					}

					missingItem = required;
					state = State.PAUSED;
					statusMsg = "missingBlock";
					return;
				}
			}

			for (ItemCooldownManager required : requiredItems)
				grabItemsFromAttachedInventories(required, requirement.getUsage(), false);
		}

		// Success
		state = State.RUNNING;
		if (blockState.b() != BellBlock.FACING || entityMode)
			statusMsg = "placing";
		else
			statusMsg = "clearing";

		ItemCooldownManager icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemCooldownManager.tick : requiredItems.get(0);
		if (entityMode)
			launchEntity(target, icon, blockReader.getEntities().collect(Collectors.toList())
				.get(printingEntityIndex));
		else if (AllBlocks.BELT.has(blockState)) {
			BeehiveBlockEntity te = blockReader.c(currentPos.add(schematicAnchor));
			blockState = stripBeltIfNotLast(blockState);
			if (te instanceof BeltTileEntity && AllBlocks.BELT.has(blockState))
				launchBelt(target, blockState, ((BeltTileEntity) te).beltLength);
			else
				launchBlock(target, icon, blockState, null);
		} else {
			CompoundTag data = null;
			if (AllBlockTags.SAFE_NBT.matches(blockState)) {
				BeehiveBlockEntity tile = blockReader.c(target);
				if (tile != null && !tile.t()) {
					data = tile.a(new CompoundTag());
				}
			}
			launchBlock(target, icon, blockState, data);
		}

		printerCooldown = config().schematicannonDelay.get();
		fuelLevel -= getFuelUsageRate();
		sendUpdate = true;
		missingItem = null;
	}

	public PistonHandler stripBeltIfNotLast(PistonHandler blockState) {
		// is highest belt?
		boolean isLastSegment = false;
		Direction facing = blockState.c(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = blockState.c(BeltBlock.SLOPE);
		boolean positive = facing.getDirection() == AxisDirection.POSITIVE;
		boolean start = blockState.c(BeltBlock.PART) == BeltPart.START;
		boolean end = blockState.c(BeltBlock.PART) == BeltPart.END;

		switch (slope) {
		case DOWNWARD:
			isLastSegment = start;
			break;
		case UPWARD:
			isLastSegment = end;
			break;
		case HORIZONTAL:
		case VERTICAL:
		default:
			isLastSegment = positive && end || !positive && start;
		}
		if (!isLastSegment)
			blockState = (blockState.c(BeltBlock.PART) == BeltPart.MIDDLE) ? BellBlock.FACING.n()
				: AllBlocks.SHAFT.getDefaultState()
					.a(AbstractShaftBlock.AXIS, facing.rotateYClockwise()
						.getAxis());
		return blockState;
	}

	public double getFuelUsageRate() {
		return hasCreativeCrate ? 0 : config().schematicannonFuelUsage.get() / 100f;
	}

	protected void initializePrinter(ItemCooldownManager blueprint) {
		if (!blueprint.n()) {
			state = State.STOPPED;
			statusMsg = "schematicInvalid";
			sendUpdate = true;
			return;
		}

		if (!blueprint.o()
			.getBoolean("Deployed")) {
			state = State.STOPPED;
			statusMsg = "schematicNotPlaced";
			sendUpdate = true;
			return;
		}

		// Load blocks into reader
		StructureProcessor activeTemplate = SchematicItem.loadSchematic(blueprint);
		BlockPos anchor = NbtHelper.toBlockPos(blueprint.o()
			.getCompound("Anchor"));

		if (activeTemplate.a()
			.equals(BlockPos.ORIGIN)) {
			state = State.STOPPED;
			statusMsg = "schematicExpired";
			inventory.setStackInSlot(0, ItemCooldownManager.tick);
			inventory.setStackInSlot(1, new ItemCooldownManager(AllItems.EMPTY_SCHEMATIC.get()));
			return;
		}

		if (!anchor.isWithinDistance(o(), MAX_ANCHOR_DISTANCE)) {
			state = State.STOPPED;
			statusMsg = "targetOutsideRange";
			return;
		}

		schematicAnchor = anchor;
		blockReader = new SchematicWorld(schematicAnchor, d);
		RuleTest settings = SchematicItem.getSettings(blueprint);
		activeTemplate.a(blockReader, schematicAnchor, settings, blockReader.getRandom());
		schematicLoaded = true;
		state = State.PAUSED;
		statusMsg = "ready";
		printingEntityIndex = -1;
		updateChecklist();
		sendUpdate = true;
		blocksToPlace += blocksPlaced;
		cqx bounds = blockReader.getBounds();
		currentPos = currentPos != null ? currentPos.west() : new BlockPos(bounds.a - 1, bounds.b, bounds.c);
	}

	protected ItemCooldownManager getItemForBlock(PistonHandler blockState) {
		HoeItem item = BannerItem.e.getOrDefault(blockState.b(), AliasedBlockItem.a);
		return item == AliasedBlockItem.a ? ItemCooldownManager.tick : new ItemCooldownManager(item);
	}

	protected boolean grabItemsFromAttachedInventories(ItemCooldownManager required, ItemUseType usage, boolean simulate) {
		if (hasCreativeCrate)
			return true;

		// Find and apply damage
		if (usage == ItemUseType.DAMAGE) {
			for (IItemHandler iItemHandler : attachedInventories) {
				for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
					ItemCooldownManager extractItem = iItemHandler.extractItem(slot, 1, true);
					if (!ItemRequirement.validate(required, extractItem))
						continue;
					if (!extractItem.e())
						continue;

					if (!simulate) {
						ItemCooldownManager stack = iItemHandler.extractItem(slot, 1, false);
						stack.b(stack.g() + 1);
						if (stack.g() <= stack.h()) {
							if (iItemHandler.getStackInSlot(slot)
								.a())
								iItemHandler.insertItem(slot, stack, false);
							else
								ItemHandlerHelper.insertItem(iItemHandler, stack, false);
						}
					}

					return true;
				}
			}
		}

		// Find and remove
		boolean success = false;
		if (usage == ItemUseType.CONSUME) {
			int amountFound = 0;
			for (IItemHandler iItemHandler : attachedInventories) {

				amountFound += ItemHelper
					.extract(iItemHandler, s -> ItemRequirement.validate(required, s), ExtractionCountMode.UPTO,
						required.E(), true)
					.E();

				if (amountFound < required.E())
					continue;

				success = true;
				break;
			}
		}

		if (!simulate && success) {
			int amountFound = 0;
			for (IItemHandler iItemHandler : attachedInventories) {
				amountFound += ItemHelper
					.extract(iItemHandler, s -> ItemRequirement.validate(required, s), ExtractionCountMode.UPTO,
						required.E(), false)
					.E();
				if (amountFound < required.E())
					continue;
				break;
			}
		}

		return success;
	}

	protected void advanceCurrentPos() {
		List<apx> entities = blockReader.getEntities().collect(Collectors.toList());
		if (printingEntityIndex != -1) {
			printingEntityIndex++;

			// End of entities reached
			if (printingEntityIndex >= entities.size()) {
				finishedPrinting();
				return;
			}

			currentPos = entities.get(printingEntityIndex)
				.cA()
				.subtract(schematicAnchor);
			return;
		}

		cqx bounds = blockReader.getBounds();
		currentPos = currentPos.offset(Direction.EAST);
		BlockPos posInBounds = currentPos.add(-bounds.a, -bounds.b, -bounds.c);

		if (posInBounds.getX() > bounds.d())
			currentPos = new BlockPos(bounds.a, currentPos.getY(), currentPos.getZ() + 1).west();
		if (posInBounds.getZ() > bounds.f())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, bounds.c).west();

		// End of blocks reached
		if (currentPos.getY() > bounds.e()) {
			printingEntityIndex = 0;
			if (entities.isEmpty()) {
				finishedPrinting();
				return;
			}
			currentPos = entities.get(0)
				.cA()
				.subtract(schematicAnchor);
		}
	}

	public void finishedPrinting() {
		inventory.setStackInSlot(0, ItemCooldownManager.tick);
		inventory.setStackInSlot(1, new ItemCooldownManager(AllItems.EMPTY_SCHEMATIC.get(), inventory.getStackInSlot(1)
			.E() + 1));
		state = State.STOPPED;
		statusMsg = "finished";
		resetPrinter();
		target = o().add(1, 0, 0);
		d.a(null, e.getX(), e.getY(), e.getZ(), AllSoundEvents.SCHEMATICANNON_FINISH.get(),
			SoundEvent.e, 1, .7f);
		sendUpdate = true;
	}

	protected void resetPrinter() {
		schematicLoaded = false;
		schematicAnchor = null;
		currentPos = null;
		blockReader = null;
		missingItem = null;
		sendUpdate = true;
		printingEntityIndex = -1;
		schematicProgress = 0;
		blocksPlaced = 0;
		blocksToPlace = 0;
	}

	protected boolean shouldPlace(BlockPos pos, PistonHandler state) {
		if (d == null)
			return false;
		PistonHandler toReplace = d.d_(pos);
		boolean placingAir = state.b().isAir(state, d, pos);

		PistonHandler toReplaceOther = null;
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.aE) && BlockHelper.hasBlockStateProperty(state, BambooLeaves.O) && state.c(BambooLeaves.aE) == ces.b)
			toReplaceOther = d.d_(pos.offset(state.c(BambooLeaves.O)));
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.aa)
			&& state.c(BambooLeaves.aa) == ComparatorMode.LOWER)
			toReplaceOther = d.d_(pos.up());

		if (!d.p(pos))
			return false;
		if (!d.f()
			.a(pos))
			return false;
		if (toReplace == state)
			return false;
		if (toReplace.h(d, pos) == -1 || (toReplaceOther != null && toReplaceOther.h(d, pos) == -1))
			return false;
		if (pos.isWithinDistance(o(), 2f))
			return false;
		if (!replaceTileEntities && (toReplace.hasTileEntity() || (toReplaceOther != null && toReplaceOther.hasTileEntity())))
			return false;

		if (shouldIgnoreBlockState(state))
			return false;

		if (replaceMode == 3)
			return true;
		if (replaceMode == 2 && !placingAir)
			return true;
		if (replaceMode == 1
			&& (state.g(blockReader, pos.subtract(schematicAnchor)) || (!toReplace.g(d, pos) && (toReplaceOther == null || !toReplaceOther.g(d, pos))))
			&& !placingAir)
			return true;
		if (replaceMode == 0 && !toReplace.g(d, pos) && (toReplaceOther == null || !toReplaceOther.g(d, pos)) && !placingAir)
			return true;

		return false;
	}

	protected boolean shouldIgnoreBlockState(PistonHandler state) {
		// Block doesnt have a mapping (Water, lava, etc)
		if (state.b() == BellBlock.iN)
			return true;
		
		ItemRequirement requirement = ItemRequirement.of(state);
		if (requirement.isEmpty())
			return false;
		if (requirement.isInvalid())
			return false;

		// Block doesnt need to be placed twice (Doors, beds, double plants)
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.aa)
			&& state.c(BambooLeaves.aa) == ComparatorMode.UPPER)
			return true;
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.aE) && state.c(BambooLeaves.aE) == ces.a)
			return true;
		if (state.b() instanceof cdy)
			return true;

		return false;
	}

	protected void tickFlyingBlocks() {
		List<LaunchedItem> toRemove = new LinkedList<>();
		for (LaunchedItem b : flyingBlocks)
			if (b.update(d))
				toRemove.add(b);
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (hasCreativeCrate)
			return;
		if (1 - fuelLevel + 1 / 128f < getFuelAddedByGunPowder())
			return;
		if (inventory.getStackInSlot(4)
			.a())
			return;

		inventory.getStackInSlot(4)
			.g(1);
		fuelLevel += getFuelAddedByGunPowder();
		sendUpdate = true;
	}

	public double getFuelAddedByGunPowder() {
		return config().schematicannonGunpowderWorth.get() / 100f;
	}

	protected void tickPaperPrinter() {
		int BookInput = 2;
		int BookOutput = 3;

		ItemCooldownManager blueprint = inventory.getStackInSlot(0);
		ItemCooldownManager paper = inventory.extractItem(BookInput, 1, true);
		boolean outputFull = inventory.getStackInSlot(BookOutput)
			.E() == inventory.getSlotLimit(BookOutput);

		if (paper.a() || outputFull) {
			if (bookPrintingProgress != 0)
				sendUpdate = true;
			bookPrintingProgress = 0;
			dontUpdateChecklist = false;
			return;
		}

		if (!schematicLoaded) {
			if (!blueprint.a())
				initializePrinter(blueprint);
			return;
		}

		if (bookPrintingProgress >= 1) {
			bookPrintingProgress = 0;

			if (!dontUpdateChecklist)
				updateChecklist();

			dontUpdateChecklist = true;
			inventory.extractItem(BookInput, 1, false);
			ItemCooldownManager stack = checklist.createItem();
			stack.e(inventory.getStackInSlot(BookOutput)
				.E() + 1);
			inventory.setStackInSlot(BookOutput, stack);
			sendUpdate = true;
			return;
		}

		bookPrintingProgress += 0.05f;
		sendUpdate = true;
	}

	protected void launchBelt(BlockPos target, PistonHandler state, int length) {
		blocksPlaced++;
		ItemCooldownManager connector = AllItems.BELT_CONNECTOR.asStack();
		flyingBlocks.add(new LaunchedItem.ForBelt(this.o(), target, connector, state, length));
		playFiringSound();
	}

	protected void launchBlock(BlockPos target, ItemCooldownManager stack, PistonHandler state, @Nullable CompoundTag data) {
		if (state.b().isAir(state, d, target))
			blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForBlockState(this.o(), target, stack, state, data));
		playFiringSound();
	}

	protected void launchEntity(BlockPos target, ItemCooldownManager stack, apx entity) {
		blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForEntity(this.o(), target, stack, entity));
		playFiringSound();
	}

	public void playFiringSound() {
		d.a(null, e.getX(), e.getY(), e.getZ(), AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.get(),
			SoundEvent.e, .1f, 1.1f);
	}

	public void sendToContainer(PacketByteBuf buffer) {
		buffer.writeBlockPos(o());
		buffer.writeCompoundTag(b());
	}

	@Override
	public FoodComponent createMenu(int id, bfs inv, PlayerAbilities player) {
		return new SchematicannonContainer(id, inv, this);
	}

	@Override
	public Text d() {
		return new LiteralText(u().getRegistryName()
			.toString());
	}

	public void updateChecklist() {
		checklist.required.clear();
		checklist.damageRequired.clear();
		checklist.blocksNotLoaded = false;

		if (schematicLoaded) {
			blocksToPlace = blocksPlaced;
			for (BlockPos pos : blockReader.getAllPositions()) {
				PistonHandler required = blockReader.d_(pos.add(schematicAnchor));

				if (!v().isAreaLoaded(pos.add(schematicAnchor), 0)) {
					checklist.warnBlockNotLoaded();
					continue;
				}
				if (!shouldPlace(pos.add(schematicAnchor), required))
					continue;
				ItemRequirement requirement = ItemRequirement.of(required);
				if (requirement.isEmpty())
					continue;
				if (requirement.isInvalid())
					continue;
				checklist.require(requirement);
				blocksToPlace++;
			}
			blockReader.getEntities().forEach(entity -> {
				ItemRequirement requirement = ItemRequirement.of(entity);
				if (requirement.isEmpty())
					return;
				if (requirement.isInvalid())
					return;
				checklist.require(requirement);
			});

		}
		checklist.gathered.clear();
		for (IItemHandler inventory : attachedInventories) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemCooldownManager stackInSlot = inventory.getStackInSlot(slot);
				if (inventory.extractItem(slot, 1, true)
					.a())
					continue;
				checklist.collect(stackInSlot);
			}
		}
		sendUpdate = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void lazyTick() {
		super.lazyTick();
		findInventories();
	}

}

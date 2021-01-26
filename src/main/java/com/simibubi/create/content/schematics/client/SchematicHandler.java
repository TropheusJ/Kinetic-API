package com.simibubi.create.content.schematics.client;

import java.util.List;
import java.util.Vector;
import bfs;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.client.tools.Tools;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.foundation.gui.ToolSelectionScreen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import dcg;
import net.minecraft.block.LoomBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;

public class SchematicHandler {

	private String displayedSchematic;
	private SchematicTransformation transformation;
	private Timer bounds;
	private boolean deployed;
	private boolean active;
	private Tools currentTool;

	private static final int SYNC_DELAY = 10;
	private int syncCooldown;
	private int activeHotbarSlot;
	private ItemCooldownManager activeSchematicItem;
	private AABBOutline outline;

	private Vector<SchematicRenderer> renderers;
	private SchematicHotbarSlotOverlay overlay;
	private ToolSelectionScreen selectionScreen;

	public SchematicHandler() {
		renderers = new Vector<>(3);
		for (int i = 0; i < renderers.capacity(); i++)
			renderers.add(new SchematicRenderer());

		overlay = new SchematicHotbarSlotOverlay();
		currentTool = Tools.Deploy;
		selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
		transformation = new SchematicTransformation();
	}

	public void tick() {
		FishingParticle player = KeyBinding.B().s;

		if (activeSchematicItem != null && transformation != null)
			transformation.tick();

		ItemCooldownManager stack = findBlueprintInHand(player);
		if (stack == null) {
			active = false;
			syncCooldown = 0;
			if (activeSchematicItem != null && itemLost(player)) {
				activeHotbarSlot = 0;
				activeSchematicItem = null;
				renderers.forEach(r -> r.setActive(false));
			}
			return;
		}

		if (!active || !stack.o()
			.getString("File")
			.equals(displayedSchematic))
			init(player, stack);
		if (!active)
			return;

		renderers.forEach(SchematicRenderer::tick);
		if (syncCooldown > 0)
			syncCooldown--;
		if (syncCooldown == 1)
			sync();

		selectionScreen.update();
		currentTool.getTool()
			.updateSelection();
	}

	private void init(FishingParticle player, ItemCooldownManager stack) {
		loadSettings(stack);
		displayedSchematic = stack.o()
			.getString("File");
		active = true;
		if (deployed) {
			setupRenderer();
			Tools toolBefore = currentTool;
			selectionScreen = new ToolSelectionScreen(Tools.getTools(player.b_()), this::equip);
			if (toolBefore != null) {
				selectionScreen.setSelectedElement(toolBefore);
				equip(toolBefore);
			}
		} else
			selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
	}

	private void setupRenderer() {
		StructureProcessor schematic = SchematicItem.loadSchematic(activeSchematicItem);
		BlockPos size = schematic.a();
		if (size.equals(BlockPos.ORIGIN))
			return;

		GameMode clientWorld = KeyBinding.B().r;
		SchematicWorld w = new SchematicWorld(clientWorld);
		SchematicWorld wMirroredFB = new SchematicWorld(clientWorld);
		SchematicWorld wMirroredLR = new SchematicWorld(clientWorld);
		RuleTest placementSettings = new RuleTest();

		schematic.a(w, BlockPos.ORIGIN, placementSettings, w.getRandom());
		placementSettings.a(LoomBlock.c);
		schematic.a(wMirroredFB, BlockPos.ORIGIN.east(size.getX() - 1), placementSettings, wMirroredFB.getRandom());
		placementSettings.a(LoomBlock.b);
		schematic.a(wMirroredLR, BlockPos.ORIGIN.south(size.getZ() - 1), placementSettings, wMirroredFB.getRandom());

		renderers.get(0)
			.display(w);
		renderers.get(1)
			.display(wMirroredFB);
		renderers.get(2)
			.display(wMirroredLR);
	}

	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		boolean present = activeSchematicItem != null;
		if (!active && !present)
			return;

		if (active) {
			ms.a();
			currentTool.getTool()
				.renderTool(ms, buffer);
			ms.b();
		}

		ms.a();
		transformation.applyGLTransformations(ms);

		if (!renderers.isEmpty()) {
			float pt = KeyBinding.B()
				.ai();
			boolean lr = transformation.getScaleLR()
				.get(pt) < 0;
			boolean fb = transformation.getScaleFB()
				.get(pt) < 0;
			if (lr && !fb)
				renderers.get(2)
					.render(ms, buffer);
			else if (fb && !lr)
				renderers.get(1)
					.render(ms, buffer);
			else
				renderers.get(0)
					.render(ms, buffer);
		}
		
		if (active)
			currentTool.getTool()
			.renderOnSchematic(ms, buffer);
		
		ms.b();

	}

	public void renderOverlay(BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay, float partialTicks) {
		if (!active)
			return;
		if (activeSchematicItem != null)
			this.overlay.renderOn(ms, activeHotbarSlot);

		currentTool.getTool()
			.renderOverlay(ms, buffer);
		selectionScreen.renderPassive(ms, partialTicks);
	}

	public void onMouseInput(int button, boolean pressed) {
		if (!active)
			return;
		if (!pressed || button != 1)
			return;
		KeyBinding mc = KeyBinding.B();
		if (mc.s.bt())
			return;
		if (mc.v instanceof dcg) {
			dcg blockRayTraceResult = (dcg) mc.v;
			if (AllBlocks.SCHEMATICANNON.has(mc.r.d_(blockRayTraceResult.a())))
				return;
		}
		currentTool.getTool()
		.handleRightClick();
	}

	public void onKeyInput(int key, boolean pressed) {
		if (!active)
			return;
		if (key != AllKeys.TOOL_MENU.getBoundCode())
			return;

		if (pressed && !selectionScreen.focused)
			selectionScreen.focused = true;
		if (!pressed && selectionScreen.focused) {
			selectionScreen.focused = false;
			selectionScreen.au_();
		}
	}

	public boolean mouseScrolled(double delta) {
		if (!active)
			return false;

		if (selectionScreen.focused) {
			selectionScreen.cycle((int) delta);
			return true;
		}
		if (AllKeys.ctrlDown())
			return currentTool.getTool()
				.handleMouseWheel(delta);
		return false;
	}

	private ItemCooldownManager findBlueprintInHand(PlayerAbilities player) {
		ItemCooldownManager stack = player.dC();
		if (!AllItems.SCHEMATIC.isIn(stack))
			return null;
		if (!stack.n())
			return null;

		activeSchematicItem = stack;
		activeHotbarSlot = player.bm.d;
		return stack;
	}

	private boolean itemLost(PlayerAbilities player) {
		for (int i = 0; i < bfs.g(); i++) {
			if (!player.bm.a(i)
				.a(activeSchematicItem))
				continue;
			if (!ItemCooldownManager.a(player.bm.a(i), activeSchematicItem))
				continue;
			return false;
		}
		return true;
	}

	public void markDirty() {
		syncCooldown = SYNC_DELAY;
	}

	public void sync() {
		if (activeSchematicItem == null)
			return;
		AllPackets.channel.sendToServer(new SchematicSyncPacket(activeHotbarSlot, transformation.toSettings(), transformation.getAnchor(), deployed));
	}

	public void equip(Tools tool) {
		this.currentTool = tool;
		currentTool.getTool()
			.init();
	}

	public void loadSettings(ItemCooldownManager blueprint) {
		CompoundTag tag = blueprint.o();
		BlockPos anchor = BlockPos.ORIGIN;
		RuleTest settings = SchematicItem.getSettings(blueprint);
		transformation = new SchematicTransformation();

		deployed = tag.getBoolean("Deployed");
		if (deployed)
			anchor = NbtHelper.toBlockPos(tag.getCompound("Anchor"));
		BlockPos size = NbtHelper.toBlockPos(tag.getCompound("Bounds"));

		bounds = new Timer(BlockPos.ORIGIN, size);
		outline = new AABBOutline(bounds);
		outline.getParams()
			.colored(0x6886c5)
			.lineWidth(1 / 16f);
		transformation.init(anchor, settings, bounds);
	}

	public void deploy() {
		if (!deployed) {
			List<Tools> tools = Tools.getTools(KeyBinding.B().s.b_());
			selectionScreen = new ToolSelectionScreen(tools, this::equip);
		}
		deployed = true;
		setupRenderer();
	}

	public String getCurrentSchematicName() {
		return displayedSchematic != null ? displayedSchematic : "-";
	}

	public void printInstantly() {
		AllPackets.channel.sendToServer(new SchematicPlacePacket(activeSchematicItem.i()));
		CompoundTag nbt = activeSchematicItem.o();
		nbt.putBoolean("Deployed", false);
		activeSchematicItem.c(nbt);
		renderers.forEach(r -> r.setActive(false));
		active = false;
		markDirty();
	}

	public boolean isActive() {
		return active;
	}

	public Timer getBounds() {
		return bounds;
	}

	public SchematicTransformation getTransformation() {
		return transformation;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public ItemCooldownManager getActiveSchematicItem() {
		return activeSchematicItem;
	}

	public AABBOutline getOutline() {
		return outline;
	}

}

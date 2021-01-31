package com.simibubi.kinetic_api.content.logistics.block.inventories;

import static com.simibubi.kinetic_api.foundation.gui.AllGuiTextures.ADJUSTABLE_CRATE;
import static com.simibubi.kinetic_api.foundation.gui.AllGuiTextures.ADJUSTABLE_DOUBLE_CRATE;
import static com.simibubi.kinetic_api.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import bfs;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.logistics.packet.ConfigureFlexcratePacket;
import com.simibubi.kinetic_api.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.kinetic_api.foundation.gui.AllGuiTextures;
import com.simibubi.kinetic_api.foundation.gui.GuiGameElement;
import com.simibubi.kinetic_api.foundation.gui.widgets.Label;
import com.simibubi.kinetic_api.foundation.gui.widgets.ScrollInput;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import com.simibubi.kinetic_api.foundation.utility.Lang;

public class AdjustableCrateScreen extends AbstractSimiContainerScreen<AdjustableCrateContainer> {

	private AdjustableCrateTileEntity te;
	private Label allowedItemsLabel;
	private ScrollInput allowedItems;
	private int lastModification;

	private List<ItemModels> extraAreas;

	private final ItemCooldownManager renderedItem = AllBlocks.ADJUSTABLE_CRATE.asStack();
	private final Text title = Lang.translate("gui.adjustable_crate.title");
	private final Text storageSpace = Lang.translate("gui.adjustable_crate.storageSpace");

	public AdjustableCrateScreen(AdjustableCrateContainer container, bfs inv, Text title) {
		super(container, inv, title);
		te = container.te;
		lastModification = -1;
	}

	@Override
	protected void b() {
		setWindowSize(PLAYER_INVENTORY.width + 100, ADJUSTABLE_CRATE.height + PLAYER_INVENTORY.height + 20);
		super.b();
		widgets.clear();

		allowedItemsLabel = new Label(w + 100 + 69, x + 108, LiteralText.EMPTY).colored(0xfefefe)
			.withShadow();
		allowedItems = new ScrollInput(w + 100 + 65, x + 104, 41, 14).titled(storageSpace.copy())
			.withRange(1, (t.doubleCrate ? 2049 : 1025))
			.writingTo(allowedItemsLabel)
			.withShiftStep(64)
			.setState(te.allowedAmount)
			.calling(s -> lastModification = 0);
		allowedItems.onChanged();
		widgets.add(allowedItemsLabel);
		widgets.add(allowedItems);

		extraAreas = new ArrayList<>();
		extraAreas.add(new ItemModels(w + ADJUSTABLE_CRATE.width + 110, x + 46, 71, 70));
	}

	@Override
	protected void renderWindow(BufferVertexConsumer matrixStack, int mouseX, int mouseY, float partialTicks) {
		int crateLeft = w + 100;
		int crateTop = x;
		int invLeft = w + 50;
		int invTop = crateTop + ADJUSTABLE_CRATE.height + 10;
		int fontColor = 0x4B3A22;

		if (t.doubleCrate) {
			crateLeft -= 72;
			ADJUSTABLE_DOUBLE_CRATE.draw(matrixStack, this, crateLeft, crateTop);
		} else
			ADJUSTABLE_CRATE.draw(matrixStack,this, crateLeft, crateTop);

		o.a(matrixStack, d, crateLeft - 3 + (ADJUSTABLE_CRATE.width - o.a(d)) / 2,
			crateTop + 3, 0xfefefe);
		String itemCount = "" + te.itemCount;
		o.b(matrixStack, itemCount, w + 100 + 53 - o.b(itemCount), crateTop + 107, fontColor);

		PLAYER_INVENTORY.draw(matrixStack, this, invLeft, invTop);
		o.b(matrixStack, u.d(), invLeft + 7, invTop + 6, 0x666666);

		for (int slot = 0; slot < (t.doubleCrate ? 32 : 16); slot++) {
			if (allowedItems.getState() > slot * 64)
				continue;
			int slotsPerRow = (t.doubleCrate ? 8 : 4);
			int x = crateLeft + 22 + (slot % slotsPerRow) * 18;
			int y = crateTop + 19 + (slot / slotsPerRow) * 18;
			AllGuiTextures.ADJUSTABLE_CRATE_LOCKED_SLOT.draw(matrixStack, this, x, y);
		}

		GuiGameElement.of(renderedItem)
				.at(w + ADJUSTABLE_CRATE.width + 110, x + 120, -150)
				.scale(5)
				.render(matrixStack);
	}

	@Override
	public void e() {
		AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.o(), allowedItems.getState()));
	}

	@Override
	public void d() {
		super.tick();

		if (!AllBlocks.ADJUSTABLE_CRATE.has(i.r.d_(te.o())))
			i.a(null);

		if (lastModification >= 0)
			lastModification++;

		if (lastModification >= 15) {
			lastModification = -1;
			AllPackets.channel.sendToServer(new ConfigureFlexcratePacket(te.o(), allowedItems.getState()));
		}

		if (t.doubleCrate != te.isDoubleCrate())
			t.playerInventory.e.m();
	}

	@Override
	public List<ItemModels> getExtraAreas() {
		return extraAreas;
	}
}

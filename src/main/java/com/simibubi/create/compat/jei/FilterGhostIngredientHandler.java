package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;

import com.simibubi.create.content.logistics.item.filter.AbstractFilterContainer;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket;
import com.simibubi.create.foundation.networking.AllPackets;

import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ShulkerBoxScreenHandler;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterGhostIngredientHandler<T extends AbstractFilterContainer> implements IGhostIngredientHandler<AbstractFilterScreen<T>> {

	@Override
	public <I> List<Target<I>> getTargets(AbstractFilterScreen<T> gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		boolean isAttributeFilter = gui instanceof AttributeFilterScreen;

		if (ingredient instanceof ItemCooldownManager) {
			for (int i = 36; i < gui.i().hunger.size(); i++) {
				targets.add(new FilterGhostTarget<>(gui, i - 36, isAttributeFilter));

				//Only accept items in 1st slot. 2nd is used for functionality, don't wanna override that one
				if (isAttributeFilter) break;
			}
		}

		return targets;
	}

	@Override
	public void onComplete() {}

	@Override
	public boolean shouldHighlightTargets() {
		//TODO change to false and highlight the slots ourself in some better way
		return true;
	}

	private static class FilterGhostTarget<I, T extends AbstractFilterContainer> implements Target<I> {

		private final ItemModels area;
		private final AbstractFilterScreen<T> gui;
		private final int slotIndex;
		private final boolean isAttributeFilter;


		public FilterGhostTarget(AbstractFilterScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
			this.gui = gui;
			this.slotIndex = slotIndex;
			this.isAttributeFilter = isAttributeFilter;
			ShulkerBoxScreenHandler slot = gui.i().hunger.get(slotIndex + 36);
			this.area = new ItemModels(
					gui.getGuiLeft() + slot.e,
					gui.getGuiTop() + slot.f,
					16,
					16);
		}

		@Override
		public ItemModels getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemCooldownManager stack = ((ItemCooldownManager) ingredient).i();
			LogManager.getLogger().info(stack);
			stack.e(1);
			gui.i().filterInventory.setStackInSlot(slotIndex, stack);

			if (isAttributeFilter) return;

			//sync new filter contents with server
			CompoundTag data = new CompoundTag();
			data.putInt("Slot", slotIndex);
			data.put("Item", stack.serializeNBT());
			AllPackets.channel.sendToServer(new FilterScreenPacket(FilterScreenPacket.Option.UPDATE_FILTER_ITEM, data));
		}
	}
}

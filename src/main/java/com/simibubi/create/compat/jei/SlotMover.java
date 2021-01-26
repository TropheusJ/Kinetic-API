package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.FoodComponent;

/**
 * Allows a {@link AbstractSimiContainerScreen} to specify an area in getExtraArea() that will be avoided by JEI
 *
 * Name is taken from CoFHCore's 1.12 implementation.
 */
public class SlotMover<T extends FoodComponent> implements IGuiContainerHandler<AbstractSimiContainerScreen<T>> {

    @Override
    public List<ItemModels> getGuiExtraAreas(AbstractSimiContainerScreen<T> containerScreen) {
        return containerScreen.getExtraAreas();
    }
}

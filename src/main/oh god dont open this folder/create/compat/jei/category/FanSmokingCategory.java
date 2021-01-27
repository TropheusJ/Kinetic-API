package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.block.BellBlock;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.recipe.SpecialRecipeSerializer;

public class FanSmokingCategory extends ProcessingViaFanCategory<SpecialRecipeSerializer> {

	public FanSmokingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), AliasedBlockItem.nz));
	}

	@Override
	public Class<? extends SpecialRecipeSerializer> getRecipeClass() {
		return SpecialRecipeSerializer.class;
	}

	@Override
	public void renderAttachedBlock(BufferVertexConsumer matrixStack) {

		GuiGameElement.of(BellBlock.bN.n())
				.scale(24)
				.atLocal(0, 0, 2)
				.render(matrixStack);

	}
}
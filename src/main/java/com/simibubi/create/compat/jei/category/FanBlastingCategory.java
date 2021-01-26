package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.GuiGameElement;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.AutomaticItemPlacementContext;

public class FanBlastingCategory extends ProcessingViaFanCategory<AutomaticItemPlacementContext> {

	public FanBlastingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), AliasedBlockItem.lM));
	}

	@Override
	public Class<? extends AutomaticItemPlacementContext> getRecipeClass() {
		return AutomaticItemPlacementContext.class;
	}

	@Override
	public void renderAttachedBlock(BufferVertexConsumer matrixStack) {
		matrixStack.a();

		GuiGameElement.of(FlowableFluid.field_15901)
			.scale(24)
			.atLocal(0, 0, 2)
			.render(matrixStack);

		matrixStack.b();
	}

}

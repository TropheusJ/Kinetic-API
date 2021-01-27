package com.simibubi.create.content.curiosities.zapper.blockzapper;

import javax.annotation.Nullable;
import net.minecraft.client.input.Input;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.utility.Lang;
import elg;

public class BlockzapperModel extends CustomRenderedItemModel {

	public BlockzapperModel(elg template) {
		super(template, "handheld_blockzapper");
		addPartials("core", "core_glow", "body", "amplifier_core", "amplifier_core_glow", "accelerator", "gold_body",
			"gold_scope", "gold_amplifier", "gold_retriever", "gold_accelerator", "chorus_body", "chorus_scope",
			"chorus_amplifier", "chorus_retriever", "chorus_accelerator");
	}

	@Override
	public Input createRenderer() {
		return new BlockzapperItemRenderer();
	}

	@Nullable
	elg getComponentPartial(BlockzapperItem.ComponentTier tier, BlockzapperItem.Components component) {
		String prefix = tier == ComponentTier.Chromatic ? "chorus_" : tier == ComponentTier.Brass ? "gold_" : "";
		return getPartial(prefix + Lang.asId(component.name()));
	}

}

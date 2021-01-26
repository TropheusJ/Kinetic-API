package com.simibubi.create.content;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;

public enum AllSections {

	/** Create's kinetic mechanisms */
	KINETICS(Palette.Red),

	/** Item transport and other Utility */
	LOGISTICS(Palette.Yellow),

	/** Tools for strucuture movement and replication */
	SCHEMATICS(Palette.Blue),

	/** Decorative blocks */
	PALETTES(Palette.Green),

	/** Base materials, ingredients and tools */
	MATERIALS(Palette.Green),
	
	/** Helpful gadgets and other shenanigans */
	CURIOSITIES(Palette.Purple),

	/** Fallback section */
	UNASSIGNED(Palette.Gray)

	;

	private Palette tooltipPalette;

	private AllSections(Palette tooltipPalette) {
		this.tooltipPalette = tooltipPalette;
	}

	public Palette getTooltipPalette() {
		return tooltipPalette;
	}

	public static AllSections of(ItemCooldownManager stack) {
		HoeItem item = stack.b();
		if (item instanceof BannerItem)
			return ofBlock(((BannerItem) item).e());
		return ofItem(item);
	}

	static AllSections ofItem(HoeItem item) {
		return Create.registrate()
			.getSection(item);
	}

	static AllSections ofBlock(BeetrootsBlock block) {
		return Create.registrate()
			.getSection(block);
	}

}

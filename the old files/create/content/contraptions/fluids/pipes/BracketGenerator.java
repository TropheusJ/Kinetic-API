package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import com.simibubi.kinetic_api.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BannerItem;
import net.minecraftforge.client.model.generators.ModelFile;

public class BracketGenerator extends DirectionalAxisBlockStateGen {

	private String material;

	public BracketGenerator(String material) {
		this.material = material;
	}

	@Override
	public <T extends BeetrootsBlock> String getModelPrefix(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return "";
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		String type = state.c(BracketBlock.TYPE)
			.a();
		boolean vertical = state.c(BracketBlock.SHAPE)
			.getAxis()
			.isVertical();

		String path = "block/bracket/" + type + "/" + (vertical ? "ground" : "wall");

		return prov.models()
			.withExistingParent(path + "_" + material, prov.modLoc(path))
			.texture("bracket", prov.modLoc("block/bracket_" + material))
			.texture("plate", prov.modLoc("block/bracket_plate_" + material));
	}

	public static <I extends BannerItem, P> NonNullFunction<ItemBuilder<I, P>, P> itemModel(String material) {
		return b -> b.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/bracket/item"))
			.texture("bracket", p.modLoc("block/bracket_" + material))
			.texture("plate", p.modLoc("block/bracket_plate_" + material)))
			.build();
	}

}

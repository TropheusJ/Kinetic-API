package com.simibubi.kinetic_api.content.logistics.block.funnel;

import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltFunnelGenerator extends SpecialBlockStateGen {

	private String type;
	private Identifier materialBlockTexture;

	public BeltFunnelGenerator(String type, Identifier materialBlockTexture) {
		this.type = type;
		this.materialBlockTexture = materialBlockTexture;
	}

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(BeltFunnelBlock.aq)) + 180;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		boolean powered = state.d(BambooLeaves.w).orElse(false);
		String shapeName = state.c(BeltFunnelBlock.SHAPE)
			.a();
		
		String poweredSuffix = powered ? "_powered" : "";
		String name = ctx.getName() + "_" + poweredSuffix;
		
		return prov.models()
			.withExistingParent(name + "_" + shapeName, prov.modLoc("block/belt_funnel/block_" + shapeName))
			.texture("particle", materialBlockTexture)
			.texture("2", prov.modLoc("block/" + type + "_funnel_neutral"))
			.texture("2_1", prov.modLoc("block/" + type + "_funnel_push"))
			.texture("2_2", prov.modLoc("block/" + type + "_funnel_pull"))
			.texture("3", prov.modLoc("block/" + type + "_funnel_back"))
			.texture("5", prov.modLoc("block/" + type + "_funnel_tall" + poweredSuffix))
			.texture("6", prov.modLoc("block/" + type + "_funnel" + poweredSuffix))
			.texture("7", prov.modLoc("block/" + type + "_funnel_plating"));
	}

}

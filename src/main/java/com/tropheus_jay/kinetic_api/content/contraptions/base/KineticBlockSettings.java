package com.tropheus_jay.kinetic_api.content.contraptions.base;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;

public class KineticBlockSettings extends FabricBlockSettings {

    protected KineticBlockSettings(AbstractBlock.Settings settings) {
        super(settings);
    }

    /*public static final KineticBlock SHAFT = KineticBlock("shaft", ShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(StressConfigDefaults.setNoImpact())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
            .simpleItem()
            .register();
            */
}

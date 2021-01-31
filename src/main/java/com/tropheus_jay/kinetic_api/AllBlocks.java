package com.tropheus_jay.kinetic_api;

import com.tropheus_jay.kinetic_api.content.contraptions.base.KineticBlockSettings;
import net.minecraft.block.Material;

public class AllBlocks {
    //test block
    //shaft

    public static final ShaftBlock SHAFT = new ShaftBlock(KineticBlockSettings.of(Material.STONE));

    /*public static final KineticBlock SHAFT = KineticBlock("shaft", ShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(StressConfigDefaults.setNoImpact())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
            .simpleItem()
            .register();
            */

    public static void init() {

    }
}



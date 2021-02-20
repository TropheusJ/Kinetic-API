package com.tropheus_jay.kinetic_api;

import com.tropheus_jay.kinetic_api.content.contraptions.base.KineticBlockSettings;
import com.tropheus_jay.kinetic_api.content.contraptions.components.motor.CreativeMotorBlock;
import com.tropheus_jay.kinetic_api.content.contraptions.relays.elementary.ShaftBlock;
import net.minecraft.block.Material;

public class AllBlocks {
    //test block
    //shaft

    public static final ShaftBlock SHAFT = new ShaftBlock(KineticBlockSettings.of(Material.STONE).nonOpaque());
    public static final CreativeMotorBlock CREATIVE_MOTOR = new CreativeMotorBlock(KineticBlockSettings.of(Material.METAL));
    
    public static void init() {

    }
}



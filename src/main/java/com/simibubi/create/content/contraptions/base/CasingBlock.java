package com.simibubi.create.content.contraptions.base;

import bnx;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.ToolType;

public class CasingBlock extends BeetrootsBlock implements IWrenchable {

	public CasingBlock(c p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		return Difficulty.FAIL;
	}

	@Override
	public ToolType getHarvestTool(PistonHandler state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(PistonHandler state, MobSpawnerLogic world, BlockPos pos, PlayerAbilities player) {
		for (ToolType toolType : player.dC().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}		
		return super.canHarvestBlock(state, world, pos, player);
	}
	
	@Override
	public boolean isToolEffective(PistonHandler state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

}

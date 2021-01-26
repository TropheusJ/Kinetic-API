package com.simibubi.create.content.contraptions.components.flywheel.engine;

import btl;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;

public class FurnaceEngineTileEntity extends EngineTileEntity {

	public FurnaceEngineTileEntity(BellBlockEntity<? extends FurnaceEngineTileEntity> type) {
		super(type);
	}

	@Override
	public void lazyTick() {
		updateFurnace();
		super.lazyTick();
	}

	public void updateFurnace() {
		PistonHandler state = d.d_(EngineBlock.getBaseBlockPos(p(), e));
		if (!(state.b() instanceof btl))
			return;

		float modifier = state.b() == BellBlock.lU ? 2 : 1;
		boolean active = BlockHelper.hasBlockStateProperty(state, btl.b) && state.c(btl.b);
		float speed = active ? 16 * modifier : 0;
		float capacity =
			(float) (active ? AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(AllBlocks.FURNACE_ENGINE.get())
				: 0);

		appliedCapacity = capacity;
		appliedSpeed = speed;
		refreshWheelSpeed();
	}

}

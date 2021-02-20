package com.tropheus_jay.kinetic_api.content.contraptions.relays.elementary;

import com.tropheus_jay.kinetic_api.content.contraptions.base.KineticTileEntity;
import net.minecraft.block.entity.BlockEntityType;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity(BlockEntityType<? extends SimpleKineticTileEntity> type) {
		super(type);
	}
/*
	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(
			new BracketedTileEntityBehaviour(this, state -> state.b() instanceof AbstractShaftBlock).withTrigger( todo: brackets
				state -> state.b() instanceof ShaftBlock ? AllTriggers.BRACKET_SHAFT : AllTriggers.BRACKET_COG));
		super.addBehaviours(behaviours);
	}*/
/* todo: hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh this is a forge thing
	@Override
	public Box getRenderBoundingBox() {
		return new Box(pos).expand(1);
	}
*/
}

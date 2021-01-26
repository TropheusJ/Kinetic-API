package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import apx;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.timer.Timer;

public class SeatMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		super.startMoving(context);
		int indexOf = context.contraption.getSeats()
			.indexOf(context.localPos);
		context.data.putInt("SeatIndex", indexOf);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		
		AbstractContraptionEntity contraptionEntity = context.contraption.entity;
		if (contraptionEntity == null)
			return;
		int index = context.data.getInt("SeatIndex");
		if (index == -1)
			return;

		Map<UUID, Integer> seatMapping = context.contraption.getSeatMapping();
		PistonHandler blockState = context.world.d_(pos);
		boolean slab = blockState.b() instanceof AbstractSignBlock && blockState.c(AbstractSignBlock.WATERLOGGED) == Property.name;
		boolean solid = blockState.l() || slab;

		// Occupied
		if (seatMapping.containsValue(index)) {
			if (!solid)
				return;
			apx toDismount = null;
			for (Map.Entry<UUID, Integer> entry : seatMapping.entrySet()) {
				if (entry.getValue() != index)
					continue;
				for (apx entity : contraptionEntity.cm()) {
					if (!entry.getKey()
						.equals(entity.bR()))
						continue;
					toDismount = entity;
				}
			}
			if (toDismount != null) {
				toDismount.l();
				EntityHitResult position = VecHelper.getCenterOf(pos)
					.b(0, slab ? .5f : 1f, 0);
				toDismount.a(position.entity, position.c, position.d);
				toDismount.getPersistentData()
					.remove("ContraptionDismountLocation");
			}
			return;
		}

		if (solid)
			return;

		List<apx> nearbyEntities = context.world.a(apx.class,
			new Timer(pos).h(1 / 16f), SeatBlock::canBePickedUp);
		if (!nearbyEntities.isEmpty())
			contraptionEntity.addSittingPassenger(nearbyEntities.get(0), index);
	}

}

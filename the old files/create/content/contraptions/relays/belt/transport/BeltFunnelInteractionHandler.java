package com.simibubi.kinetic_api.content.contraptions.relays.belt.transport;

import afj;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltFunnelInteractionHandler {

	public static boolean checkForFunnels(BeltInventory beltInventory, TransportedItemStack currentItem,
		float nextOffset) {
		boolean beltMovementPositive = beltInventory.beltMovementPositive;
		int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
		int step = beltMovementPositive ? 1 : -1;
		firstUpcomingSegment = afj.a(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

		for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset
			: segment + 1 >= nextOffset; segment += step) {
			BlockPos funnelPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
				.up();
			GameMode world = beltInventory.belt.v();
			PistonHandler funnelState = world.d_(funnelPos);
			if (!(funnelState.b() instanceof BeltFunnelBlock))
				continue;
			Direction funnelFacing = funnelState.c(BeltFunnelBlock.aq);
			Direction movementFacing = beltInventory.belt.getMovementFacing();
			boolean blocking = funnelFacing == movementFacing.getOpposite();
			if (funnelFacing == movementFacing)
				continue;
			if (funnelState.c(BeltFunnelBlock.SHAPE) == Shape.PUSHING)
				continue;

			float funnelEntry = segment + .5f;
			if (funnelState.c(BeltFunnelBlock.SHAPE) == Shape.EXTENDED)
				funnelEntry += .499f * (beltMovementPositive ? -1 : 1);
			
			boolean hasCrossed = nextOffset > funnelEntry && beltMovementPositive
				|| nextOffset < funnelEntry && !beltMovementPositive;
			if (!hasCrossed)
				return false;
			if (blocking)
				currentItem.beltPosition = funnelEntry;

			if (world.v || funnelState.d(BeltFunnelBlock.POWERED).orElse(false))
				if (blocking)
					return true;
				else
					continue;

			BeehiveBlockEntity te = world.c(funnelPos);
			if (!(te instanceof FunnelTileEntity))
				return true;

			FunnelTileEntity funnelTE = (FunnelTileEntity) te;
			InvManipulationBehaviour inserting = funnelTE.getBehaviour(InvManipulationBehaviour.TYPE);
			FilteringBehaviour filtering = funnelTE.getBehaviour(FilteringBehaviour.TYPE);

			if (inserting == null || filtering != null && !filtering.test(currentItem.stack))
				if (blocking)
					return true;
				else
					continue;

			int amountToExtract = funnelTE.getAmountToExtract();
			ItemCooldownManager toInsert = currentItem.stack.i();
			if (amountToExtract > toInsert.E())
				if (blocking)
					return true;
				else
					continue;

			if (amountToExtract != -1)
				toInsert.e(amountToExtract);

			ItemCooldownManager remainder = inserting.insert(toInsert);
			if (toInsert.equals(remainder, false))
				if (blocking)
					return true;
				else
					continue;

			int notFilled = currentItem.stack.E() - toInsert.E();
			if (!remainder.a()) {
				remainder.f(notFilled);
			} else if (notFilled > 0)
				remainder = ItemHandlerHelper.copyStackWithSize(currentItem.stack, notFilled);

			funnelTE.flap(true);
			currentItem.stack = remainder;
			beltInventory.belt.sendData();
			if (blocking)
				return true;
		}

		return false;
	}

}

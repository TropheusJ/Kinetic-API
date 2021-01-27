package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltTunnelInteractionHandler {

	public static boolean flapTunnelsAndCheckIfStuck(BeltInventory beltInventory, TransportedItemStack current,
		float nextOffset) {

		int currentSegment = (int) current.beltPosition;
		int upcomingSegment = (int) nextOffset;

		Direction movementFacing = beltInventory.belt.getMovementFacing();
		if (!beltInventory.beltMovementPositive && nextOffset == 0)
			upcomingSegment = -1;
		if (currentSegment == upcomingSegment)
			return false;

		if (stuckAtTunnel(beltInventory, upcomingSegment, current.stack, movementFacing)) {
			current.beltPosition = currentSegment + (beltInventory.beltMovementPositive ? .99f : .01f);
			return true;
		}

		GameMode world = beltInventory.belt.v();
		boolean onServer = !world.v;
		boolean removed = false;
		BeltTunnelTileEntity nextTunnel = getTunnelOnSegement(beltInventory, upcomingSegment);
		
		if (nextTunnel instanceof BrassTunnelTileEntity) {
			BrassTunnelTileEntity brassTunnel = (BrassTunnelTileEntity) nextTunnel;
			if (brassTunnel.hasDistributionBehaviour()) {
				if (!brassTunnel.canTakeItems())
					return true;
				if (onServer) {
					brassTunnel.setStackToDistribute(current.stack);
					current.stack = ItemCooldownManager.tick;
					beltInventory.belt.sendData();
					beltInventory.belt.X_();
				}
				removed = true;
			}
		} else if (nextTunnel != null) {
			PistonHandler blockState = nextTunnel.p();
			if (current.stack.E() > 1 && AllBlocks.ANDESITE_TUNNEL.has(blockState)
				&& BeltTunnelBlock.isJunction(blockState)
				&& movementFacing.getAxis() == blockState.c(BeltTunnelBlock.HORIZONTAL_AXIS)) {

				for (Direction d : Iterate.horizontalDirections) {
					if (d.getAxis() == blockState.c(BeltTunnelBlock.HORIZONTAL_AXIS))
						continue;
					if (!nextTunnel.flaps.containsKey(d))
						continue;
					BlockPos outpos = nextTunnel.o()
						.down()
						.offset(d);
					if (!world.p(outpos))
						return true;
					DirectBeltInputBehaviour behaviour =
						TileEntityBehaviour.get(world, outpos, DirectBeltInputBehaviour.TYPE);
					if (behaviour == null)
						continue;
					if (!behaviour.canInsertFromSide(d))
						continue;
					
					ItemCooldownManager toinsert = ItemHandlerHelper.copyStackWithSize(current.stack, 1);
					if (!behaviour.handleInsertion(toinsert, d, false).a())
						return true;
					if (onServer) 
						flapTunnel(beltInventory, upcomingSegment, d, false);
					
					current.stack.g(1);
					beltInventory.belt.sendData();
					if (current.stack.E() <= 1)
						break;
				}
			}
		}

		if (onServer) {
			flapTunnel(beltInventory, currentSegment, movementFacing, false);
			flapTunnel(beltInventory, upcomingSegment, movementFacing.getOpposite(), true);
		}

		if (removed)
			return true;

		return false;
	}

	public static boolean stuckAtTunnel(BeltInventory beltInventory, int offset, ItemCooldownManager stack,
		Direction movementDirection) {
		BeltTileEntity belt = beltInventory.belt;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!(belt.v()
			.d_(pos)
			.b() instanceof BrassTunnelBlock))
			return false;
		BeehiveBlockEntity te = belt.v()
			.c(pos);
		if (te == null || !(te instanceof BrassTunnelTileEntity))
			return false;
		BrassTunnelTileEntity tunnel = (BrassTunnelTileEntity) te;
		return !tunnel.canInsert(movementDirection.getOpposite(), stack);
	}

	public static void flapTunnel(BeltInventory beltInventory, int offset, Direction side, boolean inward) {
		BeltTunnelTileEntity te = getTunnelOnSegement(beltInventory, offset);
		if (te == null)
			return;
		te.flap(side, inward ^ side.getAxis() == Axis.Z);
	}

	protected static BeltTunnelTileEntity getTunnelOnSegement(BeltInventory beltInventory, int offset) {
		BeltTileEntity belt = beltInventory.belt;
		if (belt.p()
			.c(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return null;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!(belt.v()
			.d_(pos)
			.b() instanceof BeltTunnelBlock))
			return null;
		BeehiveBlockEntity te = belt.v()
			.c(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return null;
		return ((BeltTunnelTileEntity) te);
	}

}

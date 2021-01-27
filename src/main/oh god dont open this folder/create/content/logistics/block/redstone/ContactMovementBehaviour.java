package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.StructureAccessor;

public class ContactMovementBehaviour extends MovementBehaviour {

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(RedstoneContactBlock.SHAPE).getVector()).a(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		PistonHandler block = context.state;
		GameMode world = context.world;

		if (world.v)
			return;
		if (context.firstMovement)
			return;

		deactivateLastVisitedContact(context);
		PistonHandler visitedState = world.d_(pos);
		if (!AllBlocks.REDSTONE_CONTACT.has(visitedState))
			return;

		EntityHitResult contact = EntityHitResult.b(block.c(RedstoneContactBlock.SHAPE).getVector());
		contact = context.rotation.apply(contact);
		Direction direction = Direction.getFacing(contact.entity, contact.c, contact.d);

		if (!RedstoneContactBlock.hasValidContact(world, pos.offset(direction.getOpposite()), direction))
			return;
		world.a(pos, visitedState.a(RedstoneContactBlock.POWERED, true));
		context.data.put("lastContact", NbtHelper.fromBlockPos(pos));
		return;
	}

	@Override
	public void stopMoving(MovementContext context) {
		deactivateLastVisitedContact(context);
	}

	public void deactivateLastVisitedContact(MovementContext context) {
		if (context.data.contains("lastContact")) {
			BlockPos last = NbtHelper.toBlockPos(context.data.getCompound("lastContact"));
			context.world.I().a(last, AllBlocks.REDSTONE_CONTACT.get(), 1, StructureAccessor.d);
			context.data.remove("lastContact");
		}
	}

}

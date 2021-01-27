package com.simibubi.create.content.contraptions.relays.belt;

import afj;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class BeltHelper {

	public static boolean isItemUpright(ItemCooldownManager stack) {
		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
			.isPresent()
			|| stack.b()
				.a(AllItemTags.UPRIGHT_ON_BELT.tag);
	}

	public static BeltTileEntity getSegmentTE(GrassColors world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 0))
			return null;
		BeehiveBlockEntity tileEntity = world.c(pos);
		if (!(tileEntity instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) tileEntity;
	}

	public static BeltTileEntity getControllerTE(GrassColors world, BlockPos pos) {
		BeltTileEntity segment = getSegmentTE(world, pos);
		if (segment == null)
			return null;
		BlockPos controllerPos = segment.controller;
		if (controllerPos == null)
			return null;
		return getSegmentTE(world, controllerPos);
	}

	public static BeltTileEntity getBeltAtSegment(BeltTileEntity controller, int segment) {
		BlockPos pos = getPositionForOffset(controller, segment);
		BeehiveBlockEntity te = controller.v()
			.c(pos);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public static BlockPos getPositionForOffset(BeltTileEntity controller, int offset) {
		BlockPos pos = controller.o();
		Vec3i vec = controller.getBeltFacing()
			.getVector();
		BeltSlope slope = controller.p()
			.c(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		return pos.add(offset * vec.getX(), afj.a(offset, 0, controller.beltLength - 1) * verticality,
			offset * vec.getZ());
	}

	public static EntityHitResult getVectorForOffset(BeltTileEntity controller, float offset) {
		BeltSlope slope = controller.p()
			.c(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		float verticalMovement = verticality;
		if (offset < .5)
			verticalMovement = 0;
		verticalMovement = verticalMovement * (Math.min(offset, controller.beltLength - .5f) - .5f);
		EntityHitResult vec = VecHelper.getCenterOf(controller.o());
		EntityHitResult horizontalMovement = EntityHitResult.b(controller.getBeltFacing()
			.getVector()).a(offset - .5f);

		if (slope == BeltSlope.VERTICAL)
			horizontalMovement = EntityHitResult.a;

		vec = vec.e(horizontalMovement)
			.b(0, verticalMovement, 0);
		return vec;
	}

}

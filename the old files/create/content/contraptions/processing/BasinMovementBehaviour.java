package com.simibubi.kinetic_api.content.contraptions.processing;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraftforge.items.ItemStackHandler;

public class BasinMovementBehaviour extends MovementBehaviour {
	public Map<String, ItemStackHandler> getOrReadInventory(MovementContext context) {
		Map<String, ItemStackHandler> map = new HashMap<>();
		map.put("InputItems", new ItemStackHandler(9));
		map.put("OutputItems", new ItemStackHandler(8));
		map.forEach((s, h) -> h.deserializeNBT(context.tileData.getCompound(s)));
		return map;
	}

	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		super.tick(context);
		if (context.temporaryData == null || (boolean) context.temporaryData) {
			EntityHitResult facingVec = context.rotation.apply(EntityHitResult.b(Direction.UP.getVector()));
			facingVec.d();
			if (Direction.getFacing(facingVec.entity, facingVec.c, facingVec.d) == Direction.DOWN)
				dump(context, facingVec);
		}
	}

	private void dump(MovementContext context, EntityHitResult facingVec) {
		getOrReadInventory(context).forEach((key, itemStackHandler) -> {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				if (itemStackHandler.getStackInSlot(i)
					.a())
					continue;
				PaintingEntity itemEntity = new PaintingEntity(context.world, context.position.entity, context.position.c,
					context.position.d, itemStackHandler.getStackInSlot(i));
				itemEntity.f(facingVec.a(.05));
				context.world.c(itemEntity);
				itemStackHandler.setStackInSlot(i, ItemCooldownManager.tick);
			}
			context.tileData.put(key, itemStackHandler.serializeNBT());
		});
		BeehiveBlockEntity tileEntity = context.contraption.presentTileEntities.get(context.localPos);
		if (tileEntity instanceof BasinTileEntity)
			((BasinTileEntity) tileEntity).readOnlyItems(context.tileData);
		context.temporaryData = false; // did already dump, so can't any more
	}
}

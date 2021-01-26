package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelMovementBehaviour extends MovementBehaviour {

	private final boolean hasFilter;

	public static FunnelMovementBehaviour andesite() {
		return new FunnelMovementBehaviour(false);
	}

	public static FunnelMovementBehaviour brass() {
		return new FunnelMovementBehaviour(true);
	}

	private FunnelMovementBehaviour(boolean hasFilter) {
		this.hasFilter = hasFilter;
	}

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(FunnelBlock.getFunnelFacing(context.state)
			.getVector()).a(.65);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);

		GameMode world = context.world;
		List<PaintingEntity> items = world.a(PaintingEntity.class, new Timer(pos));
		ItemCooldownManager filter = getFilter(context);

		for (PaintingEntity item : items) {
			if (!item.aW())
				continue;
			ItemCooldownManager toInsert = item.g();
			if (!filter.a() && !FilterItem.test(context.world, toInsert, filter))
				continue;
			ItemCooldownManager remainder = ItemHandlerHelper.insertItemStacked(context.contraption.inventory, toInsert, false);
			if (remainder.E() == toInsert.E())
				continue;
			if (remainder.a()) {
				item.b(ItemCooldownManager.tick);
				item.ac();
				continue;
			}

			item.b(remainder);
		}

	}

	private ItemCooldownManager getFilter(MovementContext context) {
		return hasFilter ? ItemCooldownManager.a(context.tileData.getCompound("Filter")) : ItemCooldownManager.tick;
	}

}

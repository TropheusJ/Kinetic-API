package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import bfs;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.content.logistics.item.filter.FilterItem;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class DeployerMovementBehaviour extends MovementBehaviour {

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(DeployerBlock.FACING)
			.getVector())
			.a(2);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.v)
			return;

		tryGrabbingItem(context);
		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);
		if (mode == Mode.USE && !DeployerHandler.shouldActivate(player.dC(), context.world, pos))
			return;

		activate(context, pos, player, mode);
		tryDisposeOfExcess(context);
		context.stall = player.blockBreakingProgress != null;
	}

	public void activate(MovementContext context, BlockPos pos, DeployerFakePlayer player, Mode mode) {
		EntityHitResult facingVec = EntityHitResult.b(context.state.c(DeployerBlock.FACING)
			.getVector());
		facingVec = context.rotation.apply(facingVec);
		EntityHitResult vec = context.position.d(facingVec.a(2));
		player.p = AbstractContraptionEntity.yawFromVector(facingVec);
		player.q = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;

		DeployerHandler.activate(player, vec, pos, facingVec, mode);
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.v)
			return;
		if (!context.stall)
			return;

		DeployerFakePlayer player = getPlayer(context);
		Mode mode = getMode(context);

		Pair<BlockPos, Float> blockBreakingProgress = player.blockBreakingProgress;
		if (blockBreakingProgress != null) {
			int timer = context.data.getInt("Timer");
			if (timer < 20) {
				timer++;
				context.data.putInt("Timer", timer);
				return;
			}

			context.data.remove("Timer");
			activate(context, blockBreakingProgress.getKey(), player, mode);
			tryDisposeOfExcess(context);
		}

		context.stall = player.blockBreakingProgress != null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.world.v)
			return;

		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;

		context.tileData.put("Inventory", player.bm.a(new ListTag()));
		player.ac();
	}

	private void tryGrabbingItem(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		if (player.dC()
			.a()) {
			ItemCooldownManager filter = getFilter(context);
			ItemCooldownManager held = ItemHelper.extract(context.contraption.inventory,
				stack -> FilterItem.test(context.world, stack, filter), 1, false);
			player.a(ItemScatterer.RANDOM, held);
		}
	}

	private void tryDisposeOfExcess(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		bfs inv = player.bm;
		ItemCooldownManager filter = getFilter(context);

		for (List<ItemCooldownManager> list : Arrays.asList(inv.b, inv.c, inv.a)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemCooldownManager itemstack = list.get(i);
				if (itemstack.a())
					continue;

				if (list == inv.a && i == inv.d
					&& FilterItem.test(context.world, itemstack, filter))
					continue;

				dropItem(context, itemstack);
				list.set(i, ItemCooldownManager.tick);
			}
		}
	}

	@Override
	public void writeExtraData(MovementContext context) {
		DeployerFakePlayer player = getPlayer(context);
		if (player == null)
			return;
		context.data.put("HeldItem", player.dC()
			.serializeNBT());
	}

	private DeployerFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof DeployerFakePlayer) && context.world instanceof ServerWorld) {
			DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerWorld) context.world);
			deployerFakePlayer.bm.b(context.tileData.getList("Inventory", NBT.TAG_COMPOUND));
			if (context.data.contains("HeldItem"))
				deployerFakePlayer.a(ItemScatterer.RANDOM, ItemCooldownManager.a(context.data.getCompound("HeldItem")));
			context.tileData.remove("Inventory");
			context.temporaryData = deployerFakePlayer;
		}
		return (DeployerFakePlayer) context.temporaryData;
	}

	private ItemCooldownManager getFilter(MovementContext context) {
		return ItemCooldownManager.a(context.tileData.getCompound("Filter"));
	}

	private Mode getMode(MovementContext context) {
		return NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
	}

	@Override
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffers) {
		DeployerRenderer.renderInContraption(context, ms, msLocal, buffers);
	}

}

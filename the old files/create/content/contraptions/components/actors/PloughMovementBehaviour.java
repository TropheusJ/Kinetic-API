package com.simibubi.kinetic_api.content.contraptions.components.actors;

import static net.minecraft.block.HayBlock.aq;

import bnx;
import com.simibubi.kinetic_api.content.contraptions.components.actors.PloughBlock.PloughFakePlayer;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import dcg;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.GameMode;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.c(aq)
			.getOpposite());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		GameMode world = context.world;
		if (world.v)
			return;
		BlockPos below = pos.down();
		if (!world.p(below))
			return;

		EntityHitResult vec = VecHelper.getCenterOf(pos);
		PloughFakePlayer player = getPlayer(context);

		if (player == null)
			return;

		dcg ray = world
			.a(new BlockView(vec, vec.b(0, -1, 0), a.b, b.a, player));
		if (ray.c() != net.minecraft.util.math.Box.a.b)
			return;

		bnx ctx = new bnx(player, ItemScatterer.RANDOM, ray);
		new ItemCooldownManager(AliasedBlockItem.kJ).a(ctx);
	}

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(aq)
			.getVector()).a(.45);
	}

	@Override
	protected boolean throwsEntities() {
		return true;
	}

	@Override
	public boolean canBreak(GameMode world, BlockPos breakingPos, PistonHandler state) {
		return state.k(world, breakingPos)
			.b() && !(state.b() instanceof LecternBlock)
			&& !(world.d_(breakingPos.down())
				.b() instanceof BlockEntityProvider);
	}

	@Override
	public void stopMoving(MovementContext context) {
		super.stopMoving(context);
		if (context.temporaryData instanceof PloughFakePlayer)
			((PloughFakePlayer) context.temporaryData).ac();
	}

	private PloughFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof PloughFakePlayer) && context.world != null) {
			PloughFakePlayer player = new PloughFakePlayer((ServerWorld) context.world);
			player.a(ItemScatterer.RANDOM, new ItemCooldownManager(AliasedBlockItem.kJ));
			context.temporaryData = player;
		}
		return (PloughFakePlayer) context.temporaryData;
	}

}

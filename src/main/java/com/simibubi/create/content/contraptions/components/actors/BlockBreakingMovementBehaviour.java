package com.simibubi.create.content.contraptions.components.actors;

import afj;
import apx;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;

public class BlockBreakingMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		if (context.world.v)
			return;
		context.data.putInt("BreakerId", -BlockBreakingKineticTileEntity.NEXT_BREAKER_ID.incrementAndGet());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		GameMode world = context.world;
		PistonHandler stateVisited = world.d_(pos);

		if (!stateVisited.g(world, pos))
			damageEntities(context, pos, world);
		if (world.v)
			return;
		if (!canBreak(world, pos, stateVisited))
			return;

		context.data.put("BreakingPos", NbtHelper.fromBlockPos(pos));
		context.stall = true;
	}

	public void damageEntities(MovementContext context, BlockPos pos, GameMode world) {
		DamageRecord damageSource = getDamageSource();
		if (damageSource == null && !throwsEntities())
			return;
		Entities: for (apx entity : world.a(apx.class, new Timer(pos))) {
			if (entity instanceof PaintingEntity)
				continue;
			if (entity instanceof AbstractContraptionEntity)
				continue;
			if (entity instanceof ScheduleBuilder)
				for (apx passenger : entity.cn())
					if (passenger instanceof AbstractContraptionEntity
							&& ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
						continue Entities;

			if (damageSource != null && !world.v) {
				float damage = (float) afj.a(6 * Math.pow(context.relativeMotion.f(), 0.4) + 1, 2, 10);
				entity.a(damageSource, damage);
			}
			if (throwsEntities() && (world.v == (entity instanceof PlayerAbilities))) {
				EntityHitResult motionBoost = context.motion.b(0, context.motion.f() / 4f, 0);
				int maxBoost = 4;
				if (motionBoost.f() > maxBoost) {
					motionBoost = motionBoost.d(motionBoost.d().a(motionBoost.f() - maxBoost));
				}
				entity.f(entity.cB().e(motionBoost));
				entity.w = true;
			}
		}
	}

	protected DamageRecord getDamageSource() {
		return null;
	}

	protected boolean throwsEntities() {
		return getDamageSource() != null;
	}

	@Override
	public void stopMoving(MovementContext context) {
		CompoundTag data = context.data;
		if (context.world.v)
			return;
		if (!data.contains("BreakingPos"))
			return;

		GameMode world = context.world;
		int id = data.getInt("BreakerId");
		BlockPos breakingPos = NbtHelper.toBlockPos(data.getCompound("BreakingPos"));

		data.remove("Progress");
		data.remove("TicksUntilNextProgress");
		data.remove("BreakingPos");

		context.stall = false;
		world.a(id, breakingPos, -1);
	}

	@Override
	public void tick(MovementContext context) {
		tickBreaker(context);

		CompoundTag data = context.data;
		if (!data.contains("WaitingTicks"))
			return;

		int waitingTicks = data.getInt("WaitingTicks");
		if (waitingTicks-- > 0) {
			data.putInt("WaitingTicks", waitingTicks);
			context.stall = true;
			return;
		}

		BlockPos pos = NbtHelper.toBlockPos(data.getCompound("LastPos"));
		data.remove("WaitingTicks");
		data.remove("LastPos");
		context.stall = false;
		visitNewPosition(context, pos);
	}

	public void tickBreaker(MovementContext context) {
		CompoundTag data = context.data;
		if (context.world.v)
			return;
		if (!data.contains("BreakingPos"))
			return;
		if (context.relativeMotion.equals(EntityHitResult.a)) {
			context.stall = false;
			return;
		}

		int ticksUntilNextProgress = data.getInt("TicksUntilNextProgress");
		if (ticksUntilNextProgress-- > 0) {
			data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
			return;
		}

		GameMode world = context.world;
		BlockPos breakingPos = NbtHelper.toBlockPos(data.getCompound("BreakingPos"));
		int destroyProgress = data.getInt("Progress");
		int id = data.getInt("BreakerId");
		PistonHandler stateToBreak = world.d_(breakingPos);
		float blockHardness = stateToBreak.h(world, breakingPos);

		if (!canBreak(world, breakingPos, stateToBreak)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				data.remove("Progress");
				data.remove("TicksUntilNextProgress");
				data.remove("BreakingPos");
				world.a(id, breakingPos, -1);
			}
			context.stall = false;
			return;
		}

		float breakSpeed = afj.a(Math.abs(context.getAnimationSpeed()) / 500f, 1 / 128f, 16f);
		destroyProgress += afj.a((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);

		if (destroyProgress >= 10) {
			world.a(id, breakingPos, -1);
			
			// break falling blocks from top to bottom
			BlockPos ogPos = breakingPos;
			PistonHandler stateAbove = world.d_(breakingPos.up());
			while (stateAbove.b() instanceof EnderChestBlock) {
				breakingPos = breakingPos.up();
				stateAbove = world.d_(breakingPos.up());
			}
			stateToBreak = world.d_(breakingPos);
			
			context.stall = false;
			BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> this.dropItem(context, stack));
			onBlockBroken(context, ogPos, stateToBreak);
			ticksUntilNextProgress = -1;
			data.remove("Progress");
			data.remove("TicksUntilNextProgress");
			data.remove("BreakingPos");
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		world.a(id, breakingPos, (int) destroyProgress);
		data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
		data.putInt("Progress", destroyProgress);
	}

	public boolean canBreak(GameMode world, BlockPos breakingPos, PistonHandler state) {
		float blockHardness = state.h(world, breakingPos);
		return BlockBreakingKineticTileEntity.isBreakable(state, blockHardness);
	}

	protected void onBlockBroken(MovementContext context, BlockPos pos, PistonHandler brokenState) {
		// Check for falling blocks
		if (!(brokenState.b() instanceof EnderChestBlock))
			return;

		CompoundTag data = context.data;
		data.putInt("WaitingTicks", 10);
		data.put("LastPos", NbtHelper.fromBlockPos(pos));
		context.stall = true;
	}

}

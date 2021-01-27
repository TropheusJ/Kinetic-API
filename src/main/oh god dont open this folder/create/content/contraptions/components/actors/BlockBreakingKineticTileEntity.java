package com.simibubi.create.content.contraptions.components.actors;

import java.util.concurrent.atomic.AtomicInteger;
import afj;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.ExplosionBehavior;

public abstract class BlockBreakingKineticTileEntity extends KineticTileEntity {

	public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
	protected int ticksUntilNextProgress;
	protected int destroyProgress;
	protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
	protected BlockPos breakingPos;

	public BlockBreakingKineticTileEntity(BellBlockEntity<?> typeIn) {
		super(typeIn);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (destroyProgress == -1)
			destroyNextTick();
	}
	
	@Override
	public void lazyTick() {
		super.lazyTick();
		if (ticksUntilNextProgress == -1)
			destroyNextTick();
	}

	public void destroyNextTick() {
		ticksUntilNextProgress = 1;
	}

	protected abstract BlockPos getBreakingPos();

	protected boolean shouldRun() {
		return true;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Progress", destroyProgress);
		compound.putInt("NextTick", ticksUntilNextProgress);
		if (breakingPos != null)
			compound.put("Breaking", NbtHelper.fromBlockPos(breakingPos));
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		if (compound.contains("Breaking"))
			breakingPos = NbtHelper.toBlockPos(compound.getCompound("Breaking"));
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void al_() {
		if (!d.v && destroyProgress != 0)
			d.a(breakerId, breakingPos, -1);
		super.al_();
	}

	@Override
	public void aj_() {
		super.aj_();

		if (d.v)
			return;
		if (!shouldRun())
			return;
		if (getSpeed() == 0)
			return;
		
		breakingPos = getBreakingPos();
		
		if (ticksUntilNextProgress < 0)
			return;
		if (ticksUntilNextProgress-- > 0)
			return;

		PistonHandler stateToBreak = d.d_(breakingPos);
		float blockHardness = stateToBreak.h(d, breakingPos);

		if (!canBreak(stateToBreak, blockHardness)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				d.a(breakerId, breakingPos, -1);
			}
			return;
		}

		float breakSpeed = getBreakSpeed();
		destroyProgress += afj.a((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);

		if (destroyProgress >= 10) {
			onBlockBroken(stateToBreak);
			destroyProgress = 0;
			ticksUntilNextProgress = -1;
			d.a(breakerId, breakingPos, -1);
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		d.a(breakerId, breakingPos, (int) destroyProgress);
	}

	public boolean canBreak(PistonHandler stateToBreak, float blockHardness) {
		return isBreakable(stateToBreak, blockHardness);
	}

	public static boolean isBreakable(PistonHandler stateToBreak, float blockHardness) {
		return !(stateToBreak.c().a() || stateToBreak.b() instanceof AbstractFurnaceBlock
				|| blockHardness == -1);
	}

	public void onBlockBroken(PistonHandler stateToBreak) {
		EmptyFluid FluidState = d.b(breakingPos);
		d.syncWorldEvent(2001, breakingPos, BeetrootsBlock.i(stateToBreak));
		BeehiveBlockEntity tileentity = stateToBreak.hasTileEntity() ? d.c(breakingPos) : null;
		EntityHitResult vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), d.t, .125f);

		BeetrootsBlock.a(stateToBreak, (ServerWorld) d, breakingPos, tileentity).forEach((stack) -> {
			if (!stack.a() && d.U().b(ExplosionBehavior.f)
					&& !d.restoringBlockSnapshots) {
				PaintingEntity itementity = new PaintingEntity(d, vec.entity, vec.c, vec.d, stack);
				itementity.m();
				itementity.f(EntityHitResult.a);
				d.c(itementity);
			}
		});
		if (d instanceof ServerWorld)
			stateToBreak.a((ServerWorld) d, breakingPos, ItemCooldownManager.tick);
		d.a(breakingPos, FluidState.g(), 3);
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}

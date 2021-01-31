package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley;

import afj;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.BlockMovementTraits;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.LinearActuatorTileEntity;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import net.minecraft.block.BellBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;

public class PulleyTileEntity extends LinearActuatorTileEntity {

	protected int initialOffset;

	public PulleyTileEntity(BellBlockEntity<? extends PulleyTileEntity> type) {
		super(type);
	}

	@Override
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().b(0, -offset, 0);
	}

	@Override
	public double i() {
		return super.i() + offset * offset;
	}

	@Override
	protected void assemble() {
		if (!(d.d_(e)
			.b() instanceof PulleyBlock))
			return;
		if (speed == 0)
			return;
		if (offset >= getExtensionRange() && getSpeed() > 0)
			return;
		if (offset <= 0 && getSpeed() < 0)
			return;

		// Collect Construct
		if (!d.v) {
			BlockPos anchor = e.down(afj.d(offset + 1));
			initialOffset = afj.d(offset);
			PulleyContraption contraption = new PulleyContraption(initialOffset);
			boolean canAssembleStructure = contraption.assemble(d, anchor);

			if (canAssembleStructure) {
				Direction movementDirection = getSpeed() > 0 ? Direction.DOWN : Direction.UP;
				if (ContraptionCollider.isCollidingWithWorld(d, contraption, anchor.offset(movementDirection),
					movementDirection))
					canAssembleStructure = false;
			}

			if (!canAssembleStructure && getSpeed() > 0)
				return;

			for (int i = ((int) offset); i > 0; i--) {
				BlockPos offset = e.down(i);
				PistonHandler oldState = d.d_(offset);
				if (oldState.b() instanceof SeagrassBlock && oldState.b(BambooLeaves.C)
					&& oldState.c(BambooLeaves.C)) {
					d.a(offset, BellBlock.A.n(), 66);
					continue;
				}
				d.a(offset, BellBlock.FACING.n(), 66);
			}

			if (!contraption.getBlocks().isEmpty()) {
				contraption.removeBlocksFromWorld(d, BlockPos.ORIGIN);
				movedContraption = ControlledContraptionEntity.create(d, this, contraption);
				movedContraption.d(anchor.getX(), anchor.getY(), anchor.getZ());
				d.c(movedContraption);
				forceMove = true;
			}
		}

		clientOffsetDiff = 0;
		running = true;
		sendData();
	}

	@Override
	public void disassemble() {
		if (!running && movedContraption == null)
			return;
		offset = getGridOffset(offset);
		if (movedContraption != null)
			applyContraptionPosition();

		if (!d.v) {
			if (!f) {
				if (offset > 0) {
					BlockPos magnetPos = e.down((int) offset);
					EmptyFluid ifluidstate = d.b(magnetPos);
					d.b(magnetPos, d.d_(magnetPos)
						.k(d, magnetPos)
						.b());
					d.a(magnetPos, AllBlocks.PULLEY_MAGNET.getDefaultState()
						.a(BambooLeaves.C,
							Boolean.valueOf(ifluidstate.a() == FlowableFluid.c)),
						66);
				}

				boolean[] waterlog = new boolean[(int) offset];

				for (int i = 1; i <= ((int) offset) - 1; i++) {
					BlockPos ropePos = e.down(i);
					EmptyFluid ifluidstate = d.b(ropePos);
					waterlog[i] = ifluidstate.a() == FlowableFluid.c;
					d.b(ropePos, d.d_(ropePos)
						.k(d, ropePos)
						.b());
				}
				for (int i = 1; i <= ((int) offset) - 1; i++)
					d.a(e.down(i), AllBlocks.ROPE.getDefaultState()
						.a(BambooLeaves.C, waterlog[i]), 66);
			}

			if (movedContraption != null)
				movedContraption.disassemble();
		}

		if (movedContraption != null)
			movedContraption.ac();
		movedContraption = null;
		initialOffset = 0;
		running = false;
		sendData();
	}

	@Override
	protected EntityHitResult toPosition(float offset) {
		if (movedContraption.getContraption() instanceof PulleyContraption) {
			PulleyContraption contraption = (PulleyContraption) movedContraption.getContraption();
			return EntityHitResult.b(contraption.anchor).b(0, contraption.initialOffset - offset, 0);

		}
		return EntityHitResult.a;
	}

	@Override
	protected void visitNewPosition() {
		super.visitNewPosition();
		if (d.v)
			return;
		if (movedContraption != null)
			return;
		if (getSpeed() <= 0)
			return;

		BlockPos posBelow = e.down((int) (offset + getMovementSpeed()) + 1);
		if (!BlockMovementTraits.movementNecessary(d, posBelow))
			return;
		if (BlockMovementTraits.isBrittle(d.d_(posBelow)))
			return;

		disassemble();
		assembleNextTick = true;
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		initialOffset = compound.getInt("InitialOffset");
		super.fromTag(state, compound, clientPacket);
	}
	
	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("InitialOffset", initialOffset);
		super.write(compound, clientPacket);
	}

	@Override
	protected int getExtensionRange() {
		return Math.max(0, Math.min(AllConfigs.SERVER.kinetics.maxRopeLength.get(), e.getY() - 1));
	}

	@Override
	protected int getInitialOffset() {
		return initialOffset;
	}

	@Override
	protected EntityHitResult toMotionVector(float speed) {
		return new EntityHitResult(0, -speed, 0);
	}

	@Override
	protected ValueBoxTransform getMovementModeSlot() {
		return new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP);
	}

}

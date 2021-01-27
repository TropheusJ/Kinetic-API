package com.simibubi.create.content.contraptions.components.structureMovement;

import afj;
import apx;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.block.entity.EndGatewayBlockEntityRenderer;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.util.hit.EntityHitResult;

public class OrientedContraptionEntityRenderer extends AbstractContraptionEntityRenderer<OrientedContraptionEntity> {

	public OrientedContraptionEntityRenderer(DolphinEntityRenderer p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, EndGatewayBlockEntityRenderer p_225626_2_, double p_225626_3_,
		double p_225626_5_, double p_225626_7_) {
		if (!super.shouldRender(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;
		if (entity.getContraption()
			.getType() == AllContraptionTypes.MOUNTED && entity.cs() == null)
			return false;
		return true;
	}
	
	@Override
	protected void transform(OrientedContraptionEntity entity, float partialTicks, BufferVertexConsumer[] matrixStacks) {
		float angleInitialYaw = entity.getInitialYaw();
		float angleYaw = entity.h(partialTicks);
		float anglePitch = entity.g(partialTicks);

		for (BufferVertexConsumer stack : matrixStacks)
			stack.a(-.5f, 0, -.5f);

		apx ridingEntity = entity.cs();
		if (ridingEntity instanceof ScheduleBuilder)
			repositionOnCart(partialTicks, matrixStacks, ridingEntity);
		if (ridingEntity instanceof AbstractContraptionEntity) {
			if (ridingEntity.cs() instanceof ScheduleBuilder)
				repositionOnCart(partialTicks, matrixStacks, ridingEntity.cs());
			else
				repositionOnContraption(entity, partialTicks, matrixStacks, ridingEntity);
		}

		for (BufferVertexConsumer stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(entity.X())
				.centre()
				.rotateY(angleYaw)
				.rotateZ(anglePitch)
				.rotateY(angleInitialYaw)
				.unCentre();
	}

	private void repositionOnContraption(OrientedContraptionEntity entity, float partialTicks,
		BufferVertexConsumer[] matrixStacks, apx ridingEntity) {
		AbstractContraptionEntity parent = (AbstractContraptionEntity) ridingEntity;
		EntityHitResult passengerPosition = parent.getPassengerPosition(entity, partialTicks);
		double x = passengerPosition.entity - afj.d(partialTicks, entity.D, entity.cC());
		double y = passengerPosition.c - afj.d(partialTicks, entity.E, entity.cD());
		double z = passengerPosition.d - afj.d(partialTicks, entity.F, entity.cG());
		for (BufferVertexConsumer stack : matrixStacks)
			stack.a(x, y, z);
	}

	// Minecarts do not always render at their exact location, so the contraption
	// has to adjust aswell
	private void repositionOnCart(float partialTicks, BufferVertexConsumer[] matrixStacks, apx ridingEntity) {
		ScheduleBuilder cart = (ScheduleBuilder) ridingEntity;
		double cartX = afj.d(partialTicks, cart.D, cart.cC());
		double cartY = afj.d(partialTicks, cart.E, cart.cD());
		double cartZ = afj.d(partialTicks, cart.F, cart.cG());
		EntityHitResult cartPos = cart.p(cartX, cartY, cartZ);

		if (cartPos != null) {
			EntityHitResult cartPosFront = cart.a(cartX, cartY, cartZ, (double) 0.3F);
			EntityHitResult cartPosBack = cart.a(cartX, cartY, cartZ, (double) -0.3F);
			if (cartPosFront == null)
				cartPosFront = cartPos;
			if (cartPosBack == null)
				cartPosBack = cartPos;

			cartX = cartPos.entity - cartX;
			cartY = (cartPosFront.c + cartPosBack.c) / 2.0D - cartY;
			cartZ = cartPos.d - cartZ;

			for (BufferVertexConsumer stack : matrixStacks)
				stack.a(cartX, cartY, cartZ);
		}
	}

}

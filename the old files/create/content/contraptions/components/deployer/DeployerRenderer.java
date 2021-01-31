package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import static com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.kinetic_api.content.contraptions.base.DirectionalKineticBlock.FACING;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerTileEntity.Mode;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerTileEntity.State;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.BannerItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.GameMode;

public class DeployerRenderer extends SafeTileEntityRenderer<DeployerTileEntity> {

	public DeployerRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(DeployerTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		renderItem(te, partialTicks, ms, buffer, light, overlay);
		FilteringRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay);
		renderComponents(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItem(DeployerTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		PistonHandler deployerState = te.p();
		EntityHitResult offset = getHandOffset(te, partialTicks, deployerState).e(VecHelper.getCenterOf(BlockPos.ORIGIN));
		ms.a();
		ms.a(offset.entity, offset.c, offset.d);

		Direction facing = deployerState.c(FACING);
		boolean punching = te.mode == Mode.PUNCH;

		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		boolean displayMode = facing == Direction.UP && te.getSpeed() == 0 && !punching;

		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(yRot));
		if (!displayMode) {
			ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(zRot));
			ms.a(0, 0, -11 / 16f);
		}

		if (punching)
			ms.a(0, 1 / 8f, -1 / 16f);

		HorseEntityRenderer itemRenderer = KeyBinding.B()
			.ac();

		b transform = b.a;
		boolean isBlockItem = (te.heldItem.b() instanceof BannerItem)
			&& itemRenderer.a(te.heldItem, KeyBinding.B().r, null)
				.b();

		if (displayMode) {
			float scale = isBlockItem ? 1.25f : 1;
			ms.a(0, isBlockItem ? 9 / 16f : 11 / 16f, 0);
			ms.a(scale, scale, scale);
			transform = b.h;
			ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(AnimationTickHolder.getRenderTick()));

		} else {
			float scale = punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
			ms.a(scale, scale, scale);
			transform = punching ? b.c : b.i;
		}

		itemRenderer.a(te.heldItem, transform, light, overlay, ms, buffer);
		ms.b();
	}

	protected void renderComponents(DeployerTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		KineticTileEntityRenderer.renderRotatingKineticBlock(te, getRenderedBlockState(te), ms, vb, light);

		PistonHandler blockState = te.p();
		BlockPos pos = te.o();
		EntityHitResult offset = getHandOffset(te, partialTicks, blockState);

		SuperByteBuffer pole = AllBlockPartials.DEPLOYER_POLE.renderOn(blockState);
		SuperByteBuffer hand = te.getHandPose()
			.renderOn(blockState);

		transform(te.v(), pole.translate(offset.entity, offset.c, offset.d), blockState, pos, true).renderInto(ms,
			vb);
		transform(te.v(), hand.translate(offset.entity, offset.c, offset.d), blockState, pos, false).renderInto(ms,
			vb);
	}

	protected EntityHitResult getHandOffset(DeployerTileEntity te, float partialTicks, PistonHandler blockState) {
		float progress = 0;
		if (te.state == State.EXPANDING)
			progress = 1 - (te.timer - partialTicks * te.getTimerSpeed()) / 1000f;
		if (te.state == State.RETRACTING)
			progress = (te.timer - partialTicks * te.getTimerSpeed()) / 1000f;

		float handLength = te.getHandPose() == AllBlockPartials.DEPLOYER_HAND_POINTING ? 0
			: te.getHandPose() == AllBlockPartials.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
		float distance = Math.min(afj.a(progress, 0, 1) * (te.reach + handLength), 21 / 16f);
		EntityHitResult offset = EntityHitResult.b(blockState.c(FACING)
			.getVector()).a(distance);
		return offset;
	}

	protected PistonHandler getRenderedBlockState(KineticTileEntity te) {
		return KineticTileEntityRenderer.shaft(KineticTileEntityRenderer.getRotationAxisOf(te));
	}

	private static SuperByteBuffer transform(GameMode world, SuperByteBuffer buffer, PistonHandler deployerState,
		BlockPos pos, boolean axisDirectionMatters) {
		Direction facing = deployerState.c(FACING);

		float zRotLast =
			axisDirectionMatters && (deployerState.c(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Axis.Z) ? 90
				: 0;
		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

		buffer.rotateCentered(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.UP, (float) ((yRot) / 180 * Math.PI));
		buffer.rotateCentered(Direction.SOUTH, (float) ((zRotLast) / 180 * Math.PI));
		buffer.light(JsonGlProgram.a(world, deployerState, pos));
		return buffer;
	}

	public static void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		BufferVertexConsumer[] matrixStacks = new BufferVertexConsumer[] { ms, msLocal };
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.c());
		PistonHandler blockState = context.state;
		BlockPos pos = BlockPos.ORIGIN;
		Mode mode = NBTHelper.readEnum(context.tileData, "Mode", Mode.class);
		GameMode world = context.world;
		AllBlockPartials handPose =
			mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING : AllBlockPartials.DEPLOYER_HAND_POINTING;

		SuperByteBuffer pole = AllBlockPartials.DEPLOYER_POLE.renderOn(blockState);
		SuperByteBuffer hand = handPose.renderOn(blockState);
		pole = transform(world, pole, blockState, pos, true);
		hand = transform(world, hand, blockState, pos, false);

		double factor;
		if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
			factor = afj.a(AnimationTickHolder.getRenderTick() * .5f) * .25f + .25f;
		} else {
			EntityHitResult center = VecHelper.getCenterOf(new BlockPos(context.position));
			double distance = context.position.f(center);
			double nextDistance = context.position.e(context.motion)
				.f(center);
			factor = .5f - afj.a(afj.d(KeyBinding.B()
				.ai(), distance, nextDistance), 0, 1);
		}

		EntityHitResult offset = EntityHitResult.b(blockState.c(FACING)
			.getVector()).a(factor);

		Matrix4f lighting = msLocal.c()
			.a();
		for (BufferVertexConsumer m : matrixStacks)
			m.a(offset.entity, offset.c, offset.d);
		pole.light(lighting)
			.renderInto(ms, builder);
		hand.light(lighting)
			.renderInto(ms, builder);
	}

}

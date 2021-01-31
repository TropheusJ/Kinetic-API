package com.simibubi.kinetic_api.foundation.tileEntity.behaviour;

import java.util.function.Function;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import org.apache.commons.lang3.tuple.Pair;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;

public abstract class ValueBoxTransform {

	protected float scale = getScale();

	protected abstract EntityHitResult getLocalOffset(PistonHandler state);

	protected abstract void rotate(PistonHandler state, BufferVertexConsumer ms);

	public boolean testHit(PistonHandler state, EntityHitResult localHit) {
		EntityHitResult offset = getLocalOffset(state);
		if (offset == null)
			return false;
		return localHit.f(offset) < scale / 2;
	}

	public void transform(PistonHandler state, BufferVertexConsumer ms) {
		EntityHitResult position = getLocalOffset(state);
		if (position == null)
			return;
		ms.a(position.entity, position.c, position.d);
		rotate(state, ms);
		ms.a(scale, scale, scale);
	}

	public boolean shouldRender(PistonHandler state) {
		return state.c() != FluidState.CODEC && getLocalOffset(state) != null;
	}

	protected EntityHitResult rotateHorizontally(PistonHandler state, EntityHitResult vec) {
		float yRot = 0;
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.M))
			yRot = AngleHelper.horizontalAngle(state.c(BambooLeaves.M));
		if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.O))
			yRot = AngleHelper.horizontalAngle(state.c(BambooLeaves.O));
		return VecHelper.rotateCentered(vec, yRot, Axis.Y);
	}

	protected float getScale() {
		return .4f;
	}

	protected float getFontScale() {
		return 1 / 64f;
	}

	public static abstract class Dual extends ValueBoxTransform {

		protected boolean first;

		public Dual(boolean first) {
			this.first = first;
		}

		public boolean isFirst() {
			return first;
		}

		public static Pair<ValueBoxTransform, ValueBoxTransform> makeSlots(Function<Boolean, ? extends Dual> factory) {
			return Pair.of(factory.apply(true), factory.apply(false));
		}

		public boolean testHit(PistonHandler state, EntityHitResult localHit) {
			EntityHitResult offset = getLocalOffset(state);
			if (offset == null)
				return false;
			return localHit.f(offset) < scale / 3.5f;
		}

	}

	public static abstract class Sided extends ValueBoxTransform {

		protected Direction direction = Direction.UP;

		public Sided fromSide(Direction direction) {
			this.direction = direction;
			return this;
		}

		@Override
		protected EntityHitResult getLocalOffset(PistonHandler state) {
			EntityHitResult location = getSouthLocation();
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Axis.Y);
			location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Axis.Z);
			return location;
		}

		protected abstract EntityHitResult getSouthLocation();

		@Override
		protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
			float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
			float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
			MatrixStacker.of(ms)
				.rotateY(yRot)
				.rotateX(xRot);
		}

		@Override
		public boolean shouldRender(PistonHandler state) {
			return super.shouldRender(state) && isSideActive(state, getSide());
		}

		@Override
		public boolean testHit(PistonHandler state, EntityHitResult localHit) {
			return isSideActive(state, getSide()) && super.testHit(state, localHit);
		}

		protected boolean isSideActive(PistonHandler state, Direction direction) {
			return true;
		}

		public Direction getSide() {
			return direction;
		}

	}

}

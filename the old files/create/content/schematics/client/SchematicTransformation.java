package com.simibubi.kinetic_api.content.schematics.client;

import static java.lang.Math.abs;

import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingAngle;
import com.simibubi.kinetic_api.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.timer.Timer;

public class SchematicTransformation {

	private InterpolatedChasingValue x, y, z, scaleFrontBack, scaleLeftRight;
	private InterpolatedChasingAngle rotation;
	private double xOrigin;
	private double zOrigin;

	public SchematicTransformation() {
		x = new InterpolatedChasingValue();
		y = new InterpolatedChasingValue();
		z = new InterpolatedChasingValue();
		scaleFrontBack = new InterpolatedChasingValue();
		scaleLeftRight = new InterpolatedChasingValue();
		rotation = new InterpolatedChasingAngle();
	}

	public void init(BlockPos anchor, RuleTest settings, Timer bounds) {
		int leftRight = settings.c() == LoomBlock.b ? -1 : 1;
		int frontBack = settings.c() == LoomBlock.c ? -1 : 1;
		getScaleFB().start(frontBack);
		getScaleLR().start(leftRight);
		xOrigin = bounds.b() / 2f;
		zOrigin = bounds.d() / 2f;

		int r = -(settings.d()
			.ordinal() * 90);
		rotation.start(r);

		EntityHitResult vec = fromAnchor(anchor);
		x.start((float) vec.entity);
		y.start((float) vec.c);
		z.start((float) vec.d);
	}

	public void applyGLTransformations(BufferVertexConsumer ms) {
		float pt = KeyBinding.B()
			.ai();

		// Translation
		ms.a(x.get(pt), y.get(pt), z.get(pt));
		EntityHitResult rotationOffset = getRotationOffset(true);

		// Rotation & Mirror
		float fb = getScaleFB().get(pt);
		float lr = getScaleLR().get(pt);
		float rot = rotation.get(pt) + ((fb < 0 && lr < 0) ? 180 : 0);
		ms.a(xOrigin, 0, zOrigin);
		MatrixStacker.of(ms)
			.translate(rotationOffset)
			.rotateY(rot)
			.translateBack(rotationOffset);
		ms.a(abs(fb), 1, abs(lr));
		ms.a(-xOrigin, 0, -zOrigin);

	}

	public boolean isFlipped() {
		return getMirrorModifier(Axis.X) < 0 != getMirrorModifier(Axis.Z) < 0;
	}

	public EntityHitResult getRotationOffset(boolean ignoreMirrors) {
		EntityHitResult rotationOffset = EntityHitResult.a;
		if ((int) (zOrigin * 2) % 2 != (int) (xOrigin * 2) % 2) {
			boolean xGreaterZ = xOrigin > zOrigin;
			float xIn = (xGreaterZ ? 0 : .5f);
			float zIn = (!xGreaterZ ? 0 : .5f);
			if (!ignoreMirrors) {
				xIn *= getMirrorModifier(Axis.X);
				zIn *= getMirrorModifier(Axis.Z);
			}
			rotationOffset = new EntityHitResult(xIn, 0, zIn);
		}
		return rotationOffset;
	}

	public EntityHitResult toLocalSpace(EntityHitResult vec) {
		float pt = KeyBinding.B()
			.ai();
		EntityHitResult rotationOffset = getRotationOffset(true);

		vec = vec.a(x.get(pt), y.get(pt), z.get(pt));
		vec = vec.a(xOrigin + rotationOffset.entity, 0, zOrigin + rotationOffset.d);
		vec = VecHelper.rotate(vec, -rotation.get(pt), Axis.Y);
		vec = vec.b(rotationOffset.entity, 0, rotationOffset.d);
		vec = vec.d(getScaleFB().get(pt), 1, getScaleLR().get(pt));
		vec = vec.b(xOrigin, 0, zOrigin);

		return vec;
	}

	public RuleTest toSettings() {
		RuleTest settings = new RuleTest();

		int i = (int) rotation.getTarget();

		boolean mirrorlr = getScaleLR().getTarget() < 0;
		boolean mirrorfb = getScaleFB().getTarget() < 0;
		if (mirrorlr && mirrorfb) {
			mirrorlr = mirrorfb = false;
			i += 180;
		}
		i = i % 360;
		if (i < 0)
			i += 360;

		RespawnAnchorBlock rotation = RespawnAnchorBlock.CHARGES;
		switch (i) {
		case 90:
			rotation = RespawnAnchorBlock.d;
			break;
		case 180:
			rotation = RespawnAnchorBlock.field_26443;
			break;
		case 270:
			rotation = RespawnAnchorBlock.field_26442;
			break;
		default:
		}

		settings.a(rotation);
		if (mirrorfb)
			settings.a(LoomBlock.c);
		if (mirrorlr)
			settings.a(LoomBlock.b);

		return settings;
	}

	public BlockPos getAnchor() {
		EntityHitResult vec = EntityHitResult.a.b(.5, 0, .5);
		EntityHitResult rotationOffset = getRotationOffset(false);
		vec = vec.a(xOrigin, 0, zOrigin);
		vec = vec.a(rotationOffset.entity, 0, rotationOffset.d);
		vec = vec.d(getScaleFB().getTarget(), 1, getScaleLR().getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.b(xOrigin, 0, zOrigin);

		vec = vec.b(x.getTarget(), y.getTarget(), z.getTarget());
		return new BlockPos(vec.entity, vec.c, vec.d);
	}

	public EntityHitResult fromAnchor(BlockPos pos) {
		EntityHitResult vec = EntityHitResult.a.b(.5, 0, .5);
		EntityHitResult rotationOffset = getRotationOffset(false);
		vec = vec.a(xOrigin, 0, zOrigin);
		vec = vec.a(rotationOffset.entity, 0, rotationOffset.d);
		vec = vec.d(getScaleFB().getTarget(), 1, getScaleLR().getTarget());
		vec = VecHelper.rotate(vec, rotation.getTarget(), Axis.Y);
		vec = vec.b(xOrigin, 0, zOrigin);

		return EntityHitResult.b(pos.subtract(new BlockPos(vec.entity, vec.c, vec.d)));
	}

	public int getRotationTarget() {
		return (int) rotation.getTarget();
	}

	public int getMirrorModifier(Axis axis) {
		if (axis == Axis.Z)
			return (int) getScaleLR().getTarget();
		return (int) getScaleFB().getTarget();
	}

	public float getCurrentRotation() {
		float pt = KeyBinding.B()
			.ai();
		return rotation.get(pt);
	}

	public void tick() {
		x.tick();
		y.tick();
		z.tick();
		getScaleLR().tick();
		getScaleFB().tick();
		rotation.tick();
	}

	public void flip(Axis axis) {
		if (axis == Axis.X)
			getScaleLR().target(getScaleLR().getTarget() * -1);
		if (axis == Axis.Z)
			getScaleFB().target(getScaleFB().getTarget() * -1);
	}

	public void rotate90(boolean clockwise) {
		rotation.target(rotation.getTarget() + (clockwise ? -90 : 90));
	}

	public void move(float xIn, float yIn, float zIn) {
		moveTo(x.getTarget() + xIn, y.getTarget() + yIn, z.getTarget() + zIn);
	}

	public void startAt(BlockPos pos) {
		x.start(pos.getX());
		y.start(0);
		z.start(pos.getZ());
		moveTo(pos);
	}
	
	public void moveTo(BlockPos pos) {
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}

	public void moveTo(float xIn, float yIn, float zIn) {
		x.target(xIn);
		y.target(yIn);
		z.target(zIn);
	}

	public InterpolatedChasingValue getScaleFB() {
		return scaleFrontBack;
	}

	public InterpolatedChasingValue getScaleLR() {
		return scaleLeftRight;
	}

}

package com.simibubi.kinetic_api.content.schematics.client.tools;

import afj;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.content.schematics.client.SchematicTransformation;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.outliner.AABBOutline;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.timer.Timer;

public class DeployTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		selectionRange = -1;
	}

	@Override
	public void updateSelection() {
		if (schematicHandler.isActive() && selectionRange == -1) {
			selectionRange = (int) (schematicHandler.getBounds()
				.f()
				.f() / 2);
			selectionRange = afj.a(selectionRange, 1, 100);
		}
		selectIgnoreBlocks = AllKeys.ACTIVATE_TOOL.isPressed();
		super.updateSelection();
	}

	@Override
	public void renderTool(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		super.renderTool(ms, buffer);

		if (selectedPos == null)
			return;

		ms.a();
		float pt = KeyBinding.B()
			.ai();
		double x = afj.d(pt, lastChasingSelectedPos.entity, chasingSelectedPos.entity);
		double y = afj.d(pt, lastChasingSelectedPos.c, chasingSelectedPos.c);
		double z = afj.d(pt, lastChasingSelectedPos.d, chasingSelectedPos.d);

		SchematicTransformation transformation = schematicHandler.getTransformation();
		Timer bounds = schematicHandler.getBounds();
		EntityHitResult center = bounds.f();
		EntityHitResult rotationOffset = transformation.getRotationOffset(true);
		int centerX = (int) center.entity;
		int centerZ = (int) center.d;
		double xOrigin = bounds.b() / 2f;
		double zOrigin = bounds.d() / 2f;
		EntityHitResult origin = new EntityHitResult(xOrigin, 0, zOrigin);

		ms.a(x - centerX, y, z - centerZ);
		MatrixStacker.of(ms)
			.translate(origin)
			.translate(rotationOffset)
			.rotateY(transformation.getCurrentRotation())
			.translateBack(rotationOffset)
			.translateBack(origin);

		AABBOutline outline = schematicHandler.getOutline();
		outline.render(ms, buffer);
		outline.getParams()
			.clearTextures();
		ms.b();
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		if (!selectIgnoreBlocks)
			return super.handleMouseWheel(delta);
		selectionRange += delta;
		selectionRange = afj.a(selectionRange, 1, 100);
		return true;
	}

	@Override
	public boolean handleRightClick() {
		if (selectedPos == null)
			return super.handleRightClick();
		EntityHitResult center = schematicHandler.getBounds()
			.f();
		BlockPos target = selectedPos.add(-((int) center.entity), 0, -((int) center.d));

		ItemCooldownManager item = schematicHandler.getActiveSchematicItem();
		if (item != null) {
			item.o()
				.putBoolean("Deployed", true);
			item.o()
				.put("Anchor", NbtHelper.fromBlockPos(target));
		}

		schematicHandler.getTransformation()
			.moveTo(target);
		schematicHandler.markDirty();
		schematicHandler.deploy();
		return true;
	}

}

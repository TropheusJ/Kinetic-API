package com.simibubi.create.content.logistics.block.chute;

import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.MatrixStacker;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.util.math.Direction;

public class ChuteRenderer extends SafeTileEntityRenderer<ChuteTileEntity> {

	public ChuteRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(ChuteTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		if (te.item.a())
			return;
		PistonHandler blockState = te.p();
		if (blockState.c(ChuteBlock.FACING) != Direction.DOWN)
			return;
		if (blockState.c(ChuteBlock.SHAPE) != Shape.WINDOW
			&& (te.bottomPullDistance == 0 || te.itemPosition.get(partialTicks) > .5f))
			return;

		HorseEntityRenderer itemRenderer = KeyBinding.B()
			.ac();
		MatrixStacker msr = MatrixStacker.of(ms);
		ms.a();
		msr.centre();
		float itemScale = .5f;
		float itemPosition = te.itemPosition.get(partialTicks);
		ms.a(0, -.5 + itemPosition, 0);
		ms.a(itemScale, itemScale, itemScale);
		msr.rotateX(itemPosition * 180);
		msr.rotateY(itemPosition * 180);
		itemRenderer.a(te.item, b.i, light, overlay, ms, buffer);
		ms.b();
	}

}

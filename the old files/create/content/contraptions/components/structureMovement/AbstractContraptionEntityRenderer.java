package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import afj;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.block.entity.EndGatewayBlockEntityRenderer;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.client.render.entity.DragonFireballEntityRenderer;
import net.minecraft.util.Identifier;

public abstract class AbstractContraptionEntityRenderer<C extends AbstractContraptionEntity> extends DragonFireballEntityRenderer<C> {

	protected AbstractContraptionEntityRenderer(DolphinEntityRenderer p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public Identifier getEntityTexture(C p_110775_1_) {
		return null;
	}

	protected abstract void transform(C contraptionEntity, float partialTicks, BufferVertexConsumer[] matrixStacks);

	@Override
	public boolean shouldRender(C entity, EndGatewayBlockEntityRenderer p_225626_2_, double p_225626_3_, double p_225626_5_,
		double p_225626_7_) {
		if (!super.a(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;
		if (!entity.aW())
			return false;
		if (entity.getContraption() == null)
			return false;
		return true;
	}
	
	@Override
	public void render(C entity, float yaw, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffers,
		int overlay) {
		super.a(entity, yaw, partialTicks, ms, buffers, overlay);

		// Keep a copy of the transforms in order to determine correct lighting
		BufferVertexConsumer msLocal = getLocalTransform(entity);
		BufferVertexConsumer[] matrixStacks = new BufferVertexConsumer[] { ms, msLocal };

		ms.a();
		transform(entity, partialTicks, matrixStacks);
		Contraption contraption = entity.getContraption();
		if (contraption != null)
			ContraptionRenderer.render(entity.l, contraption, ms, msLocal, buffers);
		ms.b();

	}

	protected BufferVertexConsumer getLocalTransform(AbstractContraptionEntity entity) {
		double pt = KeyBinding.B()
			.ai();
		BufferVertexConsumer matrixStack = new BufferVertexConsumer();
		double x = afj.d(pt, entity.D, entity.cC());
		double y = afj.d(pt, entity.E, entity.cD());
		double z = afj.d(pt, entity.F, entity.cG());
		matrixStack.a(x, y, z);
		return matrixStack;
	}

}

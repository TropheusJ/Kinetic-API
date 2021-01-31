package com.simibubi.kinetic_api.content.contraptions.components.actors;

import java.util.function.Consumer;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PortableStorageInterfaceRenderer extends SafeTileEntityRenderer<PortableStorageInterfaceTileEntity> {

	public PortableStorageInterfaceRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(PortableStorageInterfaceTileEntity te, float partialTicks, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		PistonHandler blockState = te.p();
		float progress = te.getExtensionDistance(partialTicks);
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		render(blockState, progress, te.isConnected(), sbb -> sbb.light(light)
			.renderInto(ms, vb), ms);
	}

	public static void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		PistonHandler blockState = context.state;
		PortableStorageInterfaceTileEntity te = getTargetPSI(context);
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		float renderPartialTicks = KeyBinding.B()
			.ai();

		float progress = 0;
		boolean lit = false;
		if (te != null) {
			progress = te.getExtensionDistance(renderPartialTicks);
			lit = te.isConnected();
		}

		render(blockState, progress, lit, sbb -> sbb.light(msLocal.c()
			.a())
			.renderInto(ms, vb), ms, msLocal);
	}

	protected static PortableStorageInterfaceTileEntity getTargetPSI(MovementContext context) {
		String _workingPos_ = PortableStorageInterfaceMovement._workingPos_;
		if (!context.contraption.stalled || !context.data.contains(_workingPos_))
			return null;

		BlockPos pos = NbtHelper.toBlockPos(context.data.getCompound(_workingPos_));
		BeehiveBlockEntity tileEntity = context.world.c(pos);
		if (!(tileEntity instanceof PortableStorageInterfaceTileEntity))
			return null;

		PortableStorageInterfaceTileEntity psi = (PortableStorageInterfaceTileEntity) tileEntity;
		if (!psi.isTransferring())
			return null;
		return psi;
	}

	private static void render(PistonHandler blockState, float progress, boolean lit,
		Consumer<SuperByteBuffer> drawCallback, BufferVertexConsumer... matrixStacks) {
		for (BufferVertexConsumer ms : matrixStacks)
			ms.a();

		SuperByteBuffer middle = getMiddleForState(blockState, lit).renderOn(blockState);
		SuperByteBuffer top = getTopForState(blockState).renderOn(blockState);

		Direction facing = blockState.c(PortableStorageInterfaceBlock.SHAPE);
		for (BufferVertexConsumer ms : matrixStacks)
			MatrixStacker.of(ms)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.unCentre();

		for (BufferVertexConsumer ms : matrixStacks) {
			ms.a(0, progress / 2f, 0);
			ms.a();
			ms.a(0, 6 / 16f, 0);
		}

		drawCallback.accept(middle);

		for (BufferVertexConsumer ms : matrixStacks) {
			ms.b();
			ms.a(0, progress / 2f, 0);
		}

		drawCallback.accept(top);

		for (BufferVertexConsumer ms : matrixStacks)
			ms.b();
	}

	static AllBlockPartials getMiddleForState(PistonHandler state, boolean lit) {
		if (AllBlocks.PORTABLE_FLUID_INTERFACE.has(state))
			return lit ? AllBlockPartials.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED
				: AllBlockPartials.PORTABLE_FLUID_INTERFACE_MIDDLE;
		return lit ? AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED
			: AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE;
	}

	static AllBlockPartials getTopForState(PistonHandler state) {
		if (AllBlocks.PORTABLE_FLUID_INTERFACE.has(state))
			return AllBlockPartials.PORTABLE_FLUID_INTERFACE_TOP;
		return AllBlockPartials.PORTABLE_STORAGE_INTERFACE_TOP;
	}

}

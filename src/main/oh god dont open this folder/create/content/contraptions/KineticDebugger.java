package com.simibubi.create.content.contraptions;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;

public class KineticDebugger {

	public static void tick() {
		if (!isActive()) {
			if (KineticTileEntityRenderer.rainbowMode) {
				KineticTileEntityRenderer.rainbowMode = false;
				CreateClient.bufferCache.invalidate();
			}			
			return;
		}
		
		KineticTileEntity te = getSelectedTE();
		if (te == null)
			return;

		GameMode world = KeyBinding.B().r;
		BlockPos toOutline = te.hasSource() ? te.source : te.o();
		PistonHandler state = te.p();
		VoxelShapes shape = world.d_(toOutline)
			.l(world, toOutline);

		if (te.getTheoreticalSpeed() != 0 && !shape.b())
			CreateClient.outliner.chaseAABB("kineticSource", shape.a()
				.a(toOutline))
				.lineWidth(1 / 16f)
				.colored(te.hasSource() ? ColorHelper.colorFromLong(te.network) : 0xffcc00);

		if (state.b() instanceof IRotate) {
			Axis axis = ((IRotate) state.b()).getRotationAxis(state);
			EntityHitResult vec = EntityHitResult.b(Direction.get(AxisDirection.POSITIVE, axis)
				.getVector());
			EntityHitResult center = VecHelper.getCenterOf(te.o());
			CreateClient.outliner.showLine("rotationAxis", center.e(vec), center.d(vec))
				.lineWidth(1 / 16f);
		}

	}

	public static boolean isActive() {
		return KeyBinding.B().k.aG && AllConfigs.CLIENT.rainbowDebug.get();
	}

	public static KineticTileEntity getSelectedTE() {
		Box obj = KeyBinding.B().v;
		DragonHeadEntityModel world = KeyBinding.B().r;
		if (obj == null)
			return null;
		if (world == null)
			return null;
		if (!(obj instanceof dcg))
			return null;

		dcg ray = (dcg) obj;
		BeehiveBlockEntity te = world.c(ray.a());
		if (!(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}

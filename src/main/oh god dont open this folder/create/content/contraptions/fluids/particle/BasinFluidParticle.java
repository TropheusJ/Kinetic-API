package com.simibubi.create.content.contraptions.fluids.particle;

import afj;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.AoMode;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraftforge.fluids.FluidStack;

public class BasinFluidParticle extends FluidStackParticle {

	BlockPos basinPos;
	EntityHitResult targetPos;
	EntityHitResult centerOfBasin;
	float yOffset;

	public BasinFluidParticle(DragonHeadEntityModel world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, fluid, x, y, z, vx, vy, vz);
		u = 0;
		j = 0;
		k = 0;
		l = 0;
		yOffset = world.t.nextFloat() * 1 / 32f;
		h += yOffset;
		B = 0;
		t = 60;
		EntityHitResult currentPos = new EntityHitResult(g, h, i);
		basinPos = new BlockPos(currentPos);
		centerOfBasin = VecHelper.getCenterOf(basinPos);

		if (vx != 0) {
			t = 20;
			EntityHitResult centerOf = VecHelper.getCenterOf(basinPos);
			EntityHitResult diff = currentPos.d(centerOf)
				.d(1, 0, 1)
				.d()
				.a(.375);
			targetPos = centerOf.e(diff);
			d = g = centerOfBasin.entity;
			f = i = centerOfBasin.d;
		}
	}

	@Override
	public void clearAtlas() {
		super.clearAtlas();
		B = targetPos != null ? Math.max(1 / 32f, ((1f * s) / t) / 8)
			: 1 / 8f * (1 - ((Math.abs(s - (t / 2)) / (1f * t))));

		if (s % 2 == 0) {
			if (!AllBlocks.BASIN.has(c.d_(basinPos))) {
				j();
				return;
			}

			BeehiveBlockEntity tileEntity = c.c(basinPos);
			if (tileEntity instanceof BasinTileEntity) {
				float totalUnits = ((BasinTileEntity) tileEntity).getTotalFluidUnits(0);
				if (totalUnits < 1)
					totalUnits = 0;
				float fluidLevel = afj.a(totalUnits / 2000, 0, 1);
				h = 2 / 16f + basinPos.getY() + 12 / 16f * fluidLevel + yOffset;
			}

		}

		if (targetPos != null) {
			float progess = (1f * s) / t;
			EntityHitResult currentPos = centerOfBasin.e(targetPos.d(centerOfBasin)
				.a(progess));
			g = currentPos.entity;
			i = currentPos.d;
		}
	}

	@Override
	public void a(OverlayVertexConsumer vb, AoMode info, float pt) {
		Quaternion rotation = info.f();
		Quaternion prevRotation = new Quaternion(rotation);
		rotation.set(1, 0, 0, 1);
		rotation.normalize();
		super.a(vb, info, pt);
		rotation.set(0, 0, 0, 1);
		rotation.hamiltonProduct(prevRotation);
	}

	@Override
	protected boolean canEvaporate() {
		return false;
	}

}

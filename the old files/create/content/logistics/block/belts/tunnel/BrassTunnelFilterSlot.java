package com.simibubi.kinetic_api.content.logistics.block.belts.tunnel;

import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.util.hit.EntityHitResult;

public class BrassTunnelFilterSlot extends ValueBoxTransform.Sided {

	@Override
	protected EntityHitResult getSouthLocation() {
		return VecHelper.voxelSpace(8, 13, 15.5f);
	}

}

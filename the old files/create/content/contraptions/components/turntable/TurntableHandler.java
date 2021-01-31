package com.simibubi.kinetic_api.content.contraptions.components.turntable;

import afj;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class TurntableHandler {

	public static void gameRenderTick() {
		KeyBinding mc = KeyBinding.B();
		BlockPos pos = mc.s.cA();

		if (!AllBlocks.TURNTABLE.has(mc.r.d_(pos)))
			return;
		if (!mc.s.an())
			return;
		if (mc.S())
			return;

		BeehiveBlockEntity tileEntity = mc.r.c(pos);
		if (!(tileEntity instanceof TurntableTileEntity))
			return;
		
		TurntableTileEntity turnTable = (TurntableTileEntity) tileEntity;
		float speed = turnTable.getSpeed() * 3/10;

		if (speed == 0)
			return;
		
		EntityHitResult origin = VecHelper.getCenterOf(pos);
		EntityHitResult offset = mc.s.cz().d(origin);
		
		if (offset.f() > 1/4f)
			speed *= afj.a((1/2f - offset.f()) * 2, 0, 1);

		mc.s.p = mc.s.r - speed * mc.ai();
		mc.s.aA = mc.s.p;
	}

}

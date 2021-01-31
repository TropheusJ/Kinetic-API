package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue;

import afj;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ScrollValueHandler {

	@Environment(EnvType.CLIENT)
	public static boolean onScroll(double delta) {
		Box objectMouseOver = KeyBinding.B().v;
		if (!(objectMouseOver instanceof dcg))
			return false;

		dcg result = (dcg) objectMouseOver;
		KeyBinding mc = KeyBinding.B();
		DragonHeadEntityModel world = mc.r;
		BlockPos blockPos = result.a();

		ScrollValueBehaviour scrolling = TileEntityBehaviour.get(world, blockPos, ScrollValueBehaviour.TYPE);
		if (scrolling == null)
			return false;
		if (!mc.s.eJ())
			return false;
		if (scrolling.needsWrench && !AllItems.WRENCH.isIn(mc.s.dC()))
			return false;
		if (scrolling.slotPositioning instanceof Sided)
			((Sided) scrolling.slotPositioning).fromSide(result.b());
		if (!scrolling.testHit(objectMouseOver.e()))
			return false;

		if (scrolling instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) scrolling;
			for (SmartTileEntity te : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = te.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					applyTo(delta, other);
			}

		} else
			applyTo(delta, scrolling);

		return true;
	}

	protected static void applyTo(double delta, ScrollValueBehaviour scrolling) {
		scrolling.ticksUntilScrollPacket = 10;
		int valueBefore = scrolling.scrollableValue;

		StepContext context = new StepContext();
		context.control = AllKeys.ctrlDown();
		context.shift = AllKeys.shiftDown();
		context.currentValue = scrolling.scrollableValue;
		context.forward = delta > 0;

		double newValue = scrolling.scrollableValue + Math.signum(delta) * scrolling.step.apply(context);
		scrolling.scrollableValue = (int) afj.a(newValue, scrolling.min, scrolling.max);

		if (valueBefore != scrolling.scrollableValue)
			scrolling.clientCallback.accept(scrolling.scrollableValue);
	}

}

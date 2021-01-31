package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllKeys;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBox.IconValueBox;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBox.TextValueBox;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;

public class ScrollValueRenderer {

	public static void tick() {
		KeyBinding mc = KeyBinding.B();
		Box target = mc.v;
		if (target == null || !(target instanceof dcg))
			return;

		dcg result = (dcg) target;
		DragonHeadEntityModel world = mc.r;
		BlockPos pos = result.a();
		Direction face = result.b();

		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(world, pos, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.needsWrench && !AllItems.WRENCH.isIn(KeyBinding.B().s.dC()))
			return;
		boolean highlight = behaviour.testHit(target.e());

		if (behaviour instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) behaviour;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = smartTileEntity.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					addBox(world, smartTileEntity.o(), face, other, highlight);
			}
		} else
			addBox(world, pos, face, behaviour, highlight);
	}

	protected static void addBox(DragonHeadEntityModel world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
		boolean highlight) {
		Timer bb = new Timer(EntityHitResult.a, EntityHitResult.a).g(.5f)
			.a(0, 0, -.5f)
			.d(0, 0, -.125f);
		Text label = behaviour.label;
		ValueBox box;

		if (behaviour instanceof ScrollOptionBehaviour) {
			box = new IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb, pos);
		} else {
			box = new TextValueBox(label, bb, pos, new LiteralText(behaviour.formatValue()));
			if (behaviour.unit != null)
				box.subLabel(new LiteralText("(").append(behaviour.unit.apply(behaviour.scrollableValue)).append(")"));
		}

		box.scrollTooltip(new LiteralText("[").append(Lang.translate("action.scroll")).append("]"));
		box.offsetLabel(behaviour.textShift.b(20, -10, 0))
			.withColors(0x5A5D5A, 0xB5B7B6)
			.passive(!highlight);

		CreateClient.outliner.showValueBox(pos, box.transform(behaviour.slotPositioning))
			.lineWidth(1 / 64f)
			.highlightFace(face);
	}

}

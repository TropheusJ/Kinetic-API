package com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering;

import com.simibubi.kinetic_api.AllSpecialTextures;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.logistics.item.filter.FilterItem;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBox.ItemValueBox;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import dcg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;

public class FilteringRenderer {

	public static void tick() {
		KeyBinding mc = KeyBinding.B();
		Box target = mc.v;
		if (target == null || !(target instanceof dcg))
			return;

		dcg result = (dcg) target;
		DragonHeadEntityModel world = mc.r;
		BlockPos pos = result.a();
		PistonHandler state = world.d_(pos);

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (mc.s.bt())
			return;
		if (behaviour == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(result.b());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(result.b());
		if (!behaviour.slotPositioning.shouldRender(state))
			return;

		ItemCooldownManager filter = behaviour.getFilter();
		boolean isFilterSlotted = filter.b() instanceof FilterItem;
		boolean showCount = behaviour.isCountVisible();
		boolean fluids = behaviour.fluidFilter;
		Text label = isFilterSlotted ? LiteralText.EMPTY
			: Lang.translate(behaviour.recipeFilter ? "logistics.recipe_filter"
				: fluids ? "logistics.fluid_filter" : "logistics.filter");
		boolean hit = behaviour.slotPositioning.testHit(state, target.e()
			.d(EntityHitResult.b(pos)));

		Timer emptyBB = new Timer(EntityHitResult.a, EntityHitResult.a);
		Timer bb = isFilterSlotted ? emptyBB.c(.45f, .31f, .2f) : emptyBB.g(.25f);

		ValueBox box = showCount ? new ItemValueBox(label, bb, pos, filter, behaviour.scrollableValue)
			: new ValueBox(label, bb, pos);

		box.offsetLabel(behaviour.textShift)
			.withColors(fluids ? 0x407088 : 0x7A6A2C, fluids ? 0x70adb5 : 0xB79D64)
			.scrollTooltip(showCount ? new LiteralText("[").append(Lang.translate("action.scroll")).append("]") : LiteralText.EMPTY)
			.passive(!hit);

		CreateClient.outliner.showValueBox(Pair.of("filter", pos), box.transform(behaviour.slotPositioning))
			.lineWidth(1 / 64f)
			.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
			.highlightFace(result.b());
	}

	public static void renderOnTileEntity(SmartTileEntity tileEntityIn, float partialTicks, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {

		if (tileEntityIn == null || tileEntityIn.q())
			return;
		FilteringBehaviour behaviour = tileEntityIn.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (!behaviour.isActive())
			return;
		if (behaviour.getFilter()
			.a() && !(behaviour instanceof SidedFilteringBehaviour))
			return;

		ValueBoxTransform slotPositioning = behaviour.slotPositioning;
		PistonHandler blockState = tileEntityIn.p();

		if (slotPositioning instanceof ValueBoxTransform.Sided) {
			ValueBoxTransform.Sided sided = (ValueBoxTransform.Sided) slotPositioning;
			Direction side = sided.getSide();
			for (Direction d : Iterate.directions) {
				ItemCooldownManager filter = behaviour.getFilter(d);
				if (filter.a())
					continue;

				sided.fromSide(d);
				if (!slotPositioning.shouldRender(blockState))
					continue;

				ms.a();
				slotPositioning.transform(blockState, ms);
				ValueBoxRenderer.renderItemIntoValueBox(filter, ms, buffer, light, overlay);
				ms.b();
			}
			sided.fromSide(side);
			return;
		} else if (slotPositioning.shouldRender(blockState)) {
			ms.a();
			slotPositioning.transform(blockState, ms);
			ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
			ms.b();
		}
	}

}

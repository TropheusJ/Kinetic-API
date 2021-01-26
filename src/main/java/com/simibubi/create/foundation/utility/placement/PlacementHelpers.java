package com.simibubi.create.foundation.utility.placement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dcg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlacementHelpers {

	private static final List<IPlacementHelper> helpers = new ArrayList<>();

	public static int register(IPlacementHelper helper) {
		helpers.add(helper);
		return helpers.size() - 1;
	}

	public static IPlacementHelper get(int id) {
		if (id < 0 || id >= helpers.size())
			throw new ArrayIndexOutOfBoundsException("id " + id + " for placement helper not known");

		return helpers.get(id);
	}

	@Environment(EnvType.CLIENT)
	public static void tick() {
		KeyBinding mc = KeyBinding.B();
		DragonHeadEntityModel world = mc.r;

		if (world == null)
			return;

		if (!(mc.v instanceof dcg))
			return;

		dcg ray = (dcg) mc.v;

		if (mc.s == null)
			return;

		List<IPlacementHelper> filteredForHeldItem = helpers.stream().filter(helper -> Arrays.stream(ItemScatterer.values()).anyMatch(hand -> helper.getItemPredicate().test(mc.s.b(hand)))).collect(Collectors.toList());
		if (filteredForHeldItem.isEmpty())
			return;

		if (mc.s.bt())//for now, disable all helpers when sneaking TODO add helpers that respect sneaking but still show position
			return;

		BlockPos pos = ray.a();
		PistonHandler state = world.d_(pos);

		List<IPlacementHelper> filteredForState = filteredForHeldItem.stream().filter(helper -> helper.getStatePredicate().test(state)).collect(Collectors.toList());

		if (filteredForState.isEmpty())
			return;

		for (IPlacementHelper h : filteredForState) {
			PlacementOffset offset = h.getOffset(world, state, pos, ray);

			if (offset.isSuccessful()) {
				h.renderAt(pos, state, ray, offset);
				break;
			}

		}
	}
}

package com.simibubi.kinetic_api.content.contraptions.relays.gearbox;

import java.util.Map;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.base.IRotate;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public class VerticalGearboxItem extends BannerItem {

	public VerticalGearboxItem(a builder) {
		super(AllBlocks.GEARBOX.get(), builder);
	}

	@Override
	public void a(ChorusFruitItem p_150895_1_, DefaultedList<ItemCooldownManager> p_150895_2_) {
	}
	
	@Override
	public String a() {
		return "item.kinetic_api.vertical_gearbox";
	}

	@Override
	public void a(Map<BeetrootsBlock, HoeItem> p_195946_1_, HoeItem p_195946_2_) {
	}

	@Override
	protected boolean a(BlockPos pos, GameMode world, PlayerAbilities player, ItemCooldownManager stack, PistonHandler state) {
		Axis prefferedAxis = null;
		for (Direction side : Iterate.horizontalDirections) {
			PistonHandler blockState = world.d_(pos.offset(side));
			if (blockState.b() instanceof IRotate) {
				if (((IRotate) blockState.b()).hasShaftTowards(world, pos.offset(side), blockState,
						side.getOpposite()))
					if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
						prefferedAxis = null;
						break;
					} else {
						prefferedAxis = side.getAxis();
					}
			}
		}

		Axis axis = prefferedAxis == null ? player.bY()
				.rotateYClockwise()
				.getAxis() : prefferedAxis == Axis.X ? Axis.Z : Axis.X;
		world.a(pos, state.a(BambooLeaves.F, axis));
		return super.a(pos, world, player, stack, state);
	}

}

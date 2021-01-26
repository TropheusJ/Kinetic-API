package com.simibubi.create.content.contraptions.wrench;

import java.util.Optional;
import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public interface IWrenchableWithBracket extends IWrenchable {

	public Optional<ItemCooldownManager> removeBracket(MobSpawnerLogic world, BlockPos pos, boolean inOnReplacedContext);

	@Override
	default Difficulty onWrenched(PistonHandler state, bnx context) {
		if (tryRemoveBracket(context))
			return Difficulty.SUCCESS;
		return IWrenchable.super.onWrenched(state, context);
	}

	default boolean tryRemoveBracket(bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		Optional<ItemCooldownManager> bracket = removeBracket(world, pos, false);
		PistonHandler blockState = world.d_(pos);
		if (bracket.isPresent()) {
			PlayerAbilities player = context.n();
			if (!world.v && !player.b_())
				player.bm.a(world, bracket.get());
			if (!world.v && AllBlocks.FLUID_PIPE.has(blockState)) {
				Axis preferred = FluidPropagator.getStraightPipeAxis(blockState);
				Direction preferredDirection =
					preferred == null ? Direction.UP : Direction.get(AxisDirection.POSITIVE, preferred);
				PistonHandler updated = AllBlocks.FLUID_PIPE.get()
					.updateBlockState(blockState, preferredDirection, null, world, pos);
				if (updated != blockState)
					world.a(pos, updated);
			}
			return true;
		}
		return false;
	}

}

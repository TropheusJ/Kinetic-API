package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import java.util.Optional;
import bnx;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class BracketBlockItem extends BannerItem {

	public BracketBlockItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public Difficulty a(bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		PistonHandler state = world.d_(pos);
		BracketBlock bracketBlock = getBracketBlock();
		PlayerAbilities player = context.n();

		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (behaviour == null)
			return Difficulty.FAIL;
		if (!behaviour.canHaveBracket())
			return Difficulty.FAIL;
		if (world.v)
			return Difficulty.SUCCESS;

		Optional<PistonHandler> suitableBracket = bracketBlock.getSuitableBracket(state, context.j());
		if (!suitableBracket.isPresent() && player != null)
			suitableBracket =
				bracketBlock.getSuitableBracket(state, Direction.a(player)[0].getOpposite());
		if (!suitableBracket.isPresent())
			return Difficulty.SUCCESS;

		PistonHandler bracket = behaviour.getBracket();
		behaviour.applyBracket(suitableBracket.get());
		
		if (!world.v && player != null)
			behaviour.triggerAdvancements(world, player, state);
		
		if (player == null || !player.b_()) {
			context.m()
				.g(1);
			if (bracket != BellBlock.FACING.n()) {
				ItemCooldownManager returnedStack = new ItemCooldownManager(bracket.b());
				if (player == null)
					BeetrootsBlock.a(world, pos, returnedStack);
				else
					player.bm.a(world, returnedStack);
			}
		}
		return Difficulty.SUCCESS;
	}

	private BracketBlock getBracketBlock() {
		return (BracketBlock) e();
	}

}

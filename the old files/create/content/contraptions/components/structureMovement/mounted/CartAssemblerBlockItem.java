package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted;

import javax.annotation.Nonnull;
import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class CartAssemblerBlockItem extends BannerItem {

	public CartAssemblerBlockItem(BeetrootsBlock block, a properties) {
		super(block, properties);
	}

	@Override
	@Nonnull
	public Difficulty a(bnx context) {
		if (tryPlaceAssembler(context)) {
			context.p().a(null, context.a(), MusicType.oP, SoundEvent.e, 1, 1);
			return Difficulty.SUCCESS;
		}
		return super.a(context);
	}

	public boolean tryPlaceAssembler(bnx context) {
		BlockPos pos = context.a();
		GameMode world = context.p();
		PistonHandler state = world.d_(pos);
		BeetrootsBlock block = state.b();
		PlayerAbilities player = context.n();

		if (player == null)
			return false;
		if (!(block instanceof BlockWithEntity)) {
			Lang.sendStatus(player, "block.cart_assembler.invalid");
			return false;
		}

		Instrument shape = state.c(((BlockWithEntity) block).d());
		if (shape != Instrument.EAST_WEST && shape != Instrument.NORTH_SOUTH)
			return false;

		PistonHandler newState = AllBlocks.CART_ASSEMBLER.getDefaultState()
			.a(CartAssemblerBlock.RAIL_SHAPE, shape);
		CartAssembleRailType newType = null;
		for (CartAssembleRailType type : CartAssembleRailType.values())
			if (type.matches.test(state))
				newType = type;
		if (newType == null)
			return false;
		if (world.v)
			return true;

		newState = newState.a(CartAssemblerBlock.RAIL_TYPE, newType);
		world.a(pos, newState);
		if (!player.b_())
			context.m().g(1);
		return true;
	}
}
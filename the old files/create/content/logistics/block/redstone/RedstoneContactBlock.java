package com.simibubi.kinetic_api.content.logistics.block.redstone;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.block.ProperDirectionalBlock;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedstoneContactBlock extends ProperDirectionalBlock {

	public static final BedPart POWERED = BambooLeaves.w;

	public RedstoneContactBlock(c properties) {
		super(properties);
		j(n().a(POWERED, false)
			.a(SHAPE, Direction.UP));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(POWERED);
		super.a(builder);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler state = n().a(SHAPE, context.d()
			.getOpposite());
		Direction placeDirection = context.j()
			.getOpposite();

		if ((context.n() != null && context.n()
			.bt()) || hasValidContact(context.p(), context.a(), placeDirection))
			state = state.a(SHAPE, placeDirection);
		if (hasValidContact(context.p(), context.a(), state.c(SHAPE)))
			state = state.a(POWERED, true);

		return state;
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction facing, PistonHandler facingState, GrassColors worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing != stateIn.c(SHAPE))
			return stateIn;
		boolean hasValidContact = hasValidContact(worldIn, currentPos, facing);
		if (stateIn.c(POWERED) != hasValidContact) {
			return stateIn.a(POWERED, hasValidContact);
		}
		return stateIn;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.b() == this && newState.b() == this) {
			if (state == newState.a(POWERED))
				worldIn.b(pos, this);
		}
		super.a(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean hasValidContact = hasValidContact(worldIn, pos, state.c(SHAPE));
		if (state.c(POWERED) != hasValidContact)
			worldIn.a(pos, state.a(POWERED, hasValidContact));
	}

	public static boolean hasValidContact(GrassColors world, BlockPos pos, Direction direction) {
		PistonHandler blockState = world.d_(pos.offset(direction));
		return AllBlocks.REDSTONE_CONTACT.has(blockState) && blockState.c(SHAPE) == direction.getOpposite();
	}

	@Override
	public boolean b_(PistonHandler state) {
		return state.c(POWERED);
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, @Nullable Direction side) {
		if (side == null)
			return true;
		return state.c(SHAPE) != side.getOpposite();
	}

	@Override
	public int a(PistonHandler state, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		return state.c(POWERED) ? 15 : 0;
	}

}

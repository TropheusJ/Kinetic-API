package com.simibubi.kinetic_api.content.logistics.block.redstone;

import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class NixieTubeBlock extends HayBlock implements ITE<NixieTubeTileEntity> {

	public static final BedPart CEILING = BedPart.a("ceiling");

	public NixieTubeBlock(c properties) {
		super(properties);
		j(n().a(CEILING, false));
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg ray) {
		try {

			ItemCooldownManager heldItem = player.b(hand);
			NixieTubeTileEntity nixie = getTileEntity(world, pos);

			if (player.bt())
				return Difficulty.PASS;

			if (heldItem.a()) {
				if (nixie.reactsToRedstone())
					return Difficulty.PASS;
				nixie.clearCustomText();
				updateDisplayedRedstoneValue(state, world, pos);
				return Difficulty.SUCCESS;
			}

			if (heldItem.b() == AliasedBlockItem.pI && heldItem.t()) {
				Direction left = state.c(aq)
					.rotateYClockwise();
				Direction right = left.getOpposite();

				if (world.v)
					return Difficulty.SUCCESS;

				BlockPos currentPos = pos;
				while (true) {
					BlockPos nextPos = currentPos.offset(left);
					if (world.d_(nextPos) != state)
						break;
					currentPos = nextPos;
				}

				int index = 0;

				while (true) {
					final int rowPosition = index;
					withTileEntityDo(world, currentPos, te -> te.displayCustomNameOf(heldItem, rowPosition));
					BlockPos nextPos = currentPos.offset(right);
					if (world.d_(nextPos) != state)
						break;
					currentPos = nextPos;
					index++;
				}
			}

		} catch (TileEntityException e) {
		}

		return Difficulty.PASS;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(CEILING, aq));
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return (state.c(CEILING) ? AllShapes.NIXIE_TUBE_CEILING : AllShapes.NIXIE_TUBE)
			.get(state.c(aq)
				.getAxis());
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		BlockPos pos = context.a();
		boolean ceiling = context.j() == Direction.DOWN;
		EntityHitResult hitVec = context.k();
		if (hitVec != null)
			ceiling = hitVec.c - pos.getY() > .5f;
		return n().a(aq, context.f()
			.getOpposite())
			.a(CEILING, ceiling);
	}

	@Override
	public void a(PistonHandler p_220069_1_, GameMode p_220069_2_, BlockPos p_220069_3_, BeetrootsBlock p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		updateDisplayedRedstoneValue(p_220069_1_, p_220069_2_, p_220069_3_);
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		updateDisplayedRedstoneValue(state, worldIn, pos);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return new NixieTubeTileEntity(AllTileEntities.NIXIE_TUBE.get());
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	private void updateDisplayedRedstoneValue(PistonHandler state, GameMode worldIn, BlockPos pos) {
		if (worldIn.v)
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.reactsToRedstone())
				te.displayRedstoneStrength(getPower(worldIn, pos));
		});
	}

	static boolean isValidBlock(MobSpawnerLogic world, BlockPos pos, boolean above) {
		PistonHandler state = world.d_(pos.up(above ? 1 : -1));
		return !state.j(world, pos)
			.b();
	}

	private int getPower(GameMode worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.b(pos.offset(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.b(pos.offset(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public Class<NixieTubeTileEntity> getTileEntityClass() {
		return NixieTubeTileEntity.class;
	}

}

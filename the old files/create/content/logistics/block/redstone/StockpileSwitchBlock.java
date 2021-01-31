package com.simibubi.kinetic_api.content.logistics.block.redstone;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.gui.ScreenOpener;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.HayBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;

public class StockpileSwitchBlock extends HayBlock implements ITE<StockpileSwitchTileEntity>, IWrenchable {

	public static final DoubleBlockHalf INDICATOR = DoubleBlockHalf.of("indicator", 0, 6);

	public StockpileSwitchBlock(c p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public void b(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		updateObservedInventory(state, worldIn, pos);
	}

	@Override
	public void onNeighborChange(PistonHandler state, ItemConvertible world, BlockPos pos, BlockPos neighbor) {
		if (world.s_())
			return;
		if (!isObserving(state, pos, neighbor))
			return;
		updateObservedInventory(state, world, pos);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.STOCKPILE_SWITCH.get(state.c(aq));
	}

	private void updateObservedInventory(PistonHandler state, ItemConvertible world, BlockPos pos) {
		withTileEntityDo(world, pos, StockpileSwitchTileEntity::updateCurrentLevel);
	}

	private boolean isObserving(PistonHandler state, BlockPos pos, BlockPos observing) {
		return observing.equals(pos.offset(state.c(aq)));
	}

	@Override
	public boolean canConnectRedstone(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction side) {
		return side != null && side.getOpposite() != state.c(aq);
	}

	@Override
	public boolean b_(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, MobSpawnerLogic blockAccess, BlockPos pos, Direction side) {
		if (side == blockState.c(aq).getOpposite())
			return 0;
		try {
			return getTileEntity(blockAccess, pos).isPowered() ? 15 : 0;
		} catch (TileEntityException e) {
		}
		return 0;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(aq, INDICATOR);
		super.a(builder);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (player != null && AllItems.WRENCH.isIn(player.b(handIn)))
			return Difficulty.PASS;
		DistExecutor.runWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(worldIn, pos, te -> this.displayScreen(te, player)));
		return Difficulty.SUCCESS;
	}

	@Environment(EnvType.CLIENT)
	protected void displayScreen(StockpileSwitchTileEntity te, PlayerAbilities player) {
		if (player instanceof FishingParticle)
			ScreenOpener.open(new StockpileSwitchScreen(te));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler state = n();

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BeehiveBlockEntity te = context.p()
				.c(context.a()
					.offset(face));
			if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.isPresent())
				if (preferredFacing == null)
					preferredFacing = face;
				else {
					preferredFacing = null;
					break;
				}
		}

		if (preferredFacing != null) {
			state = state.a(aq, preferredFacing);
		} else if (context.j()
			.getAxis()
			.isHorizontal()) {
			state = state.a(aq, context.j());
		} else {
			state = state.a(aq, context.f()
				.getOpposite());
		}

		return state;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.STOCKPILE_SWITCH.create();
	}

	@Override
	public Class<StockpileSwitchTileEntity> getTileEntityClass() {
		return StockpileSwitchTileEntity.class;
	}

}

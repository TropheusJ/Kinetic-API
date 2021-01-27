package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import bnx;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock implements ITE<SequencedGearshiftTileEntity> {

	public static final BedPart VERTICAL = BedPart.a("vertical");
	public static final DoubleBlockHalf STATE = DoubleBlockHalf.of("state", 0, 5);

	public SequencedGearshiftBlock(c properties) {
		super(properties);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder.a(STATE, VERTICAL));
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.SEQUENCED_GEARSHIFT.create();
	}

	@Override
	public boolean shouldCheckWeakPower(PistonHandler state, ItemConvertible world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.v)
			return;

		boolean previouslyPowered = state.c(STATE) != 0;
		if (previouslyPowered != worldIn.r(pos))
			withTileEntityDo(worldIn, pos, SequencedGearshiftTileEntity::onRedstoneUpdate);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		if (state.c(VERTICAL))
			return face.getAxis()
				.isVertical();
		return super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		ItemCooldownManager held = player.dC();
		if (AllItems.WRENCH.isIn(held))
			return Difficulty.PASS;
		if (held.b() instanceof BannerItem) {
			BannerItem blockItem = (BannerItem) held.b();
			if (blockItem.e() instanceof KineticBlock && hasShaftTowards(worldIn, pos, state, hit.b()))
				return Difficulty.PASS;
		}

		DistExecutor.runWhenOn(Dist.CLIENT,
			() -> () -> withTileEntityDo(worldIn, pos, te -> this.displayScreen(te, player)));
		return Difficulty.SUCCESS;
	}

	@Environment(EnvType.CLIENT)
	protected void displayScreen(SequencedGearshiftTileEntity te, PlayerAbilities player) {
		if (player instanceof FishingParticle)
			ScreenOpener.open(new SequencedGearshiftScreen(te));
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
		if (preferredAxis != null && (context.n() == null || !context.n()
			.bt()))
			return withAxis(preferredAxis, context);
		return withAxis(context.d()
			.getAxis(), context);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		PistonHandler newState = state;

		if (context.j()
			.getAxis() != Axis.Y)
			if (newState.c(HORIZONTAL_AXIS) != context.j()
				.getAxis())
				newState = newState.a(VERTICAL);

		return super.onWrenched(newState, context);
	}

	private PistonHandler withAxis(Axis axis, PotionUtil context) {
		PistonHandler state = n().a(VERTICAL, axis.isVertical());
		if (axis.isVertical())
			return state.a(HORIZONTAL_AXIS, context.f()
				.getAxis());
		return state.a(HORIZONTAL_AXIS, axis);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		if (state.c(VERTICAL))
			return Axis.Y;
		return super.getRotationAxis(state);
	}

	@Override
	public Class<SequencedGearshiftTileEntity> getTileEntityClass() {
		return SequencedGearshiftTileEntity.class;
	}
	
	@Override
	public boolean a(PistonHandler p_149740_1_) {
		return true;
	}
	
	@Override
	public int a(PistonHandler state, GameMode world, BlockPos pos) {
		return state.c(STATE).intValue();
	}

}

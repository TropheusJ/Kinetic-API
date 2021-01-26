package com.simibubi.create.content.contraptions.fluids.tank;

import afj;
import apx;
import bnx;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;
import com.simibubi.create.foundation.utility.Lang;
import cut;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.SnowyBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidTankBlock extends BeetrootsBlock implements IWrenchable, ITE<FluidTankTileEntity> {

	public static final BedPart TOP = BedPart.a("top");
	public static final BedPart BOTTOM = BedPart.a("bottom");
	public static final DirectionProperty<Shape> SHAPE = DirectionProperty.a("shape", Shape.class);

	private boolean creative;

	public static FluidTankBlock regular(c p_i48440_1_) {
		return new FluidTankBlock(p_i48440_1_, false);
	}

	public static FluidTankBlock creative(c p_i48440_1_) {
		return new FluidTankBlock(p_i48440_1_, true);
	}

	protected FluidTankBlock(c p_i48440_1_, boolean creative) {
		super(p_i48440_1_);
		this.creative = creative;
		j(n().a(TOP, true)
			.a(BOTTOM, true)
			.a(SHAPE, Shape.WINDOW));
	}

	public static boolean isTank(PistonHandler state) {
		return state.b() instanceof FluidTankBlock;
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler oldState, boolean moved) {
		if (oldState.b() == state.b())
			return;
		if (moved)
			return;
		withTileEntityDo(world, pos, FluidTankTileEntity::updateConnectivity);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		p_206840_1_.a(TOP, BOTTOM, SHAPE);
	}

	@Override
	public int getLightValue(PistonHandler state, MobSpawnerLogic world, BlockPos pos) {
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, pos);
		if (tankAt == null)
			return 0;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null || !controllerTE.window)
			return 0;
		return tankAt.luminosity;
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		withTileEntityDo(context.p(), context.a(), FluidTankTileEntity::toggleWindows);
		return Difficulty.SUCCESS;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg ray) {
		ItemCooldownManager heldItem = player.b(hand);
		boolean onClient = world.v;

		if (heldItem.a())
			return Difficulty.PASS;
		if (!player.b_())
			return Difficulty.PASS;

		FluidExchange exchange = null;
		FluidTankTileEntity te = FluidTankConnectivityHandler.anyTankAt(world, pos);
		if (te == null)
			return Difficulty.FAIL;

		LazyOptional<IFluidHandler> tankCapability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		if (!tankCapability.isPresent())
			return Difficulty.PASS;
		IFluidHandler fluidTank = tankCapability.orElse(null);
		FluidStack prevFluidInTank = fluidTank.getFluidInTank(0)
			.copy();

		if (FluidHelper.tryEmptyItemIntoTE(world, player, hand, heldItem, te))
			exchange = FluidExchange.ITEM_TO_TANK;
		else if (FluidHelper.tryFillItemFromTE(world, player, hand, heldItem, te))
			exchange = FluidExchange.TANK_TO_ITEM;

		if (exchange == null) {
			if (EmptyingByBasin.canItemBeEmptied(world, heldItem)
				|| GenericItemFilling.canItemBeFilled(world, heldItem))
				return Difficulty.SUCCESS;
			return Difficulty.PASS;
		}

		MusicSound soundevent = null;
		PistonHandler fluidState = null;
		FluidStack fluidInTank = tankCapability.map(fh -> fh.getFluidInTank(0))
			.orElse(FluidStack.EMPTY);

		if (exchange == FluidExchange.ITEM_TO_TANK) {
			if (creative && !onClient) {
				FluidStack fluidInItem = EmptyingByBasin.emptyItem(world, heldItem, true)
					.getFirst();
				if (!fluidInItem.isEmpty() && fluidTank instanceof CreativeSmartFluidTank)
					((CreativeSmartFluidTank) fluidTank).setContainedFluid(fluidInItem);
			}

			cut fluid = fluidInTank.getFluid();
			fluidState = fluid.h()
				.g();
			FluidAttributes attributes = fluid.getAttributes();
			soundevent = attributes.getEmptySound();
			if (soundevent == null)
				soundevent =
					fluid.a(BlockTags.field_15471) ? MusicType.bl : MusicType.bj;
		}
		if (exchange == FluidExchange.TANK_TO_ITEM) {
			if (creative && !onClient)
				if (fluidTank instanceof CreativeSmartFluidTank)
					((CreativeSmartFluidTank) fluidTank).setContainedFluid(FluidStack.EMPTY);

			cut fluid = prevFluidInTank.getFluid();
			fluidState = fluid.h()
				.g();
			soundevent = fluid.getAttributes()
				.getFillSound();
			if (soundevent == null)
				soundevent =
					fluid.a(BlockTags.field_15471) ? MusicType.bo : MusicType.bm;
		}

		if (soundevent != null && !onClient) {
			float pitch = afj
				.a(1 - (1f * fluidInTank.getAmount() / (FluidTankTileEntity.getCapacityMultiplier() * 16)), 0, 1);
			pitch /= 1.5f;
			pitch += .5f;
			pitch += (world.t.nextFloat() - .5f) / 4f;
			world.a(null, pos, soundevent, SoundEvent.e, .5f, pitch);
		}

		if (!fluidInTank.isFluidStackIdentical(prevFluidInTank)) {
			if (te instanceof FluidTankTileEntity) {
				FluidTankTileEntity controllerTE = ((FluidTankTileEntity) te).getControllerTE();
				if (controllerTE != null) {
					if (fluidState != null && onClient) {
						BlockStateParticleEffect blockParticleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, fluidState);
						float level = (float) fluidInTank.getAmount() / fluidTank.getTankCapacity(0);

						boolean reversed = fluidInTank.getFluid()
							.getAttributes()
							.isLighterThanAir();
						if (reversed)
							level = 1 - level;

						EntityHitResult vec = ray.e();
						vec = new EntityHitResult(vec.entity, controllerTE.o()
							.getY() + level * (controllerTE.height - .5f) + .25f, vec.d);
						EntityHitResult motion = player.cz()
							.d(vec)
							.a(1 / 20f);
						vec = vec.e(motion);
						world.addParticle(blockParticleData, vec.entity, vec.c, vec.d, motion.entity, motion.c, motion.d);
						return Difficulty.SUCCESS;
					}

					controllerTE.sendDataImmediately();
					controllerTE.X_();
				}
			}
		}

		return Difficulty.SUCCESS;
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (state.hasTileEntity() && (state.b() != newState.b() || !newState.hasTileEntity())) {
			BeehiveBlockEntity te = world.c(pos);
			if (!(te instanceof FluidTankTileEntity))
				return;
			FluidTankTileEntity tankTE = (FluidTankTileEntity) te;
			world.o(pos);
			FluidTankConnectivityHandler.splitTank(tankTE);
		}
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return creative ? AllTileEntities.CREATIVE_FLUID_TANK.create() : AllTileEntities.FLUID_TANK.create();
	}

	@Override
	public Class<FluidTankTileEntity> getTileEntityClass() {
		return FluidTankTileEntity.class;
	}

	@Override
	public PistonHandler a(PistonHandler state, LoomBlock mirror) {
		if (mirror == LoomBlock.TITLE)
			return state;
		boolean x = mirror == LoomBlock.c;
		switch (state.c(SHAPE)) {
		case WINDOW_NE:
			return state.a(SHAPE, x ? Shape.WINDOW_NW : Shape.WINDOW_SE);
		case WINDOW_NW:
			return state.a(SHAPE, x ? Shape.WINDOW_NE : Shape.WINDOW_SW);
		case WINDOW_SE:
			return state.a(SHAPE, x ? Shape.WINDOW_SW : Shape.WINDOW_NE);
		case WINDOW_SW:
			return state.a(SHAPE, x ? Shape.WINDOW_SE : Shape.WINDOW_NW);
		default:
			return state;
		}
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rotation) {
		for (int i = 0; i < rotation.ordinal(); i++)
			state = rotateOnce(state);
		return state;
	}

	private PistonHandler rotateOnce(PistonHandler state) {
		switch (state.c(SHAPE)) {
		case WINDOW_NE:
			return state.a(SHAPE, Shape.WINDOW_SE);
		case WINDOW_NW:
			return state.a(SHAPE, Shape.WINDOW_NE);
		case WINDOW_SE:
			return state.a(SHAPE, Shape.WINDOW_SW);
		case WINDOW_SW:
			return state.a(SHAPE, Shape.WINDOW_NW);
		default:
			return state;
		}
	}

	public enum Shape implements SmoothUtil {
		PLAIN, WINDOW, WINDOW_NW, WINDOW_SW, WINDOW_NE, WINDOW_SE;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	// Tanks are less noisy when placed in batch
	public static final SnowyBlock SILENCED_METAL =
		new SnowyBlock(0.1F, 1.5F, MusicType.hB, MusicType.hH,
			MusicType.hE, MusicType.hD, MusicType.hC);

	@Override
	public SnowyBlock getSoundType(PistonHandler state, ItemConvertible world, BlockPos pos, apx entity) {
		SnowyBlock soundType = super.getSoundType(state, world, pos, entity);
		if (entity != null && entity.getPersistentData()
			.contains("SilenceTankSound"))
			return SILENCED_METAL;
		return soundType;
	}
}

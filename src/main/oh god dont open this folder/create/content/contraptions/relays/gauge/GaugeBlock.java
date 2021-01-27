package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.Random;
import afj;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class GaugeBlock extends DirectionalAxisKineticBlock {

	public static final GaugeShaper GAUGE = GaugeShaper.make();
	protected Type type;

	public enum Type implements SmoothUtil {
		SPEED, STRESS;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	public static GaugeBlock speed(c properties) {
		return new GaugeBlock(properties, Type.SPEED);
	}
	
	public static GaugeBlock stress(c properties) {
		return new GaugeBlock(properties, Type.STRESS);
	}
	
	protected GaugeBlock(c properties, Type type) {
		super(properties);
		this.type = type;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		switch (type) {
		case SPEED:
			return AllTileEntities.SPEEDOMETER.create();
		case STRESS:
			return AllTileEntities.STRESSOMETER.create();
		default:
			return null;
		}
	}

	/* FIXME: Is there a new way of doing this in 1.16? Or cn we just delete it?
	@SuppressWarnings("deprecation")
	@Override
	public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return Blocks.SPRUCE_PLANKS.getMaterialColor(state, worldIn, pos);
	} */

	@Override
	public PistonHandler a(PotionUtil context) {
		GameMode world = context.p();
		Direction face = context.j();
		BlockPos placedOnPos = context.a().offset(context.j().getOpposite());
		PistonHandler placedOnState = world.d_(placedOnPos);
		BeetrootsBlock block = placedOnState.b();

		if (block instanceof IRotate && ((IRotate) block).hasShaftTowards(world, placedOnPos, placedOnState, face)) {
			PistonHandler toPlace = n();
			Direction horizontalFacing = context.f();
			Direction nearestLookingDirection = context.d();
			boolean lookPositive = nearestLookingDirection.getDirection() == AxisDirection.POSITIVE;
			if (face.getAxis() == Axis.X) {
				toPlace = toPlace
						.a(FACING, lookPositive ? Direction.NORTH : Direction.SOUTH)
						.a(AXIS_ALONG_FIRST_COORDINATE, true);
			} else if (face.getAxis() == Axis.Y) {
				toPlace = toPlace
						.a(FACING, horizontalFacing.getOpposite())
						.a(AXIS_ALONG_FIRST_COORDINATE, horizontalFacing.getAxis() == Axis.X);
			} else {
				toPlace = toPlace
						.a(FACING, lookPositive ? Direction.WEST : Direction.EAST)
						.a(AXIS_ALONG_FIRST_COORDINATE, false);
			}

			return toPlace;
		}

		return super.a(context);
	}

	@Override
	protected Direction getFacingForPlacement(PotionUtil context) {
		return context.j();
	}

	protected boolean getAxisAlignmentForPlacement(PotionUtil context) {
		return context.f().getAxis() != Axis.X;
	}

	public boolean shouldRenderHeadOnFace(GameMode world, BlockPos pos, PistonHandler state, Direction face) {
		if (face.getAxis().isVertical())
			return false;
		if (face == state.c(FACING).getOpposite())
			return false;
		if (face.getAxis() == getRotationAxis(state))
			return false;
		if (getRotationAxis(state) == Axis.Y && face != state.c(FACING))
			return false;
		PistonHandler blockState = world.d_(pos.offset(face));
		if (BlockHelper.hasBlockSolidSide(blockState, world, pos, face.getOpposite()) && blockState.c() != FluidState.F
				&& !(world instanceof WrappedWorld))
			return false;
		return true;
	}

	@Override
	public void a(PistonHandler stateIn, GameMode worldIn, BlockPos pos, Random rand) {
		BeehiveBlockEntity te = worldIn.c(pos);
		if (te == null || !(te instanceof GaugeTileEntity))
			return;
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		if (gaugeTE.dialTarget == 0)
			return;
		int color = gaugeTE.color;

		for (Direction face : Iterate.directions) {
			if (!shouldRenderHeadOnFace(worldIn, pos, stateIn, face))
				continue;

			EntityHitResult rgb = ColorHelper.getRGB(color);
			EntityHitResult faceVec = EntityHitResult.b(face.getVector());
			Direction positiveFacing = Direction.get(AxisDirection.POSITIVE, face.getAxis());
			EntityHitResult positiveFaceVec = EntityHitResult.b(positiveFacing.getVector());
			int particleCount = gaugeTE.dialTarget > 1 ? 4 : 1;

			if (particleCount == 1 && rand.nextFloat() > 1 / 4f)
				continue;

			for (int i = 0; i < particleCount; i++) {
				EntityHitResult mul = VecHelper
						.offsetRandomly(EntityHitResult.a, rand, .25f)
						.h(new EntityHitResult(1, 1, 1).d(positiveFaceVec))
						.d()
						.a(.3f);
				EntityHitResult offset = VecHelper.getCenterOf(pos).e(faceVec.a(.55)).e(mul);
				worldIn
						.addParticle(new DustParticleEffect((float) rgb.entity, (float) rgb.c, (float) rgb.d, 1), offset.entity,
								offset.c, offset.d, mul.entity, mul.c, mul.d);
			}

		}

	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return GAUGE.get(state.c(FACING), state.c(AXIS_ALONG_FIRST_COORDINATE));
	}

	@Override
	public boolean a(PistonHandler state) {
		return true;
	}

	@Override
	public int a(PistonHandler blockState, GameMode worldIn, BlockPos pos) {
		BeehiveBlockEntity te = worldIn.c(pos);
		if (te instanceof GaugeTileEntity) {
			GaugeTileEntity gaugeTileEntity = (GaugeTileEntity) te;
			return afj.f(afj.a(gaugeTileEntity.dialTarget * 14, 0, 15));
		}
		return 0;
	}

}

package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston;

import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.kinetic_api.content.contraptions.wrench.IWrenchable;
import com.simibubi.kinetic_api.foundation.block.ProperDirectionalBlock;
import com.simibubi.kinetic_api.foundation.utility.placement.IPlacementHelper;
import com.simibubi.kinetic_api.foundation.utility.placement.PlacementHelpers;
import com.simibubi.kinetic_api.foundation.utility.placement.PlacementOffset;
import com.simibubi.kinetic_api.foundation.utility.placement.util.PoleHelper;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import java.util.function.Predicate;

public class PistonExtensionPoleBlock extends ProperDirectionalBlock implements IWrenchable, SeagrassBlock {

    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public PistonExtensionPoleBlock(c properties) {
        super(properties);
        j(n().a(SHAPE, Direction.UP).a(BambooLeaves.C, false));
    }

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	@Override
	public void a(GameMode worldIn, BlockPos pos, PistonHandler state, PlayerAbilities player) {
		Axis axis = state.c(SHAPE)
			.getAxis();
		Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset +=
				modifier) {
				BlockPos currentPos = pos.offset(direction, offset);
				PistonHandler block = worldIn.d_(currentPos);

				if (isExtensionPole(block) && axis == block.c(SHAPE)
					.getAxis())
					continue;

				if (isPiston(block) && block.c(BambooLeaves.M)
					.getAxis() == axis)
					pistonBase = currentPos;

				if (isPistonHead(block) && block.c(BambooLeaves.M)
					.getAxis() == axis)
					pistonHead = currentPos;

				break;
			}
		}

		if (pistonHead != null && pistonBase != null && worldIn.d_(pistonHead)
			.c(BambooLeaves.M) == worldIn.d_(pistonBase)
				.c(BambooLeaves.M)) {

			final BlockPos basePos = pistonBase;
			BlockPos.stream(pistonBase, pistonHead)
				.filter(p -> !p.equals(pos) && !p.equals(basePos))
				.forEach(p -> worldIn.b(p, !player.b_()));
			worldIn.a(basePos, worldIn.d_(basePos)
				.a(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
		}

		super.a(worldIn, pos, state, player);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.FOUR_VOXEL_POLE.get(state.c(SHAPE)
			.getAxis());
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		EmptyFluid FluidState = context.p()
			.b(context.a());
		return n().a(SHAPE, context.j()
			.getOpposite())
			.a(BambooLeaves.C, Boolean.valueOf(FluidState.a() == FlowableFluid.c));
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg ray) {
		ItemCooldownManager heldItem = player.b(hand);

        if (AllBlocks.PISTON_EXTENSION_POLE.isIn(heldItem) && !player.bt()) {
            IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
            PlacementOffset offset = placementHelper.getOffset(world, state, pos, ray);

            if (!offset.isReplaceable(world))
                return Difficulty.PASS;

            offset.placeInWorld(world, AllBlocks.PISTON_EXTENSION_POLE.getDefaultState(), player, heldItem);

            /*BlockPos newPos = new BlockPos(offset.getPos());
			if (!world.getBlockState(newPos).getMaterial().isReplaceable())
				return ActionResultType.PASS;

			if (world.isRemote)
				return ActionResultType.SUCCESS;

            world.setBlockState(newPos, offset.getTransform().apply(AllBlocks.PISTON_EXTENSION_POLE.getDefaultState()));
            if (!player.isCreative())
                heldItem.shrink(1);*/

			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@Override
	public EmptyFluid d(PistonHandler state) {
		return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false)
			: FlowableFluid.FALLING.h();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(BambooLeaves.C);
		super.a(builder);
	}

    @Override
    public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbourState, GrassColors world, BlockPos pos, BlockPos neighbourPos) {
        if (state.c(BambooLeaves.C)) {
            world.H().a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
        }
        return state;
    }

    @MethodsReturnNonnullByDefault
    public static class PlacementHelper extends PoleHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper(){
            super(
                    AllBlocks.PISTON_EXTENSION_POLE::has,
                    state -> state.c(SHAPE).getAxis(),
                    SHAPE
            );
        }

        @Override
        public Predicate<ItemCooldownManager> getItemPredicate() {
            return AllBlocks.PISTON_EXTENSION_POLE::isIn;
        }
    }
}

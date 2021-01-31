package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston;

import bnx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllSoundEvents;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import dcg;
import ddb;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.Tags;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock implements ITE<MechanicalPistonTileEntity> {

	public static final DirectionProperty<PistonState> STATE = DirectionProperty.a("state", PistonState.class);
	protected boolean isSticky;

	public static MechanicalPistonBlock normal(c properties) {
		return new MechanicalPistonBlock(properties, false);
	}

	public static MechanicalPistonBlock sticky(c properties) {
		return new MechanicalPistonBlock(properties, true);
	}

	protected MechanicalPistonBlock(c properties, boolean sticky) {
		super(properties);
		j(n().a(FACING, Direction.NORTH)
			.a(STATE, PistonState.RETRACTED));
		isSticky = sticky;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(STATE);
		super.a(builder);
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (!player.eJ())
			return Difficulty.PASS;
		if (player.bt())
			return Difficulty.PASS;
		if (!player.b(handIn)
			.b()
			.a(Tags.Items.SLIMEBALLS)) {
			if (player.b(handIn)
				.a()) {
				withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
				return Difficulty.SUCCESS;
			}
			return Difficulty.PASS;
		}
		if (state.c(STATE) != PistonState.RETRACTED)
			return Difficulty.PASS;
		Direction direction = state.c(FACING);
		if (hit.b() != direction)
			return Difficulty.PASS;
		if (((MechanicalPistonBlock) state.b()).isSticky)
			return Difficulty.PASS;
		if (worldIn.v) {
			EntityHitResult vec = hit.e();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.entity, vec.c, vec.d, 0, 0, 0);
			return Difficulty.SUCCESS;
		}
		worldIn.a(null, pos, AllSoundEvents.SLIME_ADDED.get(), SoundEvent.e, .5f, 1);
		if (!player.b_())
			player.b(handIn)
				.g(1);
		worldIn.a(pos, AllBlocks.STICKY_MECHANICAL_PISTON.getDefaultState()
			.a(FACING, direction)
			.a(AXIS_ALONG_FIRST_COORDINATE, state.c(AXIS_ALONG_FIRST_COORDINATE)));
		return Difficulty.SUCCESS;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.MECHANICAL_PISTON.create();
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		if (state.c(STATE) != PistonState.RETRACTED)
			return Difficulty.PASS;
		return super.onWrenched(state, context);
	}

	public enum PistonState implements SmoothUtil {
		RETRACTED, MOVING, EXTENDED;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	@Override
	public void a(GameMode worldIn, BlockPos pos, PistonHandler state, PlayerAbilities player) {
		Direction direction = state.c(FACING);
		BlockPos pistonHead = null;
		BlockPos pistonBase = pos;
		boolean dropBlocks = player == null || !player.b_();

		Integer maxPoles = maxAllowedPistonPoles();
		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction, offset);
			PistonHandler block = worldIn.d_(currentPos);

			if (isExtensionPole(block) && direction.getAxis() == block.c(BambooLeaves.M)
				.getAxis())
				continue;

			if (isPistonHead(block) && block.c(BambooLeaves.M) == direction) {
				pistonHead = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
			BlockPos.stream(pistonBase, pistonHead)
				.filter(p -> !p.equals(pos))
				.forEach(p -> worldIn.b(p, dropBlocks));
		}

		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
			PistonHandler block = worldIn.d_(currentPos);

			if (isExtensionPole(block) && direction.getAxis() == block.c(BambooLeaves.M)
				.getAxis()) {
				worldIn.b(currentPos, dropBlocks);
				continue;
			}

			break;
		}

		super.a(worldIn, pos, state, player);
	}

	public static int maxAllowedPistonPoles() {
		return AllConfigs.SERVER.kinetics.maxPistonPoles.get();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {

		if (state.c(STATE) == PistonState.EXTENDED)
			return AllShapes.MECHANICAL_PISTON_EXTENDED.get(state.c(FACING));

		if (state.c(STATE) == PistonState.MOVING)
			return AllShapes.MECHANICAL_PISTON.get(state.c(FACING));

		return ddb.b();
	}

	@Override
	public Class<MechanicalPistonTileEntity> getTileEntityClass() {
		return MechanicalPistonTileEntity.class;
	}

	public static boolean isPiston(PistonHandler state) {
		return AllBlocks.MECHANICAL_PISTON.has(state) || isStickyPiston(state);
	}

	public static boolean isStickyPiston(PistonHandler state) {
		return AllBlocks.STICKY_MECHANICAL_PISTON.has(state);
	}

	public static boolean isExtensionPole(PistonHandler state) {
		return AllBlocks.PISTON_EXTENSION_POLE.has(state);
	}

	public static boolean isPistonHead(PistonHandler state) {
		return AllBlocks.MECHANICAL_PISTON_HEAD.has(state);
	}
}

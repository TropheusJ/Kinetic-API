package com.simibubi.create.content.logistics.block.chute;

import java.util.HashMap;
import java.util.Map;
import apx;
import bnx;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
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

public class ChuteBlock extends BeetrootsBlock implements IWrenchable, ITE<ChuteTileEntity> {
	public static final IntProperty<Shape> SHAPE = DirectionProperty.a("shape", Shape.class);
	public static final BooleanProperty FACING = BambooLeaves.N;

	public enum Shape implements SmoothUtil {
		INTERSECTION, WINDOW, NORMAL;

		@Override
		public String a() {
			return Lang.asId(name());
		}
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CHUTE.create();
	}

	public ChuteBlock(c p_i48440_1_) {
		super(p_i48440_1_);
		j(n().a(SHAPE, Shape.NORMAL)
			.a(FACING, Direction.DOWN));
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (entityIn.l.v)
			return;
		if (!entityIn.aW())
			return;
		DirectBeltInputBehaviour input = TileEntityBehaviour.get(entityIn.l, new BlockPos(entityIn.cz()
			.b(0, 0.5f, 0)).down(), DirectBeltInputBehaviour.TYPE);
		if (input == null)
			return;
		if (!input.canInsertFromSide(Direction.UP))
			return;

		PaintingEntity itemEntity = (PaintingEntity) entityIn;
		ItemCooldownManager toInsert = itemEntity.g();
		ItemCooldownManager remainder = input.handleInsertion(toInsert, Direction.UP, false);

		if (remainder.a())
			itemEntity.ac();
		if (remainder.E() < toInsert.E())
			itemEntity.b(remainder);
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler p_220082_4_, boolean p_220082_5_) {
		withTileEntityDo(world, pos, ChuteTileEntity::onAdded);
		if (p_220082_5_)
			return;
		updateDiagonalNeighbour(state, world, pos);
	}

	protected void updateDiagonalNeighbour(PistonHandler state, GameMode world, BlockPos pos) {
		Direction facing = state.c(FACING);
		BlockPos toUpdate = pos.down();
		if (facing.getAxis()
			.isHorizontal())
			toUpdate = toUpdate.offset(facing.getOpposite());

		PistonHandler stateToUpdate = world.d_(toUpdate);
		PistonHandler updated = updateDiagonalState(stateToUpdate, world.d_(toUpdate.up()), world, toUpdate);
		if (stateToUpdate != updated && !world.v)
			world.a(toUpdate, updated);
	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler p_196243_4_, boolean p_196243_5_) {
		boolean differentBlock = state.b() != p_196243_4_.b();
		if (state.hasTileEntity() && (differentBlock || !p_196243_4_.hasTileEntity())) {
			withTileEntityDo(world, pos, c -> c.onRemoved(state));
			world.o(pos);
		}
		if (p_196243_5_ || !differentBlock)
			return;

		updateDiagonalNeighbour(state, world, pos);

		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.up()
				.offset(direction);
			PistonHandler stateToUpdate = world.d_(toUpdate);
			PistonHandler updated =
				updateDiagonalState(stateToUpdate, world.d_(toUpdate.up()), world, toUpdate);
			if (stateToUpdate != updated && !world.v)
				world.a(toUpdate, updated);
		}
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction direction, PistonHandler above, GrassColors world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (direction != Direction.UP)
			return state;
		return updateDiagonalState(state, above, world, pos);
	}

	@Override
	public void a(PistonHandler p_220069_1_, GameMode world, BlockPos pos, BeetrootsBlock p_220069_4_,
		BlockPos neighbourPos, boolean p_220069_6_) {
		if (pos.down()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, ChuteTileEntity::blockBelowChanged);
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible world, BlockPos pos) {
		PistonHandler above = world.d_(pos.up());
		return !(above.b() instanceof ChuteBlock) || above.c(FACING) == Direction.DOWN;
	}

	@Override
	public PistonHandler a(PotionUtil ctx) {
		PistonHandler state = super.a(ctx);
		Direction face = ctx.j();
		if (face.getAxis()
			.isHorizontal() && !ctx.g()) {
			GameMode world = ctx.p();
			BlockPos pos = ctx.a();
			return updateDiagonalState(state.a(FACING, face), world.d_(pos.up()), world, pos);
		}
		return state;
	}

	public static PistonHandler updateDiagonalState(PistonHandler state, PistonHandler above, MobSpawnerLogic world, BlockPos pos) {
		if (!(state.b() instanceof ChuteBlock))
			return state;

		Map<Direction, Boolean> connections = new HashMap<>();
		int amtConnections = 0;
		Direction facing = state.c(FACING);
		boolean vertical = facing == Direction.DOWN;

		if (!vertical) {
			PistonHandler target = world.d_(pos.down()
				.offset(facing.getOpposite()));
			if (!(target.b() instanceof ChuteBlock))
				return state.a(FACING, Direction.DOWN)
					.a(SHAPE, Shape.NORMAL);
		}

		for (Direction direction : Iterate.horizontalDirections) {
			PistonHandler diagonalInputChute = world.d_(pos.up()
				.offset(direction));
			boolean value =
				diagonalInputChute.b() instanceof ChuteBlock && diagonalInputChute.c(FACING) == direction;
			connections.put(direction, value);
			if (value)
				amtConnections++;
		}

		boolean noConnections = amtConnections == 0;
		if (vertical)
			return state.a(SHAPE,
				noConnections ? state.c(SHAPE) == Shape.WINDOW ? Shape.WINDOW : Shape.NORMAL : Shape.INTERSECTION);
		if (noConnections)
			return state.a(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.NORTH) && connections.get(Direction.SOUTH))
			return state.a(SHAPE, Shape.INTERSECTION);
		if (connections.get(Direction.EAST) && connections.get(Direction.WEST))
			return state.a(SHAPE, Shape.INTERSECTION);
		if (amtConnections == 1 && connections.get(facing)
			&& !(above.b() instanceof ChuteBlock && above.c(FACING) == Direction.DOWN)
			&& !(above.b() instanceof FunnelBlock && FunnelBlock.getFunnelFacing(above)
				.getAxis()
				.isVertical()))
			return state.a(SHAPE, Shape.NORMAL);
		return state.a(SHAPE, Shape.INTERSECTION);
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		Shape shape = state.c(SHAPE);
		boolean down = state.c(FACING) == Direction.DOWN;
		if (!context.p().v && down && shape != Shape.INTERSECTION) {
			context.p()
				.a(context.a(),
					state.a(SHAPE, shape == Shape.WINDOW ? Shape.NORMAL : Shape.WINDOW));
		}
		return Difficulty.SUCCESS;
	}

	@Override
	public VoxelShapes b(PistonHandler p_220053_1_, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return ChuteShapes.getShape(p_220053_1_);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean addDestroyEffects(PistonHandler state, GameMode world, BlockPos pos, ItemPickupParticle manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public VoxelShapes c(PistonHandler p_220071_1_, MobSpawnerLogic p_220071_2_, BlockPos p_220071_3_,
		ArrayVoxelShape p_220071_4_) {
		return ChuteShapes.getCollisionShape(p_220071_1_);
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> p_206840_1_) {
		super.a(p_206840_1_.a(SHAPE, FACING));
	}

	@Override
	public Class<ChuteTileEntity> getTileEntityClass() {
		return ChuteTileEntity.class;
	}

	@Override
	public Difficulty a(PistonHandler p_225533_1_, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg p_225533_6_) {
		if (!player.b(hand)
			.a())
			return Difficulty.PASS;
		if (world.v)
			return Difficulty.SUCCESS;
		try {
			ChuteTileEntity te = getTileEntity(world, pos);
			if (te == null)
				return Difficulty.PASS;
			if (te.item.a())
				return Difficulty.PASS;
			player.bm.a(world, te.item);
			te.setItem(ItemCooldownManager.tick);
			return Difficulty.SUCCESS;

		} catch (TileEntityException e) {
			e.printStackTrace();
		}
		return Difficulty.PASS;
	}

}

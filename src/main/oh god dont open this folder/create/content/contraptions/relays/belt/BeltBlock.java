package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import apx;
import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import cww;
import dcg;
import ddb;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltBlock extends HorizontalKineticBlock implements ITE<BeltTileEntity>, ISpecialBlockItemRequirement {

	public static final IntProperty<BeltSlope> SLOPE = DirectionProperty.a("slope", BeltSlope.class);
	public static final IntProperty<BeltPart> PART = DirectionProperty.a("part", BeltPart.class);
	public static final BedPart CASING = BedPart.a("casing");

	public BeltBlock(c properties) {
		super(properties);
		j(n().a(SLOPE, BeltSlope.HORIZONTAL)
			.a(PART, BeltPart.START)
			.a(CASING, false));
	}

	@Override
	public void a(ChorusFruitItem p_149666_1_, DefaultedList<ItemCooldownManager> p_149666_2_) {
		p_149666_2_.add(AllItems.BELT_CONNECTOR.asStack());
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		try {
			return getTileEntity(world, pos).hasPulley();
		} catch (TileEntityException e) {
		}
		return false;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		if (state.c(SLOPE) == BeltSlope.SIDEWAYS)
			return Axis.Y;
		return state.c(HORIZONTAL_FACING)
			.rotateYClockwise()
			.getAxis();
	}

	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
		PlayerAbilities player) {
		return AllItems.BELT_CONNECTOR.asStack();
	}

	/* FIXME
	@Override
	public Material getMaterial(BlockState state) {
		return state.get(CASING) ? Material.WOOD : Material.WOOL;
	} */

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemCooldownManager> a(PistonHandler state, net.minecraft.loot.LootGsons.a builder) {
		List<ItemCooldownManager> drops = super.a(state, builder);
		BeehiveBlockEntity tileEntity = builder.b(LootContextParameter.h);
		if (tileEntity instanceof BeltTileEntity && ((BeltTileEntity) tileEntity).hasPulley())
			drops.addAll(AllBlocks.SHAFT.getDefaultState()
				.a(builder));
		return drops;
	}

	@Override
	public void a(PistonHandler state, ServerWorld worldIn, BlockPos pos, ItemCooldownManager p_220062_4_) {
		BeltTileEntity controllerTE = BeltHelper.getControllerTE(worldIn, pos);
		if (controllerTE != null)
			controllerTE.getInventory()
				.ejectAll();
	}

	@Override
	public boolean isFlammable(PistonHandler state, MobSpawnerLogic world, BlockPos pos, Direction face) {
		return false;
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		BlockPos entityPosition = entityIn.cA();
		BlockPos beltPos = null;

		if (AllBlocks.BELT.has(worldIn.d_(entityPosition)))
			beltPos = entityPosition;
		else if (AllBlocks.BELT.has(worldIn.d_(entityPosition.down())))
			beltPos = entityPosition.down();
		if (beltPos == null)
			return;
		if (!(worldIn instanceof GameMode))
			return;

		a(worldIn.d_(beltPos), (GameMode) worldIn, beltPos, entityIn);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx entityIn) {
		if (!canTransportObjects(state))
			return;
		if (entityIn instanceof PlayerAbilities) {
			PlayerAbilities player = (PlayerAbilities) entityIn;
			if (player.bt())
				return;
			if (player.bC.properties)
				return;
		}

		BeltTileEntity belt = BeltHelper.getSegmentTE(worldIn, pos);
		if (belt == null)
			return;
		if (entityIn instanceof PaintingEntity && entityIn.aW()) {
			if (worldIn.v)
				return;
			if (entityIn.cB().c > 0)
				return;
			if (!entityIn.aW())
				return;
			withTileEntityDo(worldIn, pos, te -> {
				PaintingEntity itemEntity = (PaintingEntity) entityIn;
				IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElse(null);
				if (handler == null)
					return;
				ItemCooldownManager remainder = handler.insertItem(0, itemEntity.g()
					.i(), false);
				if (remainder.a())
					itemEntity.ac();
			});
			return;
		}

		BeltTileEntity controller = BeltHelper.getControllerTE(worldIn, pos);
		if (controller == null || controller.passengers == null)
			return;
		if (controller.passengers.containsKey(entityIn)) {
			TransportedEntityInfo info = controller.passengers.get(entityIn);
			if (info.getTicksSinceLastCollision() != 0 || pos.equals(entityIn.cA()))
				info.refresh(pos, state);
		} else {
			controller.passengers.put(entityIn, new TransportedEntityInfo(pos, state));
			entityIn.c(true);
		}
	}

	public static boolean canTransportObjects(PistonHandler state) {
		if (!AllBlocks.BELT.has(state))
			return false;
		BeltSlope slope = state.c(SLOPE);
		return slope != BeltSlope.VERTICAL && slope != BeltSlope.SIDEWAYS;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
		dcg hit) {
		if (player.bt() || !player.eJ())
			return Difficulty.PASS;
		ItemCooldownManager heldItem = player.b(handIn);
		boolean isShaft = AllBlocks.SHAFT.isIn(heldItem);
		boolean isHand = heldItem.a() && handIn == ItemScatterer.RANDOM;

		BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos);
		if (belt == null)
			return Difficulty.PASS;

		if (isHand) {
			BeltTileEntity controllerBelt = belt.getControllerTE();
			if (controllerBelt == null)
				return Difficulty.PASS;
			if (world.v)
				return Difficulty.SUCCESS;
			controllerBelt.getInventory()
				.applyToEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
					player.bm.a(world, transportedItemStack.stack);
					return TransportedResult.removeItem();
				});
		}

		if (isShaft) {
			if (state.c(PART) != BeltPart.MIDDLE)
				return Difficulty.PASS;
			if (world.v)
				return Difficulty.SUCCESS;
			if (!player.b_())
				heldItem.g(1);
			KineticTileEntity.switchToBlockState(world, pos, state.a(PART, BeltPart.PULLEY));
			return Difficulty.SUCCESS;
		}

		if (AllBlocks.BRASS_CASING.isIn(heldItem)) {
			if (world.v)
				return Difficulty.SUCCESS;
			AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.BRASS));
			return Difficulty.SUCCESS;
		}

		if (AllBlocks.ANDESITE_CASING.isIn(heldItem)) {
			if (world.v)
				return Difficulty.SUCCESS;
			AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.ANDESITE));
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		PlayerAbilities player = context.n();
		BlockPos pos = context.a();

		if (state.c(CASING)) {
			if (world.v)
				return Difficulty.SUCCESS;
			withTileEntityDo(world, pos, te -> te.setCasingType(CasingType.NONE));
			return Difficulty.SUCCESS;
		}

		if (state.c(PART) == BeltPart.PULLEY) {
			if (world.v)
				return Difficulty.SUCCESS;
			KineticTileEntity.switchToBlockState(world, pos, state.a(PART, BeltPart.MIDDLE));
			if (player != null && !player.b_())
				player.bm.a(world, AllBlocks.SHAFT.asStack());
			return Difficulty.SUCCESS;
		}

		return Difficulty.FAIL;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(SLOPE, PART, CASING);
		super.a(builder);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public cww getAiPathNodeType(PistonHandler state, MobSpawnerLogic world, BlockPos pos, ItemSteerable entity) {
		return cww.j;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean addDestroyEffects(PistonHandler state, GameMode world, BlockPos pos, ItemPickupParticle manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return BeltShapes.getShape(state);
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos,
		ArrayVoxelShape context) {
		if (state.b() != this)
			return ddb.a();

		VoxelShapes shape = b(state, worldIn, pos, context);
		try {
			if (context.getEntity() == null)
				return shape;

			BeltTileEntity belt = getTileEntity(worldIn, pos);
			BeltTileEntity controller = belt.getControllerTE();

			if (controller == null)
				return shape;
			if (controller.passengers == null || !controller.passengers.containsKey(context.getEntity())) {
				return BeltShapes.getCollisionShape(state);
			}

		} catch (TileEntityException e) {
		}
		return shape;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.BELT.create();
	}

	@Override
	public RedstoneLampBlock b(PistonHandler state) {
		return state.c(CASING) ? RedstoneLampBlock.c : RedstoneLampBlock.b;
	}

	public static void initBelt(GameMode world, BlockPos pos) {
		if (world.v)
			return;
		if (world instanceof ServerWorld && ((ServerWorld) world).getChunkManager().g() instanceof EnderDragonFight)
			return;

		PistonHandler state = world.d_(pos);
		if (!AllBlocks.BELT.has(state))
			return;
		// Find controller
		int limit = 1000;
		BlockPos currentPos = pos;
		while (limit-- > 0) {
			PistonHandler currentState = world.d_(currentPos);
			if (!AllBlocks.BELT.has(currentState)) {
				world.b(pos, true);
				return;
			}
			BlockPos nextSegmentPosition = nextSegmentPosition(currentState, currentPos, false);
			if (nextSegmentPosition == null)
				break;
			if (!world.isAreaLoaded(nextSegmentPosition, 0))
				return;
			currentPos = nextSegmentPosition;
		}

		// Init belts
		int index = 0;
		List<BlockPos> beltChain = getBeltChain(world, currentPos);
		if (beltChain.size() < 2) {
			world.b(currentPos, true);
			return;
		}

		for (BlockPos beltPos : beltChain) {
			BeehiveBlockEntity tileEntity = world.c(beltPos);
			PistonHandler currentState = world.d_(beltPos);

			if (tileEntity instanceof BeltTileEntity && AllBlocks.BELT.has(currentState)) {
				BeltTileEntity te = (BeltTileEntity) tileEntity;
				te.setController(currentPos);
				te.beltLength = beltChain.size();
				te.index = index;
				te.attachKinetics();
				te.X_();
				te.sendData();

				if (te.isController() && !canTransportObjects(currentState))
					te.getInventory()
						.ejectAll();
			} else {
				world.b(currentPos, true);
				return;
			}
			index++;
		}

	}

	@Override
	public void a(PistonHandler state, GameMode world, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (world.v)
			return;
		if (state.b() == newState.b())
			return;
		if (isMoving)
			return;

		BeehiveBlockEntity te = world.c(pos);
		if (te instanceof BeltTileEntity) {
			BeltTileEntity beltTileEntity = (BeltTileEntity) te;
			if (beltTileEntity.isController())
				beltTileEntity.getInventory()
					.ejectAll();
			world.o(pos);
		}

		// Destroy chain
		for (boolean forward : Iterate.trueAndFalse) {
			BlockPos currentPos = nextSegmentPosition(state, pos, forward);
			if (currentPos == null)
				continue;
			PistonHandler currentState = world.d_(currentPos);
			if (!AllBlocks.BELT.has(currentState))
				continue;

			boolean hasPulley = false;
			BeehiveBlockEntity tileEntity = world.c(currentPos);
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity belt = (BeltTileEntity) tileEntity;
				if (belt.isController())
					belt.getInventory()
						.ejectAll();

				belt.al_();
				hasPulley = belt.hasPulley();
			}

			PistonHandler shaftState = AllBlocks.SHAFT.getDefaultState()
				.a(BambooLeaves.F, getRotationAxis(currentState));
			world.a(currentPos, hasPulley ? shaftState : BellBlock.FACING.n(), 3);
			world.syncWorldEvent(2001, currentPos, BeetrootsBlock.i(currentState));
		}
	}

	@Override
	public PistonHandler a(PistonHandler state, Direction side, PistonHandler p_196271_3_, GrassColors world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (side.getAxis()
			.isHorizontal())
			updateTunnelConnections(world, pos.up());
		return state;
	}

	private void updateTunnelConnections(GrassColors world, BlockPos pos) {
		BeetrootsBlock tunnelBlock = world.d_(pos)
			.b();
		if (tunnelBlock instanceof BeltTunnelBlock)
			((BeltTunnelBlock) tunnelBlock).updateTunnel(world, pos);
	}

	public static List<BlockPos> getBeltChain(GameMode world, BlockPos controllerPos) {
		List<BlockPos> positions = new LinkedList<>();

		PistonHandler blockState = world.d_(controllerPos);
		if (!AllBlocks.BELT.has(blockState))
			return positions;

		int limit = 1000;
		BlockPos current = controllerPos;
		while (limit-- > 0 && current != null) {
			PistonHandler state = world.d_(current);
			if (!AllBlocks.BELT.has(state))
				break;
			positions.add(current);
			current = nextSegmentPosition(state, current, true);
		}

		return positions;
	}

	public static BlockPos nextSegmentPosition(PistonHandler state, BlockPos pos, boolean forward) {
		Direction direction = state.c(HORIZONTAL_FACING);
		BeltSlope slope = state.c(SLOPE);
		BeltPart part = state.c(PART);

		int offset = forward ? 1 : -1;

		if (part == BeltPart.END && forward || part == BeltPart.START && !forward)
			return null;
		if (slope == BeltSlope.VERTICAL)
			return pos.up(direction.getDirection() == AxisDirection.POSITIVE ? offset : -offset);
		pos = pos.offset(direction, offset);
		if (slope != BeltSlope.HORIZONTAL && slope != BeltSlope.SIDEWAYS)
			return pos.up(slope == BeltSlope.UPWARD ? offset : -offset);
		return pos;
	}

	public static boolean canAccessFromSide(Direction facing, PistonHandler belt) {
//		if (facing == null)
//			return true;
//		if (!belt.get(BeltBlock.CASING))
//			return false;
//		BeltPart part = belt.get(BeltBlock.PART);
//		if (part != BeltPart.MIDDLE && facing.getAxis() == belt.get(HORIZONTAL_FACING)
//			.rotateY()
//			.getAxis())
//			return false;
//
//		BeltSlope slope = belt.get(BeltBlock.SLOPE);
//		if (slope != BeltSlope.HORIZONTAL) {
//			if (slope == BeltSlope.DOWNWARD && part == BeltPart.END)
//				return true;
//			if (slope == BeltSlope.UPWARD && part == BeltPart.START)
//				return true;
//			Direction beltSide = belt.get(HORIZONTAL_FACING);
//			if (slope == BeltSlope.DOWNWARD)
//				beltSide = beltSide.getOpposite();
//			if (beltSide == facing)
//				return false;
//		}

		return true;
	}

	@Override
	public Class<BeltTileEntity> getTileEntityClass() {
		return BeltTileEntity.class;
	}

	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		List<ItemCooldownManager> required = new ArrayList<>();
		if (state.c(PART) != BeltPart.MIDDLE)
			required.add(AllBlocks.SHAFT.asStack());
		if (state.c(PART) == BeltPart.START)
			required.add(AllItems.BELT_CONNECTOR.asStack());
		if (required.isEmpty())
			return ItemRequirement.NONE;
		return new ItemRequirement(ItemUseType.CONSUME, required);
	}

	@Override
	public PistonHandler a(PistonHandler state, RespawnAnchorBlock rot) {
		PistonHandler rotate = super.a(state, rot);

		if (state.c(SLOPE) != BeltSlope.VERTICAL)
			return rotate;
		if (state.c(HORIZONTAL_FACING)
			.getDirection() != rotate.c(HORIZONTAL_FACING)
				.getDirection()) {
			if (state.c(PART) == BeltPart.START)
				return rotate.a(PART, BeltPart.END);
			if (state.c(PART) == BeltPart.END)
				return rotate.a(PART, BeltPart.START);
		}

		return rotate;
	}

}

package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import afj;
import apx;
import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import ddb;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.common.util.LazyOptional;

public class CartAssemblerBlock extends BlockWithEntity
	implements ITE<CartAssemblerTileEntity>, IWrenchable, ISpecialBlockItemRequirement {

	public static final IntProperty<Instrument> RAIL_SHAPE =
		DirectionProperty.a("shape", Instrument.class, Instrument.EAST_WEST, Instrument.NORTH_SOUTH);
	public static final IntProperty<CartAssembleRailType> RAIL_TYPE =
		DirectionProperty.a("rail_type", CartAssembleRailType.class);
	public static final BedPart POWERED = BambooLeaves.w;

	public CartAssemblerBlock(c properties) {
		super(true, properties);
		j(n().a(POWERED, false)
			.a(RAIL_TYPE, CartAssembleRailType.POWERED_RAIL));
	}

	public static PistonHandler createAnchor(PistonHandler state) {
		Axis axis = state.c(RAIL_SHAPE) == Instrument.NORTH_SOUTH ? Axis.Z : Axis.X;
		return AllBlocks.MINECART_ANCHOR.getDefaultState()
			.a(BambooLeaves.E, axis);
	}

	private static HoeItem getRailItem(PistonHandler state) {
		return state.c(RAIL_TYPE).getItem();
	}

	public static PistonHandler getRailBlock(PistonHandler state) {
		BlockWithEntity railBlock = (BlockWithEntity) state.c(RAIL_TYPE).getBlock();
		PistonHandler railState = railBlock.n()
			.a(railBlock.d(), state.c(RAIL_SHAPE));
		if (railState.b(ControllerRailBlock.BACKWARDS)) {
			railState = railState.a(ControllerRailBlock.BACKWARDS, state.c(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS);
		}
		return railState;
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(RAIL_SHAPE, POWERED, RAIL_TYPE);
		super.a(builder);
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CART_ASSEMBLER.create();
	}

	@Override
	public boolean canMakeSlopes(@Nonnull PistonHandler state, @Nonnull MobSpawnerLogic world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	public void onMinecartPass(@Nonnull PistonHandler state, @Nonnull GameMode world, @Nonnull BlockPos pos,
		ScheduleBuilder cart) {
		if (!canAssembleTo(cart))
			return;
		if (world.v)
			return;

		withTileEntityDo(world, pos, te -> {
			/*
		}
<<<<<<< HEAD
			if (te.isMinecartUpdateValid()) {
				switch (state.get(RAIL_TYPE)) {
				case POWERED_RAIL:
					if (state.get(POWERED)) {
						assemble(world, pos, cart);
						Direction facing = cart.getAdjustedHorizontalFacing();
						float speed = getRailMaxSpeed(state, world, pos, cart);
						cart.setMotion(facing.getXOffset() * speed, facing.getYOffset() * speed,
							facing.getZOffset() * speed);
					} else {
						disassemble(world, pos, cart);
						Vector3d diff = VecHelper.getCenterOf(pos)
							.subtract(cart.getPositionVec());
						cart.setMotion(diff.x / 16f, 0, diff.z / 16f);
					}
					break;
				case REGULAR:
					if (state.get(POWERED)) {
						assemble(world, pos, cart);
					} else {
						disassemble(world, pos, cart);
					}
					break;
				case ACTIVATOR_RAIL:
					if (state.get(POWERED)) {
						disassemble(world, pos, cart);
					}
					break;
				case DETECTOR_RAIL:
					if (cart.getPassengers()
						.isEmpty()) {
						assemble(world, pos, cart);
						Direction facing = cart.getAdjustedHorizontalFacing();
						float speed = getRailMaxSpeed(state, world, pos, cart);
						cart.setMotion(facing.getXOffset() * speed, facing.getYOffset() * speed,
							facing.getZOffset() * speed);
					} else {
						disassemble(world, pos, cart);
					}
					break;
				default:
					break;
				}
				te.resetTicksSinceMinecartUpdate();
=======*/
			if (!te.isMinecartUpdateValid())
				return;

			CartAssemblerAction action = getActionForCart(state, cart);
			if (action.shouldAssemble())
				assemble(world, pos, cart);
			if (action.shouldDisassemble())
				disassemble(world, pos, cart);
			if (action == CartAssemblerAction.ASSEMBLE_ACCELERATE) {
				Direction facing = cart.bZ();
				float speed = getRailMaxSpeed(state, world, pos, cart);
				cart.n(facing.getOffsetX() * speed, facing.getOffsetY() * speed, facing.getOffsetZ() * speed);
			}
			if (action == CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL) {
				Vec3i accelerationVector = ControllerRailBlock.getAccelerationVector(AllBlocks.CONTROLLER_RAIL.getDefaultState().a(ControllerRailBlock.SHAPE, state.c(RAIL_SHAPE)).a(ControllerRailBlock.BACKWARDS, state.c(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS));
				float speed = getRailMaxSpeed(state, world, pos, cart);
				cart.f(EntityHitResult.b(accelerationVector).a(speed));
			}
			if (action == CartAssemblerAction.DISASSEMBLE_BRAKE) {
				EntityHitResult diff = VecHelper.getCenterOf(pos)
					.d(cart.cz());
				cart.n(diff.entity / 16f, 0, diff.d / 16f);
			}

		});
	}

	public enum CartAssemblerAction {
		ASSEMBLE, DISASSEMBLE, ASSEMBLE_ACCELERATE, DISASSEMBLE_BRAKE, ASSEMBLE_ACCELERATE_DIRECTIONAL, PASS;

		public boolean shouldAssemble() {
			return this == ASSEMBLE || this == ASSEMBLE_ACCELERATE || this == ASSEMBLE_ACCELERATE_DIRECTIONAL;
		}

		public boolean shouldDisassemble() {
			return this == DISASSEMBLE || this == DISASSEMBLE_BRAKE;
		}
	}

	public static CartAssemblerAction getActionForCart(PistonHandler state, ScheduleBuilder cart) {
		CartAssembleRailType type = state.c(RAIL_TYPE);
		boolean powered = state.c(POWERED);

		if (type == CartAssembleRailType.REGULAR)
			return powered ? CartAssemblerAction.ASSEMBLE : CartAssemblerAction.DISASSEMBLE;

		if (type == CartAssembleRailType.ACTIVATOR_RAIL)
			return powered ? CartAssemblerAction.DISASSEMBLE : CartAssemblerAction.PASS;

		if (type == CartAssembleRailType.POWERED_RAIL)
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE_BRAKE;

		if (type == CartAssembleRailType.DETECTOR_RAIL)
			return cart.cm()
				.isEmpty() ? CartAssemblerAction.ASSEMBLE_ACCELERATE : CartAssemblerAction.DISASSEMBLE;

		if (type == CartAssembleRailType.CONTROLLER_RAIL || type == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS)
			return powered ? CartAssemblerAction.ASSEMBLE_ACCELERATE_DIRECTIONAL : CartAssemblerAction.DISASSEMBLE_BRAKE;

		return CartAssemblerAction.PASS;
	}

	public static boolean canAssembleTo(ScheduleBuilder cart) {
		return cart.canBeRidden() || cart instanceof MinecartEntity || cart instanceof BoatEntity;
	}

	@Override
	@Nonnull
	public Difficulty a(@Nonnull PistonHandler state, @Nonnull GameMode world, @Nonnull BlockPos pos,
		PlayerAbilities player, @Nonnull ItemScatterer hand, @Nonnull dcg blockRayTraceResult) {

		ItemCooldownManager itemStack = player.b(hand);
		HoeItem previousItem = getRailItem(state);
		HoeItem heldItem = itemStack.b();
		if (heldItem != previousItem) {

			CartAssembleRailType newType = null;
			for (CartAssembleRailType type : CartAssembleRailType.values())
				if (heldItem == type.getItem())
					newType = type;
			if (newType == null)
				return Difficulty.PASS;
			world.a(null, pos, MusicType.gL, SoundEvent.h, 1, 1);
			world.a(pos, state.a(RAIL_TYPE, newType));

			if (!player.b_()) {
				itemStack.g(1);
				player.bm.a(world, new ItemCooldownManager(previousItem));
			}
			return Difficulty.SUCCESS;
		}

		return Difficulty.PASS;
	}

	protected void assemble(GameMode world, BlockPos pos, ScheduleBuilder cart) {
		if (!cart.cm()
			.isEmpty())
			return;

		LazyOptional<MinecartController> optional =
			cart.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (optional.isPresent() && optional.orElse(null)
			.isCoupledThroughContraption())
			return;

		CartMovementMode mode =
			getTileEntityOptional(world, pos).map(te -> CartMovementMode.values()[te.movementMode.value])
				.orElse(CartMovementMode.ROTATE);

		MountedContraption contraption = new MountedContraption(mode);
		if (!contraption.assemble(world, pos))
			return;

		boolean couplingFound = contraption.connectedCart != null;
		Optional<Direction> initialOrientation = cart.cB()
			.f() < 1 / 512f ? Optional.empty() : Optional.of(cart.bZ());

		if (couplingFound) {
			cart.d(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
			if (!CouplingHandler.tryToCoupleCarts(null, world, cart.X(),
				contraption.connectedCart.X()))
				return;
		}

		contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
		contraption.startMoving(world);
		contraption.expandBoundsAroundAxis(Axis.Y);

		if (couplingFound) {
			EntityHitResult diff = contraption.connectedCart.cz()
				.d(cart.cz());
			initialOrientation = Optional.of(Direction.fromRotation(afj.d(diff.d, diff.entity) * 180 / Math.PI));
		}

		OrientedContraptionEntity entity = OrientedContraptionEntity.create(world, contraption, initialOrientation);
		if (couplingFound)
			entity.setCouplingId(cart.bR());
		entity.d(pos.getX(), pos.getY(), pos.getZ());
		world.c(entity);
		entity.m(cart);

		if (cart instanceof MinecartEntity) {
			CompoundTag nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", 0);
			nbt.putDouble("PushX", 0);
			cart.deserializeNBT(nbt);
		}
	}

	protected void disassemble(GameMode world, BlockPos pos, ScheduleBuilder cart) {
		if (cart.cm()
			.isEmpty())
			return;
		apx entity = cart.cm()
			.get(0);
		if (!(entity instanceof OrientedContraptionEntity))
			return;
		OrientedContraptionEntity contraption = (OrientedContraptionEntity) entity;
		UUID couplingId = contraption.getCouplingId();

		if (couplingId == null) {
			disassembleCart(cart);
			return;
		}

		Couple<MinecartController> coupledCarts = contraption.getCoupledCartsIfPresent();
		if (coupledCarts == null)
			return;

		// Make sure connected cart is present and being disassembled
		for (boolean current : Iterate.trueAndFalse) {
			MinecartController minecartController = coupledCarts.get(current);
			if (minecartController.cart() == cart)
				continue;
			BlockPos otherPos = minecartController.cart()
				.cA();
			PistonHandler blockState = world.d_(otherPos);
			if (!AllBlocks.CART_ASSEMBLER.has(blockState))
				return;
			if (!getActionForCart(blockState, minecartController.cart()).shouldDisassemble())
				return;
		}

		for (boolean current : Iterate.trueAndFalse)
			coupledCarts.get(current)
				.removeConnection(current);
		disassembleCart(cart);
	}

	protected void disassembleCart(ScheduleBuilder cart) {
		cart.bd();
		if (cart instanceof MinecartEntity) {
			CompoundTag nbt = cart.serializeNBT();
			nbt.putDouble("PushZ", cart.cB().entity);
			nbt.putDouble("PushX", cart.cB().d);
			cart.deserializeNBT(nbt);
		}
	}

	@Override
	public void a(@Nonnull PistonHandler state, @Nonnull GameMode worldIn, @Nonnull BlockPos pos,
		@Nonnull BeetrootsBlock blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
		super.a(state, worldIn, pos, blockIn, fromPos, isMoving);

		if (worldIn.v)
			return;

		boolean previouslyPowered = state.c(POWERED);
		if (previouslyPowered != worldIn.r(pos)) {
			worldIn.a(pos, state.a(POWERED), 2);
		}
	}

	@Override
	@Nonnull
	public IntProperty<Instrument> d() {
		return RAIL_SHAPE;
	}

	@Override
	@Nonnull
	public VoxelShapes b(PistonHandler state, @Nonnull MobSpawnerLogic worldIn, @Nonnull BlockPos pos,
		@Nonnull ArrayVoxelShape context) {
		return AllShapes.CART_ASSEMBLER.get(getRailAxis(state));
	}

	protected Axis getRailAxis(PistonHandler state) {
		return state.c(RAIL_SHAPE) == Instrument.NORTH_SOUTH ? Direction.Axis.Z : Direction.Axis.X;
	}

	@Override
	@Nonnull
	public VoxelShapes c(@Nonnull PistonHandler state, @Nonnull MobSpawnerLogic worldIn, @Nonnull BlockPos pos,
		ArrayVoxelShape context) {
		apx entity = context.getEntity();
		if (entity instanceof ScheduleBuilder)
			return ddb.a();
		if (entity instanceof PlayerAbilities)
			return AllShapes.CART_ASSEMBLER_PLAYER_COLLISION.get(getRailAxis(state));
		return ddb.b();
	}

	@Override
	@Nonnull
	public LavaFluid f(@Nonnull PistonHandler state) {
		return LavaFluid.c;
	}

	/* FIXME: Is there a 1.16 equivalent to be used? Or is this just removed?
	@Override
	public boolean isNormalCube(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
		return false;
	}
	 */

	@Override
	public Class<CartAssemblerTileEntity> getTileEntityClass() {
		return CartAssemblerTileEntity.class;
	}

	@Override
	public boolean a(@Nonnull PistonHandler state, @Nonnull ItemConvertible world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	@Nonnull
	public List<ItemCooldownManager> a(@Nonnull PistonHandler state,
		@Nonnull net.minecraft.loot.LootGsons.a builder) {
		List<ItemCooldownManager> drops = super.a(state, builder);
		drops.addAll(getRailBlock(state).a(builder));
		return drops;
	}

	@Override
	public ItemRequirement getRequiredItems(PistonHandler state) {
		ArrayList<ItemCooldownManager> reuiredItems = new ArrayList<ItemCooldownManager>();
		reuiredItems.add(new ItemCooldownManager(getRailItem(state)));
		reuiredItems.add(new ItemCooldownManager(h()));
		return new ItemRequirement(ItemUseType.CONSUME, reuiredItems);
	}

	@SuppressWarnings("deprecation")
	public List<ItemCooldownManager> getDropedAssembler(PistonHandler state, ServerWorld world, BlockPos pos,
		@Nullable BeehiveBlockEntity p_220077_3_, @Nullable apx p_220077_4_, ItemCooldownManager p_220077_5_) {
		return super.a(state, (new LootGsons.a(world)).a(world.t)
			.a(LootContextParameter.f, EntityHitResult.b(pos))
			.a(LootContextParameter.i, p_220077_5_)
			.b(LootContextParameter.id, p_220077_4_)
			.b(LootContextParameter.h, p_220077_3_));
	}

	@Override
	public Difficulty onSneakWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		BlockPos pos = context.a();
		PlayerAbilities player = context.n();
		if (world.v)
			return Difficulty.SUCCESS;

		if (player != null && !player.b_())
			getDropedAssembler(state, (ServerWorld) world, pos, world.c(pos), player, context.m())
				.forEach(itemStack -> {
					player.bm.a(world, itemStack);
				});
		if(world instanceof ServerWorld)
			state.a((ServerWorld) world, pos, ItemCooldownManager.tick);
		world.a(pos, getRailBlock(state));
		return Difficulty.SUCCESS;
	}

	public static class MinecartAnchorBlock extends BeetrootsBlock {

		public MinecartAnchorBlock(c p_i48440_1_) {
			super(p_i48440_1_);
		}

		@Override
		protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
			builder.a(BambooLeaves.E);
			super.a(builder);
		}

		@Override
		@Nonnull
		public VoxelShapes b(@Nonnull PistonHandler p_220053_1_, @Nonnull MobSpawnerLogic p_220053_2_,
			@Nonnull BlockPos p_220053_3_, @Nonnull ArrayVoxelShape p_220053_4_) {
			return ddb.a();
		}
	}

	@Override
	public Difficulty onWrenched(PistonHandler state, bnx context) {
		GameMode world = context.p();
		if (world.v)
			return Difficulty.SUCCESS;
		BlockPos pos = context.a();
		PistonHandler newState = state.a(RAIL_SHAPE, state.c(RAIL_SHAPE) == Instrument.NORTH_SOUTH ? Instrument.EAST_WEST : Instrument.NORTH_SOUTH);
		if (state.c(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL || state.c(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS) {
			newState = newState.a(RAIL_TYPE, AllBlocks.CONTROLLER_RAIL.get().a(AllBlocks.CONTROLLER_RAIL.getDefaultState()
				.a(ControllerRailBlock.SHAPE, state.c(RAIL_SHAPE)).a(ControllerRailBlock.BACKWARDS,
					state.c(RAIL_TYPE) == CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS), RespawnAnchorBlock.field_26442)
				.c(ControllerRailBlock.BACKWARDS) ? CartAssembleRailType.CONTROLLER_RAIL_BACKWARDS : CartAssembleRailType.CONTROLLER_RAIL);
		}
			context.p().a(pos, newState, 3);
			world.b(pos.down(), this);
		return Difficulty.SUCCESS;
	}
}

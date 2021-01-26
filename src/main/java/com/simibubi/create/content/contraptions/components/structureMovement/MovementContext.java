package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.function.UnaryOperator;

import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.Constants.NBT;

public class MovementContext {

	public EntityHitResult position;
	public EntityHitResult motion;
	public EntityHitResult relativeMotion;
	public UnaryOperator<EntityHitResult> rotation;

	public GameMode world;
	public PistonHandler state;
	public BlockPos localPos;
	public CompoundTag tileData;

	public boolean stall;
	public boolean firstMovement;
	public CompoundTag data;
	public Contraption contraption;
	public Object temporaryData;

	public MovementContext(GameMode world, c info, Contraption contraption) {
		this.world = world;
		this.state = info.b;
		this.tileData = info.c;
		this.contraption = contraption;
		localPos = info.a;

		firstMovement = true;
		motion = EntityHitResult.a;
		relativeMotion = EntityHitResult.a;
		rotation = v -> v;
		position = null;
		data = new CompoundTag();
		stall = false;
	}

	public float getAnimationSpeed() {
		int modifier = 1000;
		double length = -motion.f();
		if (world.v && contraption.stalled)
			return 700;
		if (Math.abs(length) < 1 / 512f)
			return 0;
		return (((int) (length * modifier + 100 * Math.signum(length))) / 100) * 100;
	}

	public static MovementContext readNBT(GameMode world, c info, CompoundTag nbt, Contraption contraption) {
		MovementContext context = new MovementContext(world, info, contraption);
		context.motion = VecHelper.readNBT(nbt.getList("Motion", NBT.TAG_DOUBLE));
		context.relativeMotion = VecHelper.readNBT(nbt.getList("RelativeMotion", NBT.TAG_DOUBLE));
		if (nbt.contains("Position"))
			context.position = VecHelper.readNBT(nbt.getList("Position", NBT.TAG_DOUBLE));
		context.stall = nbt.getBoolean("Stall");
		context.firstMovement = nbt.getBoolean("FirstMovement");
		context.data = nbt.getCompound("Data");
		return context;
	}

	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.put("Motion", VecHelper.writeNBT(motion));
		nbt.put("RelativeMotion", VecHelper.writeNBT(relativeMotion));
		if (position != null)
			nbt.put("Position", VecHelper.writeNBT(position));
		nbt.putBoolean("Stall", stall);
		nbt.putBoolean("FirstMovement", firstMovement);
		nbt.put("Data", data);
		return nbt;
	}

}
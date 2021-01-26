package com.simibubi.create.content.contraptions.processing.burner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import bcx;
import bnx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GravityField;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlockItem extends BannerItem {

	private final boolean capturedBlaze;

	public static BlazeBurnerBlockItem empty(a properties) {
		return new BlazeBurnerBlockItem(AllBlocks.BLAZE_BURNER.get(), properties, false);
	}

	public static BlazeBurnerBlockItem withBlaze(BeetrootsBlock block, a properties) {
		return new BlazeBurnerBlockItem(block, properties, true);
	}
	
	@Override
	public void a(Map<BeetrootsBlock, HoeItem> p_195946_1_, HoeItem p_195946_2_) {
		if (!hasCapturedBlaze())
			return;
		super.a(p_195946_1_, p_195946_2_);
	}

	private BlazeBurnerBlockItem(BeetrootsBlock block, a properties, boolean capturedBlaze) {
		super(block, properties);
		this.capturedBlaze = capturedBlaze;
	}

	@Override
	public void a(ChorusFruitItem p_150895_1_, DefaultedList<ItemCooldownManager> p_150895_2_) {
		if (!hasCapturedBlaze())
			return;
		super.a(p_150895_1_, p_150895_2_);
	}

	@Override
	public String a() {
		return hasCapturedBlaze() ? super.a() : "item.create." + getRegistryName().getPath();
	}

	@Override
	public Difficulty a(bnx context) {
		if (hasCapturedBlaze())
			return super.a(context);

		GameMode world = context.p();
		BlockPos pos = context.a();
		BeehiveBlockEntity te = world.c(pos);
		PlayerAbilities player = context.n();

		if (!(te instanceof SignBlockEntity))
			return super.a(context);

		TradeOfferList spawner = ((SignBlockEntity) te).d();
		List<GravityField> possibleSpawns =
			ObfuscationReflectionHelper.getPrivateValue(TradeOfferList.class, spawner, "field_98285_e");
		if (possibleSpawns.isEmpty()) {
			possibleSpawns = new ArrayList<>();
			possibleSpawns
				.add(ObfuscationReflectionHelper.getPrivateValue(TradeOfferList.class, spawner, "field_98282_f"));
		}

		Identifier blazeId = EntityDimensions.f.getRegistryName();
		for (GravityField e : possibleSpawns) {
			Identifier spawnerEntityId = new Identifier(e.b()
				.getString("id"));
			if (!spawnerEntityId.equals(blazeId))
				continue;

			spawnCaptureEffects(world, VecHelper.getCenterOf(pos));
			if (world.v || player == null)
				return Difficulty.SUCCESS;

			giveBurnerItemTo(player, context.m(), context.o());
			return Difficulty.SUCCESS;
		}

		return super.a(context);
	}

	@Override
	public Difficulty a(ItemCooldownManager heldItem, PlayerAbilities player, SaddledComponent entity, ItemScatterer hand) {
		if (hasCapturedBlaze())
			return Difficulty.PASS;
		if (!(entity instanceof bcx))
			return Difficulty.PASS;

		GameMode world = player.l;
		spawnCaptureEffects(world, entity.cz());
		if (world.v)
			return Difficulty.FAIL;

		giveBurnerItemTo(player, heldItem, hand);
		entity.ac();
		return Difficulty.FAIL;
	}

	protected void giveBurnerItemTo(PlayerAbilities player, ItemCooldownManager heldItem, ItemScatterer hand) {
		ItemCooldownManager filled = AllBlocks.BLAZE_BURNER.asStack();
		if (!player.b_())
			heldItem.g(1);
		if (heldItem.a()) {
			player.a(hand, filled);
			return;
		}
		player.bm.a(player.l, filled);
	}

	private void spawnCaptureEffects(GameMode world, EntityHitResult vec) {
		if (world.v) {
			for (int i = 0; i < 40; i++) {
				EntityHitResult motion = VecHelper.offsetRandomly(EntityHitResult.a, world.t, .125f);
				world.addParticle(ParticleTypes.FLAME, vec.entity, vec.c, vec.d, motion.entity, motion.c, motion.d);
				EntityHitResult circle = motion.d(1, 0, 1)
					.d()
					.a(.5f);
				world.addParticle(ParticleTypes.SMOKE, circle.entity, vec.c, circle.d, 0, -0.125, 0);
			}
			return;
		}

		BlockPos soundPos = new BlockPos(vec);
		world.a(null, soundPos, MusicType.aO, SoundEvent.f, .25f, .75f);
		world.a(null, soundPos, MusicType.ej, SoundEvent.f, .5f, .75f);
	}

	public boolean hasCapturedBlaze() {
		return capturedBlaze;
	}

}

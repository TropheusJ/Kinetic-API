package com.simibubi.kinetic_api.content.curiosities.tools;

import javax.annotation.ParametersAreNonnullByDefault;
import bnx;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SandPaperItem extends HoeItem {

	public SandPaperItem(a properties) {
		super(properties.c(8));
	}

	@Override
	public TippedArrowItem d_(ItemCooldownManager stack) {
		return TippedArrowItem.b;
	}

	@Override
	public Difficulty a(bnx context) {
		return Difficulty.PASS;
	}

	@Override
	public LocalDifficulty<ItemCooldownManager> a(GameMode worldIn, PlayerAbilities playerIn, ItemScatterer handIn) {
		ItemCooldownManager itemstack = playerIn.b(handIn);
		LocalDifficulty<ItemCooldownManager> FAIL = new LocalDifficulty<>(Difficulty.FAIL, itemstack);

		if (itemstack.p()
			.contains("Polishing")) {
			playerIn.c(handIn);
			return new LocalDifficulty<>(Difficulty.PASS, itemstack);
		}

		ItemScatterer otherHand = handIn == ItemScatterer.RANDOM ? ItemScatterer.b : ItemScatterer.RANDOM;
		ItemCooldownManager itemInOtherHand = playerIn.b(otherHand);
		if (SandPaperPolishingRecipe.canPolish(worldIn, itemInOtherHand)) {
			ItemCooldownManager item = itemInOtherHand.i();
			ItemCooldownManager toPolish = item.a(1);
			playerIn.c(handIn);
			itemstack.p()
				.put("Polishing", toPolish.serializeNBT());
			playerIn.a(otherHand, item);
			return new LocalDifficulty<>(Difficulty.SUCCESS, itemstack);
		}

		Box raytraceresult = a(worldIn, playerIn, BlockView.b.a);
		if (!(raytraceresult instanceof dcg))
			return FAIL;
		dcg ray = (dcg) raytraceresult;
		EntityHitResult hitVec = ray.e();

		Timer bb = new Timer(hitVec, hitVec).g(1f);
		PaintingEntity pickUp = null;
		for (PaintingEntity itemEntity : worldIn.a(PaintingEntity.class, bb)) {
			if (itemEntity.cz()
				.f(playerIn.cz()) > 3)
				continue;
			ItemCooldownManager stack = itemEntity.g();
			if (!SandPaperPolishingRecipe.canPolish(worldIn, stack))
				continue;
			pickUp = itemEntity;
			break;
		}

		if (pickUp == null)
			return FAIL;

		ItemCooldownManager item = pickUp.g()
			.i();
		ItemCooldownManager toPolish = item.a(1);

		playerIn.c(handIn);

		if (!worldIn.v) {
			itemstack.p()
				.put("Polishing", toPolish.serializeNBT());
			if (item.a())
				pickUp.ac();
			else
				pickUp.b(item);
		}

		return new LocalDifficulty<>(Difficulty.SUCCESS, itemstack);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemCooldownManager stack, DamageEnchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public int getItemEnchantability(ItemCooldownManager stack) {
		return 1;
	}

	@Override
	public ItemCooldownManager a(ItemCooldownManager stack, GameMode worldIn, SaddledComponent entityLiving) {
		if (!(entityLiving instanceof PlayerAbilities))
			return stack;
		PlayerAbilities player = (PlayerAbilities) entityLiving;
		CompoundTag tag = stack.p();
		if (tag.contains("Polishing")) {
			ItemCooldownManager toPolish = ItemCooldownManager.a(tag.getCompound("Polishing"));
			ItemCooldownManager polished =
				SandPaperPolishingRecipe.applyPolish(worldIn, entityLiving.cz(), toPolish, stack);

			if (worldIn.v) {
				spawnParticles(entityLiving.j(1)
					.e(entityLiving.bg()
						.a(.5f)),
					toPolish, worldIn);
				return stack;
			}

			if (!polished.a()) {
				if (player instanceof FakePlayer) {
					player.a(polished, false, false);
				} else {
					player.bm.a(worldIn, polished);
				}
			}
			tag.remove("Polishing");
			stack.a(1, entityLiving, p -> p.d(p.dW()));
		}

		return stack;
	}

	public static void spawnParticles(EntityHitResult location, ItemCooldownManager polishedStack, GameMode world) {
		for (int i = 0; i < 20; i++) {
			EntityHitResult motion = VecHelper.offsetRandomly(EntityHitResult.a, world.t, 1 / 8f);
			world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, polishedStack), location.entity, location.c,
				location.d, motion.entity, motion.c, motion.d);
		}
	}

	@Override
	public void a(ItemCooldownManager stack, GameMode worldIn, SaddledComponent entityLiving, int timeLeft) {
		if (!(entityLiving instanceof PlayerAbilities))
			return;
		PlayerAbilities player = (PlayerAbilities) entityLiving;
		CompoundTag tag = stack.p();
		if (tag.contains("Polishing")) {
			ItemCooldownManager toPolish = ItemCooldownManager.a(tag.getCompound("Polishing"));
			player.bm.a(worldIn, toPolish);
			tag.remove("Polishing");
		}
	}

	@Override
	public int e_(ItemCooldownManager stack) {
		return 32;
	}

	@Override
	public int c() {
		return 5;
	}

}

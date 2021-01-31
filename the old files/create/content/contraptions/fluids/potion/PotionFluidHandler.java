package com.simibubi.kinetic_api.content.contraptions.fluids.potion;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluid.BottleType;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.fluid.FluidIngredient;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.DamageModifierStatusEffect;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Wearable;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.Potion;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluidHandler {

	public static Pair<FluidStack, ItemCooldownManager> emptyPotion(ItemCooldownManager stack, boolean simulate) {
		FluidStack fluid = getFluidFromPotionItem(stack);
		if (!simulate)
			stack.g(1);
		return Pair.of(fluid, new ItemCooldownManager(AliasedBlockItem.nw));
	}

	public static FluidIngredient potionIngredient(Wearable potion, int amount) {
		return FluidIngredient.fromFluidStack(FluidHelper.copyStackWithAmount(PotionFluidHandler
			.getFluidFromPotionItem(WrittenBookItem.a(new ItemCooldownManager(AliasedBlockItem.nv), potion)), amount));
	}

	public static FluidStack getFluidFromPotionItem(ItemCooldownManager stack) {
		Wearable potion = WrittenBookItem.d(stack);
		List<InstantStatusEffect> list = WrittenBookItem.b(stack);
		FluidStack fluid = PotionFluid.withEffects(250, potion, list);
		BottleType bottleTypeFromItem = bottleTypeFromItem(stack);
		if (potion == Potion.effects && list.isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
			return new FluidStack(FlowableFluid.c, fluid.getAmount());
		NBTHelper.writeEnum(fluid.getOrCreateTag(), "Bottle", bottleTypeFromItem);
		return fluid;
	}

	public static BottleType bottleTypeFromItem(ItemCooldownManager stack) {
		HoeItem item = stack.b();
		if (item == AliasedBlockItem.qm)
			return BottleType.LINGERING;
		if (item == AliasedBlockItem.qj)
			return BottleType.SPLASH;
		return BottleType.REGULAR;
	}

	public static GameRules itemFromBottleType(BottleType type) {
		switch (type) {
		case LINGERING:
			return AliasedBlockItem.qm;
		case SPLASH:
			return AliasedBlockItem.qj;
		case REGULAR:
		default:
			return AliasedBlockItem.nv;
		}
	}

	public static int getRequiredAmountForFilledBottle(ItemCooldownManager stack, FluidStack availableFluid) {
		return 250;
	}

	public static ItemCooldownManager fillBottle(ItemCooldownManager stack, FluidStack availableFluid) {
		CompoundTag tag = availableFluid.getOrCreateTag();
		ItemCooldownManager potionStack = new ItemCooldownManager(itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class)));
		WrittenBookItem.a(potionStack, WrittenBookItem.c(tag));
		WrittenBookItem.a(potionStack, WrittenBookItem.b(tag));
		return potionStack;
	}

	public static Text getPotionName(FluidStack fs) {
		CompoundTag tag = fs.getOrCreateTag();
		GameRules itemFromBottleType = itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
		return new TranslatableText(WrittenBookItem.c(tag)
			.b(itemFromBottleType.h()
				.a() + ".effect."));
	}

	// Modified version of PotionUtils#addPotionTooltip
	@Environment(EnvType.CLIENT)
	public static void addPotionTooltip(FluidStack fs, List<Text> tooltip, float p_185182_2_) {
		List<InstantStatusEffect> list = WrittenBookItem.a(fs.getOrCreateTag());
		List<StringIdentifiable<String, EntityAttribute>> list1 = Lists.newArrayList();
		if (list.isEmpty()) {
			tooltip.add((new TranslatableText("effect.none")).formatted(Formatting.GRAY));
		} else {
			for (InstantStatusEffect effectinstance : list) {
				TranslatableText textcomponent = new TranslatableText(effectinstance.g());
				DamageModifierStatusEffect effect = effectinstance.a();
				Map<SpawnRestriction, EntityAttribute> map = effect.g();
				if (!map.isEmpty()) {
					for (Entry<SpawnRestriction, EntityAttribute> entry : map.entrySet()) {
						EntityAttribute attributemodifier = entry.getValue();
						EntityAttribute attributemodifier1 = new EntityAttribute(attributemodifier.b(),
							effect.a(effectinstance.c(), attributemodifier),
							attributemodifier.c());
						list1.add(new StringIdentifiable<>(
							entry.getKey().c(),
							attributemodifier1));
					}
				}

				if (effectinstance.c() > 0) {
					textcomponent.append(" ")
						.append(new TranslatableText("potion.potency." + effectinstance.c()).getString());
				}

				if (effectinstance.b() > 20) {
					textcomponent.append(" (")
						.append(StatusEffect.a(effectinstance, p_185182_2_))
						.append(")");
				}

				tooltip.add(textcomponent.formatted(effect.e()
					.a()));
			}
		}

		if (!list1.isEmpty()) {
			tooltip.add(new LiteralText(""));
			tooltip.add((new TranslatableText("potion.whenDrank")).formatted(Formatting.DARK_PURPLE));

			for (StringIdentifiable<String, EntityAttribute> tuple : list1) {
				EntityAttribute attributemodifier2 = tuple.b();
				double d0 = attributemodifier2.d();
				double d1;
				if (attributemodifier2.c() != EntityAttribute.a.b
					&& attributemodifier2.c() != EntityAttribute.a.c) {
					d1 = attributemodifier2.d();
				} else {
					d1 = attributemodifier2.d() * 100.0D;
				}

				if (d0 > 0.0D) {
					tooltip.add((new TranslatableText(
						"attribute.modifier.plus." + attributemodifier2.c()
							.a(),
						ItemCooldownManager.c.format(d1),
						new TranslatableText(tuple.a())))
							.formatted(Formatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					tooltip.add((new TranslatableText(
						"attribute.modifier.take." + attributemodifier2.c()
							.a(),
						ItemCooldownManager.c.format(d1),
						new TranslatableText(tuple.a())))
							.formatted(Formatting.RED));
				}
			}
		}

	}

}

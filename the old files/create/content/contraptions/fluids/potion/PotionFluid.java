package com.simibubi.kinetic_api.content.contraptions.fluids.potion;

import java.util.Collection;
import java.util.List;

import com.simibubi.kinetic_api.AllFluids;
import com.simibubi.kinetic_api.content.contraptions.fluids.VirtualFluid;
import cut;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.item.Wearable;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionFluid extends VirtualFluid {

	public enum BottleType {
		REGULAR, SPLASH, LINGERING;
	}
	 
	public PotionFluid(Properties properties) {
		super(properties);
	}

	public static FluidStack withEffects(int amount, Wearable potion, List<InstantStatusEffect> customEffects) {
		FluidStack fluidStack = new FluidStack(AllFluids.POTION.get()
			.e(), amount);
		addPotionToFluidStack(fluidStack, potion);
		appendEffects(fluidStack, customEffects);
		return fluidStack;
	}

	public static class PotionFluidAttributes extends FluidAttributes {

		public PotionFluidAttributes(Builder builder, cut fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(FluidStack stack) {
			CompoundTag tag = stack.getOrCreateTag();
			int color = WrittenBookItem.a(WrittenBookItem.a(tag)) | 0xff000000;
			return color;
		}

	}

	public static FluidStack addPotionToFluidStack(FluidStack fs, Wearable potion) {
		Identifier resourcelocation = ForgeRegistries.POTION_TYPES.getKey(potion);
		if (potion == Potion.baseName) {
			fs.removeChildTag("Potion");
			return fs;
		}
		fs.getOrCreateTag()
			.putString("Potion", resourcelocation.toString());
		return fs;
	}

	public static FluidStack appendEffects(FluidStack fs, Collection<InstantStatusEffect> customEffects) {
		if (customEffects.isEmpty())
			return fs;
		CompoundTag compoundnbt = fs.getOrCreateTag();
		ListTag listnbt = compoundnbt.getList("CustomPotionEffects", 9);
		for (InstantStatusEffect effectinstance : customEffects)
			listnbt.add(effectinstance.a(new CompoundTag()));
		compoundnbt.put("CustomPotionEffects", listnbt);
		return fs;
	}

}

package com.simibubi.kinetic_api.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.kinetic_api.content.logistics.item.filter.ItemAttribute;
import cut;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidContentsAttribute implements ItemAttribute {
    public static final FluidContentsAttribute EMPTY = new FluidContentsAttribute(null);

    private final cut fluid;

    public FluidContentsAttribute(@Nullable cut fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        return extractFluids(itemStack).contains(fluid);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        return extractFluids(itemStack).stream().map(FluidContentsAttribute::new).collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_fluid";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if(fluid != null)
            parameter = new TranslatableText(fluid.getAttributes().getTranslationKey()).getString();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        if (fluid == null)
            return;
        Identifier id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null)
            return;
        nbt.putString("id", id.toString());
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return nbt.contains("id") ? new FluidContentsAttribute(ForgeRegistries.FLUIDS.getValue(Identifier.tryParse(nbt.getString("id")))) : EMPTY;
    }

    private List<cut> extractFluids(ItemCooldownManager stack) {
        List<cut> fluids = new ArrayList<>();

        LazyOptional<IFluidHandlerItem> capability =
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        capability.ifPresent((cap) -> {
            for(int i = 0; i < cap.getTanks(); i++) {
                fluids.add(cap.getFluidInTank(i).getFluid());
            }
        });

        return fluids;
    }
}

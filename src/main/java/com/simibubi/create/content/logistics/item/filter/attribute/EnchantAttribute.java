package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantAttribute implements ItemAttribute {
    public static final EnchantAttribute EMPTY = new EnchantAttribute(null);

    private final DamageEnchantment enchantment;

    public EnchantAttribute(@Nullable DamageEnchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        return EfficiencyEnchantment.a(itemStack).containsKey(enchantment);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        return EfficiencyEnchantment.a(itemStack).keySet().stream().map(EnchantAttribute::new).collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if(enchantment != null)
            parameter = new TranslatableText(enchantment.g()).getString();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        if (enchantment == null)
            return;
        Identifier id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null)
            return;
        nbt.putString("id", id.toString());
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return nbt.contains("id") ? new EnchantAttribute(ForgeRegistries.ENCHANTMENTS.getValue(Identifier.tryParse(nbt.getString("id")))) : EMPTY;
    }
}

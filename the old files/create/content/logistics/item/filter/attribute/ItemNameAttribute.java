package com.simibubi.kinetic_api.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import com.google.gson.JsonParseException;
import com.simibubi.kinetic_api.content.logistics.item.filter.ItemAttribute;

public class ItemNameAttribute implements ItemAttribute {
    String itemName;

    public ItemNameAttribute(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        return extractCustomName(itemStack).equals(itemName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        String name = extractCustomName(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if(name.length() > 0) {
            atts.add(new ItemNameAttribute(name));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "has_name";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { itemName };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("name", this.itemName);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new ItemNameAttribute(nbt.getString("name"));
    }

    private String extractCustomName(ItemCooldownManager stack) {
        CompoundTag compoundnbt = stack.b("display");
        if (compoundnbt != null && compoundnbt.contains("Name", 8)) {
            try {
                Text itextcomponent = Text.Serializer.fromJson(compoundnbt.getString("Name"));
                if (itextcomponent != null) {
                    return itextcomponent.getString();
                }
            } catch (JsonParseException ignored) {
            }
        }
        return "";
    }
}

package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.LilyPadItem;
import net.minecraft.nbt.CompoundTag;
import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

public class BookCopyAttribute implements ItemAttribute {
    int generation;

    public BookCopyAttribute(int generation) {
        this.generation = generation;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        return extractGeneration(itemStack) == generation;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        int generation = extractGeneration(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if(generation >= 0) {
            atts.add(new BookCopyAttribute(generation));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        switch(generation){
            case 0:
                return "book_copy_original";
            case 1:
                return "book_copy_first";
            case 2:
                return "book_copy_second";
            default:
                return "book_copy_tattered";
        }
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putInt("generation", this.generation);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new BookCopyAttribute(nbt.getInt("generation"));
    }

    private int extractGeneration(ItemCooldownManager stack) {
        CompoundTag nbt = stack.o();
        if (nbt != null && stack.b() instanceof LilyPadItem) {
            return nbt.getInt("generation");
        }
        return -1;
    }
}

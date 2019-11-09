package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.config.ConfigHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ExtendedItemStackHandler extends ItemStackHandler {

  public ExtendedItemStackHandler() {
    this(1);
  }

  public ExtendedItemStackHandler(int size) {
    super(size);
  }

  public ExtendedItemStackHandler(NonNullList<ItemStack> stacks) {
    super(stacks);
  }

  @Override
  public int getSlotLimit(int slot) {
    return 64 * ConfigHandler.serverSideConfig.RadiantMultiplier;
  }

  @Override
  public int getStackLimit(int slot, @Nonnull ItemStack stack) {
    return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * ConfigHandler.serverSideConfig.RadiantMultiplier);
  }

  @Override
  public void onContentsChanged(int slot) {

  }

  @Nonnull
  @Override
  // This specifically allows for extraction of more than the max stack size
  // from a chest.
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }

    validateSlotIndex(slot);

    ItemStack existing = this.stacks.get(slot);

    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }

    int toExtract;

    if (existing.getMaxStackSize() == 1) {
      toExtract = Math.min(1, amount);
    } else {
      toExtract = Math.min(amount, existing.getMaxStackSize() * ConfigHandler.serverSideConfig.RadiantMultiplier);
    }

    if (existing.getCount() <= toExtract) {
      if (!simulate) {
        this.stacks.set(slot, ItemStack.EMPTY);
        onContentsChanged(slot);
      }
      return existing;
    } else {
      if (!simulate) {
        this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
        onContentsChanged(slot);
      }

      return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
    }
  }

  @Override
  public CompoundNBT serializeNBT() {
    ListNBT nbtTagList = new ListNBT();
    for (int i = 0; i < stacks.size(); i++) {
      if (!stacks.get(i).isEmpty()) {
        short realCount = (short) Math.min(Short.MAX_VALUE, stacks.get(i).getCount());
        CompoundNBT itemTag = new CompoundNBT();
        itemTag.setInteger("Slot", i);
        stacks.get(i).writeToNBT(itemTag);
        itemTag.setShort("ExtendedCount", realCount);
        nbtTagList.appendTag(itemTag);
      }
    }
    CompoundNBT nbt = new CompoundNBT();
    nbt.setTag("Items", nbtTagList);
    nbt.setInteger("Size", stacks.size());
    return nbt;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
    ListNBT tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
    for (int i = 0; i < tagList.tagCount(); i++) {
      CompoundNBT itemTags = tagList.getCompoundTagAt(i);
      int slot = itemTags.getInteger("Slot");

      if (slot >= 0 && slot < stacks.size()) {
        if (itemTags.hasKey("StackList", Constants.NBT.TAG_LIST)) { // migrate from old ExtendedItemStack system
          ItemStack stack = ItemStack.EMPTY;
          ListNBT stackTagList = itemTags.getTagList("StackList", Constants.NBT.TAG_COMPOUND);
          for (int j = 0; j < stackTagList.tagCount(); j++) {
            CompoundNBT itemTag = stackTagList.getCompoundTagAt(j);
            ItemStack temp = new ItemStack(itemTag);
            if (!temp.isEmpty()) {
              if (stack.isEmpty()) {
                stack = temp;
              } else {
                stack.grow(temp.getCount());
              }
            }
          }
          if (!stack.isEmpty()) {
            int count = stack.getCount();
            count = Math.min(count, getStackLimit(slot, stack));
            stack.setCount(count);

            stacks.set(slot, stack);
          }
        } else {
          ItemStack stack = new ItemStack(itemTags);
          if (itemTags.hasKey("ExtendedCount", Constants.NBT.TAG_SHORT)) {
            stack.setCount(itemTags.getShort("ExtendedCount"));
          }
          stacks.set(slot, stack);
        }
      }
    }
    onLoad();
  }

  public int calcRedstone() {
    int numStacks = 0;
    float f = 0F;

    for (int slot = 0; slot < this.getSlots(); slot++) {
      ItemStack stack = this.getStackInSlot(slot);

      if (!stack.isEmpty()) {
        f += (float) stack.getCount() / (float) this.getStackLimit(slot, stack);
        numStacks++;
      }
    }

    f /= this.getSlots();
    return MathHelper.floor(f * 14F) + (numStacks > 0 ? 1 : 0);
  }

}

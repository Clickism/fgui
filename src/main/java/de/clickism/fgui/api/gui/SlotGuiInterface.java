package de.clickism.fgui.api.gui;

import de.clickism.fgui.api.ClickType;
import de.clickism.fgui.api.SlotHolder;
import de.clickism.fgui.api.elements.GuiElementInterface;
import de.clickism.fgui.virtual.inventory.VirtualScreenHandler;
import de.clickism.fgui.virtual.inventory.VirtualSlot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface SlotGuiInterface extends SlotHolder, GuiInterface {

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the inventory size
     */
    int getSize();

    boolean getLockPlayerInventory();

    void setLockPlayerInventory(boolean value);

    /**
     * Used internally to receive clicks from the client.
     *
     * @see SlotGuiInterface#onClick(int, ClickType, net.minecraft.world.inventory.ClickType, GuiElementInterface)
     * @see SlotGuiInterface#onAnyClick(int, ClickType, net.minecraft.world.inventory.ClickType)
     */
    @ApiStatus.Internal
    default boolean click(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            element.getGuiCallback().click(index, type, action, this);
        }
        return this.onClick(index, type, action, element);
    }

    /**
     * Executes when player clicks any slot.
     *
     * @param index  the slot index
     * @param type   the simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return <code>true</code> if to allow manipulation of redirected slots, otherwise <code>false</code>
     */
    default boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        return true;
    }

    /**
     * Executed when player clicks a {@link GuiElementInterface}
     *
     * @param index   slot index
     * @param type    Simplified type of click
     * @param action  Minecraft's Slot Action Type
     * @param element Clicked GuiElement
     * @return Returns false, for automatic handling and syncing or true, if you want to do it manually
     */
    default boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element) {
        return false;
    }

    /**
     * Whether spectators can click on slots.
     *
     * @return Returns true if spectators can use this gui.
     */
    default boolean canSpectatorsClick() {
        return true;
    }

    /**
     * Maps a hotbar index into a slot index.
     *
     * @param slots The number of slots in the screen handler.
     * @param index The hotbar index, this should be [0-8]
     * @return The mapped slot index
     */
    default int getHotbarSlotIndex(int slots, int index) {
        return slots + index - 9;
    }

    /**
     * Gets the offhand slot index
     *
     * @return The offhand slot index
     */
    default int getOffhandSlotIndex() {
        return -1;
    }

    @Nullable
    default Slot getSlotRedirectOrPlayer(int index) {
        if (index < this.getSize()) {
            return this.getSlotRedirect(index);
        }

        if (this.getPlayer().containerMenu instanceof VirtualScreenHandler virt && virt.getGui() == this && index < virt.slots.size()) {
            return virt.slots.get(index);
        }
        return null;
    }

    default ItemStack quickMove(int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.getSlotRedirectOrPlayer(index);
        if (slot != null && slot.hasItem() && !(slot instanceof VirtualSlot)) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < this.getVirtualSize()) {
                if (!this.insertItem(itemStack2, this.getVirtualSize(), this.getVirtualSize() + 9 * 4, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, this.getVirtualSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        } else if (slot instanceof VirtualSlot) {
            return slot.getItem();
        }

        return itemStack;
    }

    default boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean modified = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                var slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null && slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stack, stackInSlot)) {
                        var totalCount = stackInSlot.getCount() + stack.getCount();
                        var maxSize = slot.getMaxStackSize(stackInSlot);
                        if (totalCount <= maxSize) {
                            stack.setCount(0);
                            stackInSlot.setCount(totalCount);
                            slot.setChanged();
                            modified = true;
                        } else if (stackInSlot.getCount() < maxSize) {
                            stack.shrink(maxSize - stackInSlot.getCount());
                            stackInSlot.setCount(maxSize);
                            slot.setChanged();
                            modified = true;
                        }
                    }
                }

                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (fromLast ? i >= startIndex : i < endIndex) {
                var slot = this.getSlotRedirectOrPlayer(i);
                if (slot != null && slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (stackInSlot.isEmpty() && slot.mayPlace(stack)) {
                        int maxSize = slot.getMaxStackSize(stack);
                        slot.setByPlayer(stack.split(Math.min(stack.getCount(), maxSize)));
                        slot.setChanged();
                        modified = true;
                        break;
                    }
                }

                if (fromLast) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        return modified;
    }

   default boolean insertItem(ItemStack stack, List<Slot> slots, boolean fromLast) {
        boolean modified = false;

        if (fromLast) {
            slots = slots.reversed();
        }

        if (stack.isStackable()) {
            for (var slot : slots) {
                if (stack.isEmpty()) {
                    break;
                }

                if (slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stack, stackInSlot)) {
                        var totalCount = stackInSlot.getCount() + stack.getCount();
                        var maxSize = slot.getMaxStackSize(stackInSlot);
                        if (totalCount <= maxSize) {
                            stack.setCount(0);
                            stackInSlot.setCount(totalCount);
                            slot.setChanged();
                            modified = true;
                        } else if (stackInSlot.getCount() < maxSize) {
                            stack.shrink(maxSize - stackInSlot.getCount());
                            stackInSlot.setCount(maxSize);
                            slot.setChanged();
                            modified = true;
                        }
                    }
                }
            }
        }

        if (!stack.isEmpty()) {
            for (var slot : slots) {
                if (slot.mayPlace(stack)) {
                    var stackInSlot = slot.getItem();
                    if (stackInSlot.isEmpty() && slot.mayPlace(stack)) {
                        var maxSize = slot.getMaxStackSize(stack);
                        slot.setByPlayer(stack.split(Math.min(stack.getCount(), maxSize)));
                        slot.setChanged();
                        modified = true;
                        break;
                    }
                }
            }
        }

        return modified;
    }
}

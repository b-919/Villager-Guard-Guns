package com.b919.guardsgun.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;

public abstract class PagedList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    protected PagedList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @Override
    public int getRowWidth() {
        return Math.max(100, getWidth() - 24);
    }

    @Override
    protected int getScrollbarPosition() {
        return getRowLeft() + getRowWidth() + 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)
                && mouseX >= getScrollbarPosition() && mouseX < getScrollbarPosition() + 6
                && getMaxScroll() > 0) {
            int thumbHeight = Mth.clamp(
                    (int) ((float) (getHeight() * getHeight()) / getMaxPosition()),
                    32, getHeight() - 8);
            int thumbTop = (int) getScrollAmount() * (getHeight() - thumbHeight)
                    / Math.max(1, getMaxScroll()) + getRowTop(0) - 4 + (int) getScrollAmount();
            if (mouseY < thumbTop) {
                setScrollAmount(getScrollAmount() - getHeight());
                return true;
            }
            if (mouseY > thumbTop + thumbHeight) {
                setScrollAmount(getScrollAmount() + getHeight());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

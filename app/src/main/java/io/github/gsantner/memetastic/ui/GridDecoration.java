package io.github.gsantner.memetastic.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Decoration to set a border around a View
 */
public class GridDecoration extends RecyclerView.ItemDecoration {
    private int halfSpace;

    /**
     * Constructor
     *
     * @param space the border around a view in pixels
     */
    public GridDecoration(int space) {
        this.halfSpace = space / 2;
    }

    /**
     * set the border around the View
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (parent.getPaddingLeft() != halfSpace) {
            parent.setPadding(halfSpace, halfSpace, halfSpace, halfSpace);
            parent.setClipToPadding(false);
        }

        outRect.top = halfSpace;
        outRect.bottom = halfSpace;
        outRect.left = halfSpace;
        outRect.right = halfSpace;
    }

}

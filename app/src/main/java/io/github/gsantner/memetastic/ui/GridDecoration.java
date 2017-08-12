package io.github.gsantner.memetastic.ui;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Decoration to set a border around a View
 */
public class GridDecoration extends RecyclerView.ItemDecoration {
    private final int _borderSpace;

    /**
     * Constructor
     *
     * @param borderSize the border around a view in dp
     */
    public GridDecoration(float borderSize) {
        _borderSpace = (int) (borderSize * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * set the border around the View
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getPaddingLeft() != _borderSpace) {
            parent.setPadding(_borderSpace, _borderSpace, _borderSpace, _borderSpace);
            parent.setClipToPadding(false);
        }

        outRect.top = _borderSpace;
        outRect.bottom = _borderSpace;
        outRect.left = _borderSpace;
        outRect.right = _borderSpace;
    }

}

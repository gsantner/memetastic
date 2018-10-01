/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: GNU GPLv3 (Commercial upon request)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
#########################################################*/
package net.gsantner.memetastic.ui;

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

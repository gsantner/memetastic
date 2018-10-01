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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.ParcelableSpan;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.gsantner.memetastic.data.MemeData;

import java.util.List;

/**
 * Adapter to show the available fonts rendered in there own style as preview
 */
public class FontItemAdapter extends ArrayAdapter<MemeData.Font> {
    private boolean _showCustomSelectedText;
    private String _customSelectedText;
    private List<MemeData.Font> _fontList;

    public FontItemAdapter(Context context, int resource, List<MemeData.Font> fontList) {
        this(context, resource, fontList, false, "");
    }

    public FontItemAdapter(Context context, int resource, List<MemeData.Font> fontList, boolean showCustomSelectedText, String customSelectedText) {
        super(context, resource, fontList);
        _fontList = fontList;
        _showCustomSelectedText = showCustomSelectedText;
        _customSelectedText = customSelectedText;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = getTheView(position, convertView, parent);
        if (_showCustomSelectedText) {
            ((TextView) v).setText(_customSelectedText);
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    // set how the item should look like (rendered in own conf)
    private View getTheView(int position, View convertView, ViewGroup parent) {
        MemeData.Font item = getItem(position);
        TextView textview = (TextView) super.getDropDownView(position, convertView, parent);
        if (item != null && item.conf != null) {
            String fontName = item.conf.getTitle();
            String fontDescription = item.conf.getDescription();

            if (fontName.contains("_") && !fontName.endsWith("_")) ;
            fontName = fontName.substring(fontName.indexOf('_') + 1);

            textview.setTypeface(item.typeFace);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

            SpannableString spannedText = null;
            if (TextUtils.isEmpty(fontDescription)) {
                spannedText = new SpannableString(fontName);
            } else {
                fontName += "\n" + fontDescription;
                spannedText = new SpannableString(fontName);
                ParcelableSpan[] spanMods = new ParcelableSpan[]{
                        new RelativeSizeSpan(0.7f),
                        new ForegroundColorSpan(Color.GRAY),
                        new StyleSpan(Typeface.NORMAL),
                        new TypefaceSpan("sans-serif")
                };
                for (ParcelableSpan spanMod : spanMods) {
                    spannedText.setSpan(spanMod, fontName.indexOf("\n"), fontName.length(), 0);
                }
            }
            textview.setText(spannedText);
        }
        return textview;
    }

    public void setSelectedFont(Spinner spinner, MemeData.Font font) {
        for (int i = 0; i < _fontList.size(); i++) {
            if (_fontList.get(i).equals(font)) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
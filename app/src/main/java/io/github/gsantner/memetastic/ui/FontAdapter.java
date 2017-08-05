package io.github.gsantner.memetastic.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.github.gsantner.memetastic.data.MemeFont;

/**
 * Adapter to show the available fonts rendered in there own style as preview
 */
public class FontAdapter extends ArrayAdapter<MemeFont> {
    boolean _showCustomSelectedText;
    String _customSelectedText;

    public FontAdapter(Context context, int resource, List<MemeFont> fontList) {
        this(context, resource, fontList, false, "");
    }

    public FontAdapter(Context context, int resource, List<MemeFont> fontList, boolean showCustomSelectedText, String customSelectedText) {
        super(context, resource, fontList);
        _showCustomSelectedText = showCustomSelectedText;
        _customSelectedText = customSelectedText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = getTheView(position, convertView, parent);
        if (_showCustomSelectedText) {
            ((TextView) v).setText(_customSelectedText);
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    // set how the item should look like (rendered in own font)
    private View getTheView(int position, View convertView, ViewGroup parent) {
        String fontName = getItem(position).getFontName();
        if (fontName.contains("_") && !fontName.endsWith("_")) ;
        fontName = fontName.substring(fontName.indexOf('_') + 1);

        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setTypeface(getItem(position).getFont());
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        view.setText(fontName);
        return view;
    }
}
package io.github.gsantner.memetastic.ui;

import android.content.Context;
import android.graphics.Typeface;
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
public class FontAdapter extends ArrayAdapter<MemeFont<Typeface>> {
    public FontAdapter(Context context, int resource, List<MemeFont<Typeface>> fontList) {
        super(context, resource, fontList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
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
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        view.setText(fontName);
        return view;
    }
}
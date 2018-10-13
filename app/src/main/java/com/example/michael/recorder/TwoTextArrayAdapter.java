package com.example.michael.recorder;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.util.List;

public class TwoTextArrayAdapter extends ArrayAdapter<Item> {
    private LayoutInflater mInflater;
    private OnClickDeleteButtonListener listener;

    public enum RowType {
        LIST_ITEM, HEADER_ITEM
    }

    public TwoTextArrayAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getViewTypeCount() {
        return RowType.values().length;

    }
    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = getItem(position).getView(mInflater, convertView);

        return getItem(position).getView(mInflater, convertView);
    }

}
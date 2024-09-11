package com.simple.top.autocalc.item.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.simple.top.autocalc.R;
import com.simple.top.autocalc.item.ConvertsListItem;
import com.simple.top.autocalc.item.ConvertsListViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

public class ConvertsListAdapter extends ArrayAdapter<ConvertsListItem> {

  private int layoytId;

  public ConvertsListAdapter(Context context, int layoutId, List<ConvertsListItem> list) {
    super(context, layoutId, list);
    this.layoytId = layoutId;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    final ConvertsListItem item = getItem(position);

    ConvertsListViewHolder viewHolder;

    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(layoytId, parent, false);
      viewHolder = new ConvertsListViewHolder();
      viewHolder.text = convertView.findViewById(R.id.text_title);
      viewHolder.image = convertView.findViewById(R.id.image);

      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ConvertsListViewHolder) convertView.getTag();
    }

    if (item != null) {
      viewHolder.text.setText(item.text);
      viewHolder.image.setImageDrawable(item.image);
    }

    return convertView;
  }
}

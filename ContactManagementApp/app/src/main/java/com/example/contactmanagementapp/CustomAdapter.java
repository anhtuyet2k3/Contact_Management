package com.example.contactmanagementapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Item> {
    private Context mContext;
    private int mResource;
    private ArrayList<Item> mItems;

    public CustomAdapter(Context context, int resource, ArrayList<Item> items) {
        super(context, resource, items);
        this.mContext = context;
        this.mResource = resource;
        this.mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.textViewName = convertView.findViewById(R.id.textViewName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item currentItem = mItems.get(position);

        // Display name
        holder.textViewName.setText(currentItem.getName());

        // Display image using file path
        String imagePath = currentItem.getImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Check if file exists
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                // Load image using Picasso
                holder.imageView.setVisibility(View.VISIBLE);
                Picasso.get().load(imgFile).into(holder.imageView);
            } else {
                // Display default image or hide ImageView if file doesn't exist
                holder.imageView.setImageResource(R.drawable.ic_user);
            }
        } else {
            // Display default image or hide ImageView if no image path
            holder.imageView.setImageResource(R.drawable.ic_user);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textViewName;
    }
}


package com.example.contactmanagementapp;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UnitAdapter extends ArrayAdapter<Unit>{
    private Context context;
    private List<Unit> units;

    public UnitAdapter(@NonNull Context context, ArrayList<Unit> units) {
        super(context, 0, units);
        this.context = context;
        this.units = units;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Unit unit = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.textViewName);
        ImageView imageView = convertView.findViewById(R.id.imageView);


        nameTextView.setText(unit.getName());

        if (unit.getImageUri() != null) {
            imageView.setImageURI(unit.getImageUri());
        } else {
            imageView.setImageResource(R.drawable.ic_user); // Ảnh mặc định
        }

        return convertView;
    }
}

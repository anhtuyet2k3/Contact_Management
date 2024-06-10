package com.example.contactmanagementapp;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends ArrayAdapter<Employee> implements Filterable {
    private List<Employee> originalList;
    private List<Employee> filteredList;
    private EmployeeFilter filter;

    public EmployeeAdapter(Context context, List<Employee> employees) {
        super(context, 0, employees);
        this.originalList = new ArrayList<>(employees);
        this.filteredList = new ArrayList<>(employees);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new EmployeeFilter();
        }
        return filter;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Employee getItem(int position) {
        return filteredList.get(position);
    }

    // Cài đặt bộ lọc tùy chỉnh cho EmployeeAdapter
    private class EmployeeFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Employee> filteredResults = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredResults.addAll(originalList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Employee employee : originalList) {
                    if (employee.getName().toLowerCase().contains(filterPattern)) {
                        filteredResults.add(employee);
                    }
                }
            }

            results.values = filteredResults;
            results.count = filteredResults.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            filteredList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    }
}

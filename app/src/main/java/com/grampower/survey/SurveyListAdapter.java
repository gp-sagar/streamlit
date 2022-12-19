package com.grampower.survey;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class SurveyListAdapter  extends BaseAdapter {

    List<String> options;
    Activity context;
    boolean[] itemChecked;

    public SurveyListAdapter(Activity context, List<String> options) {
        super();
        this.context = context;
        this.options = options;
        itemChecked = new boolean[options.size()];
    }

    private class ViewHolder {
        TextView textDescrip;
        CheckBox ck1;
    }

    public int getCount() {
        return options.size();
    }

    public Object getItem(int position) {
        return options.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_survey, null);
            holder = new ViewHolder();
            holder.textDescrip = (TextView) convertView.findViewById(R.id.element);
            holder.ck1 = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textDescrip.setText(options.get(position));
        holder.ck1.setChecked(false);

        if (itemChecked[position])
            holder.ck1.setChecked(true);
        else
            holder.ck1.setChecked(false);

        holder.ck1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.ck1.isChecked())
                    itemChecked[position] = true;
                else
                    itemChecked[position] = false;
            }
        });
        return convertView;

    }

    public String asString() {
        StringBuilder result = new StringBuilder();
        for(int i=0; i<itemChecked.length; i++) {
            if (itemChecked[i]) {
                result.append(options.get(i) + "\n");
            }
        }
        return result.toString();
    }
}
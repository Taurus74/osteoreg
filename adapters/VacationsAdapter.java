package com.aconst.spinareg.adapters;

import android.app.TimePickerDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.aconst.spinareg.CalendarHelper;
import com.aconst.spinareg.R;
import com.aconst.spinareg.model.OptionsWeekDay;

import java.util.LinkedList;
import java.util.List;

public class WeekdaysAdapter extends RecyclerView.Adapter<WeekdaysAdapter.ViewHolder> {
    private List<OptionsWeekDay> weekDayList;

    public WeekdaysAdapter(List<OptionsWeekDay> weekDayList) {
        this.weekDayList = weekDayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.option_week_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OptionsWeekDay item = getItem(position);
        holder.swWeekDay.setText(item.getWeekDay());
        boolean isChecked = item.isEnabled();
        holder.swWeekDay.setChecked(isChecked);
        int time = item.getTimeStart();
        if (time > -1)
            holder.tvTimeStart.setText(CalendarHelper.getMinute(time));
        else
            holder.tvTimeStart.setText("");
        time = item.getTimeStop();
        if (time > -1)
            holder.tvTimeStop.setText(CalendarHelper.getMinute(time));
        else
            holder.tvTimeStop.setText("");

        holder.tvTimeStart.setClickable(isChecked);
        holder.tvTimeStart.setFocusable(isChecked);
        holder.tvTimeStop.setClickable(isChecked);
        holder.tvTimeStop.setFocusable(isChecked);

    }

    @Override
    public int getItemCount() {
        return weekDayList.size();
    }

    private OptionsWeekDay getItem(int position) {
        return weekDayList.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SwitchCompat swWeekDay;
        TextView tvTimeStart;
        TextView tvTimeStop;

        public ViewHolder(View itemView) {
            super(itemView);
            swWeekDay = itemView.findViewById(R.id.swWeekDay);
            swWeekDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    tvTimeStart.setClickable(isChecked);
                    tvTimeStart.setFocusable(isChecked);
                    tvTimeStop.setClickable(isChecked);
                    tvTimeStop.setFocusable(isChecked);
                }
            });
            tvTimeStart = itemView.findViewById(R.id.tvTimeStart);
            tvTimeStart.setOnClickListener(this);
            tvTimeStop = itemView.findViewById(R.id.tvTimeStop);
            tvTimeStop.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OptionsWeekDay item = getItem(getAdapterPosition());
            if (v.getId() == R.id.tvTimeStart)
                openTimeDialog(v, item.getTimeStart(), item);
            else if  (v.getId() == R.id.tvTimeStop)
                openTimeDialog(v, item.getTimeStop(), item);
        }

        private void openTimeDialog(final View v, int minute, final OptionsWeekDay item) {
            int hour = 0;
            int min = 0;
            if (minute > -1) {
                hour = minute / 60;
                min = minute - hour * 60;
            }
            new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    ((TextView) v).setText(CalendarHelper.getMinute(hourOfDay * 60 + minute));
                    if (v.getId() == R.id.tvTimeStart)
                        item.setTimeStart(hourOfDay * 60 + minute);
                    else
                        item.setTimeStop(hourOfDay * 60 + minute);
                }
            }, hour, min, true).show();
        }
    }
}

package com.aconst.calendarwidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class EventsFactory implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<String> events;
    private Context context;
    private EventsData eventsData;

    EventsFactory(Context context, Intent intent) {
        this.context = context;
        eventsData = new EventsData(context, intent);
    }

    @Override
    public void onCreate() {
        events = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        events.clear();
        events = eventsData.getEventsAsStringArray();
        if (events.size() == 0)
            events.add(context.getResources().getString
                    (R.string.no_events, eventsData.getDateForNoEvents())
            );
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // Настройка выводимого элемента
        String text = events.get(position);
        boolean active = false;
        if (text.charAt(0) == EventsData.ACTIVE_EVENT.charAt(0)) {
            text = text.substring(1);
            active = true;
        }
        RemoteViews rView = new RemoteViews(context.getPackageName(), R.layout.item);
        rView.setTextViewText(R.id.tvEvent, text);
        if (active)
            rView.setTextColor(R.id.tvEvent, context.getResources().getColor(R.color.colorToday));
        else
            rView.setTextColor(R.id.tvEvent,
                    context.getResources().getColor(R.color.colorSecondary));

        // Привязка данных к элементу для перехода в календарь
        Intent clickIntent = new Intent();
        clickIntent.putExtra(MyCalWidget.EVENT_ID, eventsData.getEventId(position));
        rView.setOnClickFillInIntent(R.id.tvEvent, clickIntent);

        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return eventsData.getEventId(position);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}

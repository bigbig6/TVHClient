package org.tvheadend.tvhclient.ui.recordings.timer_recordings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.tvheadend.tvhclient.R;
import org.tvheadend.tvhclient.data.Constants;
import org.tvheadend.tvhclient.data.DataStorage;
import org.tvheadend.tvhclient.data.model.Channel;
import org.tvheadend.tvhclient.data.model.TimerRecording;
import org.tvheadend.tvhclient.utils.MiscUtils;
import org.tvheadend.tvhclient.utils.UIUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO time offset when adding / editing a recording

public class TimerRecordingListAdapter extends ArrayAdapter<TimerRecording> {

    private final Activity context;
    private final List<TimerRecording> list = new ArrayList<>();
    private final int htspVersion;
    private final SharedPreferences sharedPreferences;
    private int selectedPosition = 0;

    TimerRecordingListAdapter(Activity context) {
        super(context, 0);
        this.context = context;
        this.htspVersion = DataStorage.getInstance().getProtocolVersion();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void sort(final int type) {
        switch (type) {
            case Constants.RECORDING_SORT_ASCENDING:
                sort(new Comparator<TimerRecording>() {
                    public int compare(TimerRecording x, TimerRecording y) {
                        if (x != null && y != null && x.title != null && y.title != null) {
                            return (y.title.compareTo(x.title));
                        }
                        return 0;
                    }
                });
                break;
            case Constants.RECORDING_SORT_DESCENDING:
                sort(new Comparator<TimerRecording>() {
                    public int compare(TimerRecording x, TimerRecording y) {
                        if (x != null && y != null && x.title != null && y.title != null) {
                            return (x.title.compareTo(y.title));
                        }
                        return 0;
                    }
                });
                break;
        }
    }

    public void setPosition(int pos) {
        selectedPosition = pos;
    }

    static class ViewHolder {
        @BindView(R.id.icon) ImageView iconImageView;
        @BindView(R.id.title) TextView titleTextView;
        @BindView(R.id.channel) TextView channelTextView;
        @BindView(R.id.days_of_week) TextView daysOfWeekTextView;
        @BindView(R.id.time) TextView timeTextView;
        @BindView(R.id.duration) TextView durationTextView;
        @BindView(R.id.enabled) TextView isEnabledTextView;
        @Nullable
        @BindView(R.id.dual_pane_list_item_selection) ImageView dual_pane_list_item_selection;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = context.getLayoutInflater().inflate(R.layout.timer_recording_list_adapter, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        boolean lightTheme = sharedPreferences.getBoolean("lightThemePref", true);
        boolean showChannelIcons = sharedPreferences.getBoolean("showIconPref", true);

        if (holder.dual_pane_list_item_selection != null) {
            // Set the correct indication when the dual pane mode is active
            // If the item is selected the the arrow will be shown, otherwise
            // only a vertical separation line is displayed.                
            if (selectedPosition == position) {
                final int icon = (lightTheme) ? R.drawable.dual_pane_selector_active_light : R.drawable.dual_pane_selector_active_dark;
                holder.dual_pane_list_item_selection.setBackgroundResource(icon);
            } else {
                final int icon = R.drawable.dual_pane_selector_inactive;
                holder.dual_pane_list_item_selection.setBackgroundResource(icon);
            }
        }

        // Get the program and assign all the values
        TimerRecording recording = getItem(position);
        if (recording != null) {
            Channel channel = DataStorage.getInstance().getChannelFromArray(recording.channel);
            if (!TextUtils.isEmpty(recording.title)) {
                holder.titleTextView.setText(recording.title);
            } else {
                holder.titleTextView.setText(recording.name);
            }

            if (channel != null) {
                holder.channelTextView.setText(channel.channelName);
                Bitmap iconBitmap = MiscUtils.getCachedIcon(context, channel.channelIcon);
                holder.iconImageView.setImageBitmap(iconBitmap);
                holder.iconImageView.setVisibility(showChannelIcons ? ImageView.VISIBLE : ImageView.GONE);
            } else {
                holder.channelTextView.setText(R.string.all_channels);
            }

            holder.daysOfWeekTextView.setText(UIUtils.getDaysOfWeekText(context, recording.daysOfWeek));

            String time = UIUtils.getTime(getContext(), recording.start) + " - " + UIUtils.getTime(getContext(), recording.stop);
            holder.timeTextView.setText(time);

            holder.durationTextView.setText(context.getString(R.string.minutes, recording.getDuration()));

            holder.isEnabledTextView.setVisibility(htspVersion >= 19 ? View.VISIBLE : View.GONE);
            holder.isEnabledTextView.setText(recording.enabled > 0 ? R.string.recording_enabled : R.string.recording_disabled);
        }
        return view;
    }

    public void update(TimerRecording trec) {
        int length = list.size();
        // Go through the list of programs and find the
        // one with the same id. If its been found, replace it.
        for (int i = 0; i < length; ++i) {
            if (list.get(i).id.equals(trec.id)) {
                list.set(i, trec);
                break;
            }
        }
    }

    List<TimerRecording> getAllItems() {
        return list;
    }
}
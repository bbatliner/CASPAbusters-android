package beest.caspabusters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RequestsAdapter extends ArrayAdapter<Request> {

    private ArrayList<Request> requests;

    public RequestsAdapter(Context context, int resource, ArrayList<Request> requests) {
        super(context, resource, requests);
        this.requests = requests;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v != null) {
            holder = (ViewHolder) v.getTag(R.id.TAG_VIEW_HOLDER);
        } else {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.request_item, parent, false);
            holder = new ViewHolder(v);
            v.setTag(R.id.TAG_VIEW_HOLDER, holder);
        }

        Request r = requests.get(position);

        if (holder.name != null) {
            holder.name.setText(r.name);
        }
        if (holder.hall != null) {
            holder.hall.setText(r.hall);
        }
        if (holder.wing != null) {
            holder.wing.setText(r.wing);
        }
        if (holder.earliestTime != null) {
            holder.earliestTime.setText(Request.twelveHourTimeFormat.format(r.earliestWakeTime));
        }
        if (holder.latestTime != null) {
            holder.latestTime.setText(Request.twelveHourTimeFormat.format(r.latestWakeTime));
        }

        v.setTag(R.id.TAG_PHONE_NUMBER, r.phoneNumber);

        return v;
    }

    // ButterKnife container
    static class ViewHolder {
        @Bind(R.id.request_name)
        TextView name;
        @Bind(R.id.request_hall)
        TextView hall;
        @Bind(R.id.request_wing)
        TextView wing;
        @Bind(R.id.request_earliest_time)
        TextView earliestTime;
        @Bind(R.id.request_latest_time)
        TextView latestTime;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

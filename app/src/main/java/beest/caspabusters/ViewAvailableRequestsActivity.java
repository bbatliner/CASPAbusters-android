package beest.caspabusters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static beest.caspabusters.AndroidUtil.getUTCOffsetInMillis;


public class ViewAvailableRequestsActivity extends Activity {

    @Bind(R.id.requests)
    ListView requestsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        ButterKnife.bind(this);

        CaspaBustersAPI.getAllRequests(new Callback<ArrayList<Request>>() {
            @Override
            public void success(ArrayList<Request> requests, Response response) {
                // Offset the UTC time so it is local
                long utcOffset = getUTCOffsetInMillis();
                for (Request request : requests) {
                    request.earliestWakeTime.setTime(request.earliestWakeTime.getTime() + utcOffset);
                    request.latestWakeTime.setTime(request.latestWakeTime.getTime() + utcOffset);
                }
                ArrayAdapter<Request> adapter = new RequestsAdapter(getApplicationContext(), R.layout.request_item, requests);
                requestsListView.setAdapter(adapter);
                requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Get the phone number from the view and start the next activity
                        String phoneNumber = view.getTag(R.id.TAG_PHONE_NUMBER).toString();
                        Intent intent = new Intent(ViewAvailableRequestsActivity.this, ViewRequestActivity.class);
                        Bundle data = new Bundle();
                        data.putString(getString(R.string.EXTRA_PHONE_NUMBER), phoneNumber);
                        intent.putExtras(data);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("", error.toString());
            }
        });
    }
}

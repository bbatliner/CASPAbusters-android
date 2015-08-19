package beest.caspabusters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
    }

    // Setup onClick handler for "Request" button
    @OnClick(R.id.request_button)
    void goToRequest() {
        Intent intent = new Intent(MenuActivity.this, RequestActivity.class);
        startActivity(intent);
    }

    // Setup onClick handler for "Wake Up" button
    @OnClick(R.id.view_requests_button)
    void goToViewRequests() {
        Intent intent = new Intent(MenuActivity.this, ViewAvailableRequestsActivity.class);
        startActivity(intent);
    }
}

package beest.caspabusters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ViewRequestActivity extends Activity {

    @Bind(R.id.phone_number_display)
    TextView phoneNumberDisplay;

    // The phone number this Activity should call
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        ButterKnife.bind(this);

        phoneNumber = getIntent().getStringExtra(getString(R.string.EXTRA_PHONE_NUMBER));
        phoneNumberDisplay.setText(phoneNumber);
    }

    // TODO: Make the activity layout
    @OnClick(R.id.start_wake_up_call)
    void startCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }
}

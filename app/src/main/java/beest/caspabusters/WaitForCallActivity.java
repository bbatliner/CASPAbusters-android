package beest.caspabusters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static beest.caspabusters.AndroidUtil.makeToast;


public class WaitForCallActivity extends Activity {

    @Bind(R.id.time_display)
    TextView wakeTimeDisplay;

    // Various data related to the request that this activity is waiting on
    // This information should be passed in an Intent Bundle
    private ObjectId requestId;
    private Date earliestWakeTime;
    private Date latestWakeTime;

    private CallStateListener callStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_call);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        try {
            earliestWakeTime = Request.dbDateFormat.parse(data.getString(getString(R.string.EXTRA_EARLIEST_WAKE_TIME)));
            latestWakeTime = Request.dbDateFormat.parse(data.getString(getString(R.string.EXTRA_LATEST_WAKE_TIME)));
        } catch (ParseException e) {
            Log.e("", "Unable to parse passed Bundle times.");
            e.printStackTrace();
        }
        requestId = new ObjectId(data.getString(getString(R.string.EXTRA_REQUEST_ID)));

        String time = Request.twelveHourTimeFormat.format(earliestWakeTime)
                + " - "
                + Request.twelveHourTimeFormat.format(latestWakeTime);
        wakeTimeDisplay.setText(time);

        callStateListener = new CallStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

//    @Override
//    protected void onStop() {
//        ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).listen(callStateListener, PhoneStateListener.LISTEN_NONE);
//    }

    @OnClick(R.id.cancel_request)
    void cancelRequest() {
        CaspaBustersAPI.deleteRequest(requestId, new ResponseCallback() {
            @Override
            public void success(Response response) {
                makeToast(getApplicationContext(), "Request successfully cancelled.", Toast.LENGTH_SHORT);
                // Go to the menu
                Intent intent = new Intent(WaitForCallActivity.this, MenuActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                makeToast(getApplicationContext(), CaspaBustersAPI.getResponseString(error.getResponse()), Toast.LENGTH_SHORT);
            }
        });
    }

    private class CallStateListener extends PhoneStateListener {

        // Date to store the time of the call. Useful if, say, someone gets called at 9:44 AM and
        // their latest wake up time is 9:45 AM. The verification will still trigger even if the
        // call ends at, say, 9:46 AM.
        private Date callTime;

        // Boolean to differentiate between the beginning and end of calls with CALL_STATE_IDLE
        private boolean callIsActive = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // When a call comes in, store the time of the call
                    callTime = Calendar.getInstance().getTime();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // When the call is picked up, mark the call as active
                    callIsActive = true;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    // If the OFFHOOK state was triggered before the IDLE state,
                    // then we know that a phone call just ended
                    if (callIsActive) {
                        // Reset the boolean
                        callIsActive = false;

                        // If the call started within the requested wake time range,
                        if (earliestWakeTime.getTime() < callTime.getTime() && callTime.getTime() < latestWakeTime.getTime()) {
                            // Go to math problem activity (first step of verification)
                            Intent intent = new Intent(WaitForCallActivity.this, MathProblemVerificationActivity.class);
                            Bundle data = new Bundle();
                            data.putString(getString(R.string.EXTRA_REQUEST_ID), requestId.id);
                            intent.putExtras(data);
                            startActivity(intent);
                        }

                        // Reset the stored time after it is used
                        callTime = null;
                    }
                    break;
            }
        }
    }
}

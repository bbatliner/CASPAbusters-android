package beest.caspabusters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static beest.caspabusters.AndroidUtil.getTimeComponentsFromString;
import static beest.caspabusters.AndroidUtil.getUTCOffsetInMillis;
import static beest.caspabusters.AndroidUtil.makeToast;


public class RequestActivity extends Activity {

    @Bind(R.id.spinner_hall)
    Spinner hallSpinner;
    @Bind(R.id.spinner_wing)
    Spinner wingSpinner;
    @Bind(R.id.earliestTime)
    Button earliestWakeTimeButton;
    @Bind(R.id.latestTime)
    Button latestWakeTimeButton;
    @Bind(R.id.edit_name)
    EditText nameEditText;
    @Bind(R.id.specialRequests)
    EditText specialRequestsEditText;

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        // View to update when the user selects a time
        private Button display;

        public void setDisplay(Button display) {
            this.display = display;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour;
            int minute;
            // If the display has a time on it, use its time as the default value for the dialog
            if (display != null && display.getText().toString().contains(":")) {
                int[] timeComponents = getTimeComponentsFromString(display.getText().toString());
                hour = timeComponents[0] % 12;
                minute = timeComponents[1];
                if (timeComponents[2] == Calendar.PM) {
                    hour += 12;
                }
            }
            // Otherwise use the current time
            else {
                final Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar setTime = Calendar.getInstance();
            setTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            setTime.set(Calendar.MINUTE, minute);
            String timeText = Request.twelveHourTimeFormat.format(setTime.getTime());
            display.setText(timeText);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        ButterKnife.bind(this);

        // Load hall options
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.halls_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallSpinner.setAdapter(adapter1);

        // Load wing options
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.wings_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(adapter2);
    }

    // Register click handlers for time selectors
    @OnClick({R.id.earliestTime, R.id.latestTime})
    void showEarliestTimeFragment(Button button) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setDisplay(button);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // Register click handler for submit button
    @OnClick(R.id.submit)
    void submitRequest() {

        final Calendar now = Calendar.getInstance();

        // Initialize calendars for earliest and latest wake time
        // These Calendars, although instantiated in UTC, are effectively local time because
        // the UTC offset is handled manually. They are passed to the WaitForRequest activity
        final Calendar eWTcal = new GregorianCalendar();
        final Calendar lWTcal = new GregorianCalendar();

        // Clear some Calendar fields to make the dates more readable
        for (Calendar cal : new Calendar[]{eWTcal, lWTcal}) {
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }

        String eWTstring = earliestWakeTimeButton.getText().toString();
        // Make sure earliest wake time has been picked
        if (eWTstring.contains(":")) {
            int[] eWTcomponents = getTimeComponentsFromString(eWTstring);

            // Magical "% 12" ensures that 12:xx times correctly count as "0 hours", instead of 12 extra
            eWTcal.set(Calendar.HOUR, eWTcomponents[0] % 12);
            eWTcal.set(Calendar.MINUTE, eWTcomponents[1]);
            eWTcal.set(Calendar.AM_PM, eWTcomponents[2]);

            // If the earliest wake time calendar is before now, add a day
            if (eWTcal.getTimeInMillis() < now.getTimeInMillis()) {
                eWTcal.add(Calendar.DATE, 1);
            }
        } else {
            makeToast(getApplicationContext(), "Earliest wake time is required.", Toast.LENGTH_SHORT);
            return;
        }

        String lWTstring = latestWakeTimeButton.getText().toString();
        // Make sure latest wake time has been picked
        if (lWTstring.contains(":")) {
            int[] lWTcomponents = getTimeComponentsFromString(lWTstring);

            // Magical "% 12" ensures that 12:xx times correctly count as "0 hours", instead of 12 extra
            lWTcal.set(Calendar.HOUR, lWTcomponents[0] % 12);
            lWTcal.set(Calendar.MINUTE, lWTcomponents[1]);
            lWTcal.set(Calendar.AM_PM, lWTcomponents[2]);

            // If the latest wake time calendar is before now, add a day
            if (lWTcal.getTimeInMillis() < now.getTimeInMillis()) {
                lWTcal.add(Calendar.DATE, 1);
            }
        } else {
            makeToast(getApplicationContext(), "Latest wake time is required.", Toast.LENGTH_SHORT);
            return;
        }

        if (eWTcal.getTimeInMillis() > lWTcal.getTimeInMillis()) {
            makeToast(getApplicationContext(), "Earliest wake time must be before latest wake time.", Toast.LENGTH_SHORT);
            return;
        }

        /*
        This "conversion" deserves some explanation.
        new GregorianCalendar() uses UTC/GMT timezone. Even if you specify a TimeZone when you
        instantiate the Calendar, it still represents the same time, as seen with .getTimeInMillis().
        To ensure the time in the database is truly a local time, we need to manually add the time
        zone offset from UTC.
        For example, if a phone's default TimeZone is America/New_York, its UTC offset is -0500.
        This code will add 0500 hours to the Calendar so that when converted back to a local time
        zone, the -0500 offset results in the same time the user specified when they created their
        request. Simple!
        */
        long utcOffset = getUTCOffsetInMillis();
        Calendar eWTcalUTC = new GregorianCalendar();
        eWTcalUTC.setTimeInMillis(eWTcal.getTimeInMillis() - utcOffset);
        Calendar lWTcalUTC = new GregorianCalendar();
        lWTcalUTC.setTimeInMillis(lWTcal.getTimeInMillis() - utcOffset);

        // Prepare the fields of the Request object
        final Date eWT = eWTcalUTC.getTime();
        final Date lWT = lWTcalUTC.getTime();
        final String h = hallSpinner.getSelectedItem().toString();
        final String w = wingSpinner.getSelectedItem().toString();
        final String n = nameEditText.getText().toString();
        final String m = specialRequestsEditText.getText().toString();
        final String phoneNumber = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();

        // Verify phone number is correct with user
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.confirm_phone_dialog_title)
                .setMessage(phoneNumber)
                .setPositiveButton(R.string.confirm_phone_dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Phone number is correct - go ahead and perform the API call
                        Request newRequest = new Request(n, h, w, eWT, lWT, m, phoneNumber);
                        postNewRequest(newRequest, eWTcal, lWTcal);
                    }
                })
                .setNegativeButton(R.string.confirm_phone_dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ask the user for their phone number if the system provided one is incorrect
                        final EditText newPhoneNumber = new EditText(RequestActivity.this);
                        newPhoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
                        new AlertDialog.Builder(RequestActivity.this)
                                .setTitle(R.string.new_phone_dialog_title)
                                .setView(newPhoneNumber)
                                .setNeutralButton(R.string.new_phone_dialog_neutral, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Request newRequest = new Request(n, h, w, eWT, lWT, m, newPhoneNumber.getText().toString());
                                        postNewRequest(newRequest, eWTcal, lWTcal);
                                    }
                                })
                                .show();
                    }
                })
                .show();
    }

    /**
     * Post a new request to the CASPABusters API.
     *
     * @param newRequest The Request object to be POSTed.
     * @param localEWT   The LOCAL earliest wake time (not the UTC-offset one contained in the Request).
     * @param localLWT   The LOCAL latest wake time (not the UTC-offset one contained in the Request).
     */
    private void postNewRequest(Request newRequest, final Calendar localEWT, final Calendar localLWT) {
        // Perform the API request
        CaspaBustersAPI.postNewRequest(newRequest, new Callback<ObjectId>() {
            @Override
            public void success(ObjectId id, Response response) {
                // Go to waiting screen, passing the time data with the intent
                Intent intent = new Intent(RequestActivity.this, WaitForCallActivity.class);
                Bundle data = new Bundle();
                data.putString(getString(R.string.EXTRA_REQUEST_ID), id.id);
                // Pass the LOCAL times in the Bundle (not the offset UTC times)
                data.putString(getString(R.string.EXTRA_EARLIEST_WAKE_TIME), Request.dbDateFormat.format(localEWT.getTime()));
                data.putString(getString(R.string.EXTRA_LATEST_WAKE_TIME), Request.dbDateFormat.format(localLWT.getTime()));
                intent.putExtras(data);
                startActivity(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                makeToast(getApplicationContext(), CaspaBustersAPI.getResponseString(error.getResponse()), Toast.LENGTH_SHORT);
            }
        });
    }
}

package beest.caspabusters;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

public final class AndroidUtil {

    // Private constructor prevents instantiation (only static access)
    private AndroidUtil() {
    }

    /**
     * Make some Toast!
     *
     * @param context  The context, usually getApplicationContext()
     * @param message  The message to be shown in the Toast.
     * @param duration Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void makeToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    /**
     * A utility method to get the current TimeZone's offset from UTC, in milliseconds.
     *
     * @return A long representing the current TimeZone's offset from UTC, in milliseconds.
     */
    public static long getUTCOffsetInMillis() {
        return TimeZone.getDefault().getOffset(Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
    }

    /**
     * Gets the integer hour, minute, and AM/PM components from a time string.
     *
     * @param timeString A string representing a 12-hour time, e.g. 12:15 PM
     * @return An int array, [hour, minute, AMPM].
     */
    public static int[] getTimeComponentsFromString(String timeString) {
        /*
        After weighing the alternatives, this is the *most concise* way to parse the hour,
        minute, and AM/PM from the time string and set those specific attributes of a Calendar.
        A more "best practice" way to do this would be to create another Calendar to hold
        the time components, and then set the "official" Calendar's components to those values, but
        this takes a lot of lines, multiple Objects, and is a lot of overhead for something so simple.
        */
        int hour = Integer.parseInt(timeString.split(":")[0]);
        int minute = Integer.parseInt(timeString.split(":")[1].split(" ")[0]);
        int AMPM = timeString.split(":")[1].split(" ")[1].equals("AM") ? Calendar.AM : Calendar.PM;
        return new int[]{hour, minute, AMPM};
    }
}

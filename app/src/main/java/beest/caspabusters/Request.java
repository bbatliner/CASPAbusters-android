package beest.caspabusters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The Request class for the corresponding CASPABusters data model.
 */
public class Request {

    /**
     * MongoDB date format
     */
    public final static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    /**
     * 12-hour time format
     */
    public final static SimpleDateFormat twelveHourTimeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

    public final String phoneNumber;
    public final Date earliestWakeTime;
    public final Date latestWakeTime;
    public final String hall;
    public final String wing;
    public final String name;
    public final String message;

    public Request(String name, String hall, String wing, Date earliestWakeTime, Date latestWakeTime, String message, String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.earliestWakeTime = earliestWakeTime;
        this.latestWakeTime = latestWakeTime;
        this.hall = hall;
        this.wing = wing;
        this.name = name;
        this.message = message;
    }
}


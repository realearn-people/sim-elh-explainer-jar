package sim.explainer.library.util;


import org.joda.time.DateTime;

public class TimeUtils {

    public static String getTotalTimeDifferenceStringInMillis(DateTime dateTime1, DateTime dateTime2) {
        StringBuilder builder = new StringBuilder();
        builder.append(Math.abs(dateTime1.getMillis() - dateTime2.getMillis()));
        return builder.toString();
    }
}

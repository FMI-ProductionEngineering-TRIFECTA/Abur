package ro.unibuc.hello.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            return formatter.parse(dateStr);
        }
        catch (Exception e) {
            return null;
        }
    }
}

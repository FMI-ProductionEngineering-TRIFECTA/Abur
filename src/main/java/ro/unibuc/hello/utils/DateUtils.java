package ro.unibuc.hello.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class DateUtils {

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    private DateUtils() {}

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

    public static Date dateNow() {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

}

package util;

import java.time.LocalDate;
import java.util.List;

public class DateUtil {

    public static boolean isNotNullOrEmpty(LocalDate date) {
        return date != null;
    }

    public static boolean isNotNullOrEmpty(List<LocalDate> dates) {
        if(dates.isEmpty()) return false;

        for(LocalDate date : dates) {
            if(date == null) return false;
        }

        return true;
    }

}

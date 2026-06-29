package tpo.g16.blackwood.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

/**
 * Utility class to handle date and time conversions between UTC (server side)
 * and Local Timezone (client side) to ensure timezone-safe operations.
 */
public class TimeUtils {

    /**
     * Converts UTC date and time strings into a localized format for user display.
     *
     * @param fechaUtc Date in "yyyy-MM-dd" format.
     * @param horaUtc  Time in "HH:mm:ss" or "HH:mm" format.
     * @return Formatted local date and time string (e.g., "dd/MM/yyyy HH:mm").
     */
    public static String formatUtcToLocal(String fechaUtc, String horaUtc) {
        if (fechaUtc == null || fechaUtc.trim().isEmpty()) {
            return "—";
        }
        if (horaUtc == null || horaUtc.trim().isEmpty()) {
            return fechaUtc;
        }

        try {
            String time = horaUtc.trim();
            // Ensure format has seconds for parsing consistency
            if (time.length() == 5) {
                time += ":00";
            }

            String dateTimeStr = fechaUtc.trim() + " " + time;
            SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdfUtc.parse(dateTimeStr);

            SimpleDateFormat sdfLocal = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            // Automatically uses device's default timezone
            return sdfLocal.format(date);
        } catch (Exception e) {
            // Fallback representation if parsing fails
            return fechaUtc + " · " + horaUtc;
        }
    }

    /**
     * Converts Local date and time strings (from client device timezone) to UTC.
     *
     * @param localFecha Date in "yyyy-MM-dd" format.
     * @param localHora  Time in "HH:mm" or "HH:mm:ss" format.
     * @return A String array containing [fechaUtc, horaUtc].
     */
    public static String[] convertToUtc(String localFecha, String localHora) {
        if (localFecha == null || localFecha.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String time = localHora != null ? localHora.trim() : "00:00";
        if (time.length() == 5) {
            time += ":00";
        }

        try {
            String dateTimeStr = localFecha.trim() + " " + time;
            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            // Default time zone is local (device) timezone
            Date date = sdfLocal.parse(dateTimeStr);

            SimpleDateFormat sdfUtcDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdfUtcDate.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat sdfUtcTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
            sdfUtcTime.setTimeZone(TimeZone.getTimeZone("UTC"));

            return new String[]{
                sdfUtcDate.format(date),
                sdfUtcTime.format(date)
            };
        } catch (Exception e) {
            // Fallback in case of parse/format error
            return new String[]{localFecha, localHora};
        }
    }
}

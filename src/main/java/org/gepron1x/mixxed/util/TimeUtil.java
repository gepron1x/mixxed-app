package org.gepron1x.mixxed.util;

public final class TimeUtil {

    public static int parseTime(String time) {
        if (time == null || time.isBlank()) return 0;
        try {
            String[] parts = time.split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
            return Integer.parseInt(time);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatSeconds(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return minutes + ":" + sec;
    }
}

package com.recoded.taqadam;

import java.util.Random;

/**
 * Created by wisam on Jan 19 18.
 */

public class Utils {
    private static final String ALLOWED_CHARACTERS = "mnbvcxzlkjhgfdsapoiuytrewq9876543210taqadam0123456789qwertyuiopasdfghjklzxcvbnm";

    public static String getRandomString(int length) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public static String getFormattedDuration(long seconds) {
        StringBuilder sb = new StringBuilder();

        seconds = addUnit(sb, seconds, 604800, " wk, ");
        seconds = addUnit(sb, seconds, 86400, " d, ");
        seconds = addUnit(sb, seconds, 3600, " hr, ");
        seconds = addUnit(sb, seconds, 60, " min, ");
        addUnit(sb, seconds, 1, " sec, ");

        sb.setLength(sb.length() > 2 ? sb.length() - 2 : 0);

        return sb.toString();
    }

    private static long addUnit(StringBuilder sb, long sec, long unit, String s) {
        long n;
        if ((n = sec / unit) > 0) {
            sb.append(n).append(s);
            sec %= (n * unit);
        }
        return sec;
    }
}

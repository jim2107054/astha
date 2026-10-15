package com.astha.platform.money;

public final class Money {
    private static final char[] BENGALI_DIGITS = {'০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'};

    private Money() {}

    public static long toPaisa(double taka) {
        return Math.round(taka * 100.0);
    }

    public static double toTaka(long paisa) {
        return paisa / 100.0;
    }

    /**
     * Formats integer paisa to Taka with standard Bangladesh grouping (lakh, crore).
     * Example: 12500000 paisa -> "1,25,000.00"
     */
    public static String formatTaka(long paisa) {
        boolean negative = paisa < 0;
        long absPaisa = Math.abs(paisa);
        long taka = absPaisa / 100;
        long remPaisa = absPaisa % 100;

        String takaStr = String.valueOf(taka);
        StringBuilder grouped = new StringBuilder();

        int len = takaStr.length();
        if (len <= 3) {
            grouped.append(takaStr);
        } else {
            // Last 3 digits (hundreds)
            String lastThree = takaStr.substring(len - 3);
            String remaining = takaStr.substring(0, len - 3);

            StringBuilder remBuilder = new StringBuilder();
            int remLen = remaining.length();
            while (remLen > 2) {
                remBuilder.insert(0, "," + remaining.substring(remLen - 2, remLen));
                remLen -= 2;
            }
            if (remLen > 0) {
                remBuilder.insert(0, remaining.substring(0, remLen));
            }

            grouped.append(remBuilder).append(",").append(lastThree);
        }

        String result = (negative ? "-" : "") + "৳" + grouped + String.format(".%02d", remPaisa);
        return result;
    }

    /**
     * Converts an ASCII numeric/formatted string to Bengali numerals.
     */
    public static String toBengaliNumerals(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append(BENGALI_DIGITS[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

package com.astha.platform.masking;

public class PiiMasker {

    /**
     * Masks a Bangladeshi phone number (e.g. +8801712345678 -> +880 17****5678)
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String clean = phone.replaceAll("[^0-9+]", "");
        if (clean.length() < 7) return "***";
        int len = clean.length();
        String prefix = clean.substring(0, Math.min(5, len - 4));
        String suffix = clean.substring(len - 4);
        return prefix + "****" + suffix;
    }

    /**
     * Masks an NID (e.g. 1990123456789 -> 1990******6789)
     */
    public static String maskNid(String nid) {
        if (nid == null || nid.isBlank()) return "";
        int len = nid.length();
        if (len < 6) return "******";
        return nid.substring(0, 4) + "******" + nid.substring(len - 4);
    }
}

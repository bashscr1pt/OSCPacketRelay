package co.bashscript.oscpacketrelay.utils;

public class BSStringUtils {
    public static boolean tryParseInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static float tryParseFloat(String s, float d) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return d;
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Float.parseFloat(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

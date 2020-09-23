package co.bashscript.oscpacketrelay.utils;

public class BSMath {
    public static float Clamp01(float value)
    {
        if (value < 0F)
            return 0F;
        else if (value > 1F)
            return 1F;
        else
            return value;
    }

    public static float InverseLerp(float min, float max, float value)
    {
        if (min != max)
            return Clamp01((value - min) / (max - min));
        else
            return 0.0f;
    }

    public static float Lerp(float min, float max, float value)
    {
        return min + (max - min) * Clamp01(value);
    }
}

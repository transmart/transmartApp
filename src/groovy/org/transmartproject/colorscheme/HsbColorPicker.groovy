package org.transmartproject.colorscheme

import java.awt.Color

class HsbColorPicker {

    public static final float DEFAULT_COLOR_SATURATION = 1.0f
    public static final float DEFAULT_COLOR_BRIGHTNESS = 0.8f

    public static final float HUE_START = 0.7f

    private final float minValue
    private final float maxValue
    private final float colorSaturation
    private final float colorBrightness

    public HsbColorPicker(final float minValue,
                          final float maxValue,
                          final float colorSaturation,
                          final float colorBrightness) {
        this.minValue = minValue
        this.maxValue = maxValue
        this.colorSaturation = colorSaturation
        this.colorBrightness = colorBrightness
    }

    public HsbColorPicker(final float minValue,
                          final float maxValue) {
        this(
                minValue,
                maxValue,
                DEFAULT_COLOR_SATURATION,
                DEFAULT_COLOR_BRIGHTNESS
        )
    }

    /**
     * Return color in the color range from `HUE_START` (0.7 - dark blue) to 1 (red)
     * depending on value.
     * @param value
     * @return
     */
    public List<Integer> scaleLinearly(final float value) {
        float k = (value - minValue) / (maxValue - minValue)

        if (k > 1) {
            k = 1
        } else if (k < 0) {
            k = 0
        }

        float colorHue = HUE_START * (1 - k)
        def color = Color.getHSBColor(colorHue, colorSaturation, colorBrightness)

        [ color.red , color.green, color.blue ]
    }

}

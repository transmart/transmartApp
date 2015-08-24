package org.transmartproject.colorscheme

import org.junit.Test
import static org.junit.Assert.assertThat

import static org.hamcrest.Matchers.equalTo

class HsbColorPickerTests {

    @Test
    void testLowestValueColor() {
        def colorPicker = new HsbColorPicker(0, 100)

        def lowestValColor = colorPicker.scaleLinearly(0)

        def darkBlueRgb = [41, 0 , 204]
        assertThat darkBlueRgb, equalTo(lowestValColor)
    }

    @Test
    void testMiddleValueColor() {
        def colorPicker = new HsbColorPicker(0, 100)

        def middleValColor = colorPicker.scaleLinearly(50)

        def greenRgb = [0, 204, 20]
        assertThat greenRgb, equalTo(middleValColor)
    }

    @Test
    void testHighestValueColor() {
        def colorPicker = new HsbColorPicker(0, 100)

        def highestValColor = colorPicker.scaleLinearly(100)

        def darkRedRgb = [204, 0, 0]
        assertThat darkRedRgb, equalTo(highestValColor)
    }

    @Test
    void testLowBoundOutlier() {
        def colorPicker = new HsbColorPicker(0, 100)

        def lowValColor = colorPicker.scaleLinearly(0)
        def lowBoundOutlierColor = colorPicker.scaleLinearly(-1)

        assertThat lowValColor, equalTo(lowBoundOutlierColor)
    }

    @Test
    void testHighBoundOutlier() {
        def colorPicker = new HsbColorPicker(0, 100)

        def highValColor = colorPicker.scaleLinearly(100)
        def highBoundOutlierColor = colorPicker.scaleLinearly(101)

        assertThat highValColor, equalTo(highBoundOutlierColor)
    }

}

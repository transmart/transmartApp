


package com.recomdata.charting;

import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.util.List;

public class PieRenderer {
    private Color[] color;

    public PieRenderer(Color[] color) {
        this.color = color;
    }

    public void setColor(PiePlot plot, PieDataset dataset) {
        List<Comparable> keys = dataset.getKeys();
        int aInt;

        for (int i = 0; i < keys.size(); i++) {
            aInt = i % this.color.length;
            plot.setSectionPaint(keys.get(i), this.color[aInt]);
        }
    }
}


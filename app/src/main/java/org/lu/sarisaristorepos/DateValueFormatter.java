package org.lu.sarisaristorepos;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateValueFormatter extends ValueFormatter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        // Convert the float value to a Date and format it
        return dateFormat.format(new Date((long) value));
    }
}

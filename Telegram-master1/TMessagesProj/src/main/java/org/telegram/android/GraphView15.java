package org.telegram.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.View;

/**
 * GraphView creates a scaled line or bar graph with x and y axis labels.
 *
 * @author Arno den Hond
 */
public class GraphView15 extends View {

    public static boolean BAR = true;
    public static boolean LINE = false;

    private Paint paint;
    private double[] values;
    private String[] horlabels;
    private String[] verlabels;
    private String title;
    private boolean type;

    public GraphView15(Context context, double[] values, String title, String[] horlabels, String[] verlabels, boolean type) {
        super(context);
        if (values == null)
            values = new double[0];
        else
            this.values = values;
        if (title == null)
            title = "";
        else
            this.title = title;
        if (horlabels == null)
            this.horlabels = new String[0];
        else
            this.horlabels = horlabels;
        if (verlabels == null)
            this.verlabels = new String[0];
        else
            this.verlabels = verlabels;
        this.type = type;
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float border = 80;
        float horstart = border;
        float height = (float)getHeight();
        float width = getWidth() - 1;
        double max = getMax();
        double min = getMin();
        double diff = max - min;
        float graphheight = height - (2 * border);
        float graphwidth = width - (2 * border);

        Paint background = new Paint();
        background.setColor(Color.WHITE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), background); //배경

        paint.setStrokeWidth(10);
        paint.setTextAlign(Align.LEFT);
        int vers = verlabels.length - 1;
        for (int i = 0; i < verlabels.length; i++) { //가로줄 기입
            paint.setColor(Color.DKGRAY);
            float y = ((graphheight / vers) * i) + border;
            canvas.drawLine(horstart, y, width - horstart, y, paint);
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);
            canvas.drawText(verlabels[i], 8, y + 10, paint);
        }
        paint.setStrokeWidth(5);
        paint.setColor(Color.LTGRAY);
        for (int is = 1; is < 4; is++) { //가로줄 기입
            //paint.setColor(Color.DKGRAY);
            float yy = ((graphheight / 4) * is) + border;
            canvas.drawLine(horstart, yy, width - horstart, yy, paint);
        }
        //double ye = ((border - getEverage() / diff));
        //Log.i("average", Double.toString(getEverage()));
        //canvas.drawLine(horstart,(float)ye, width - 10, (float)ye, paint);
        paint.setStrokeWidth(10);
        int hors = horlabels.length;
        for (int i = 0; i <= horlabels.length; i++) { //날짜 기입
            paint.setColor(Color.DKGRAY);
            float x = ((graphwidth / hors) * i) + horstart;
            if (i == 0)
                canvas.drawLine(x + 5, height - border, x + 5, border, paint);
            if (i == horlabels.length)
                canvas.drawLine(x + 5, height - border, x + 5, border, paint);
            paint.setTextAlign(Align.CENTER);
            //if (i==horlabels.length-1)
            //	paint.setTextAlign(Align.RIGHT);
            //if (i==0)
            //	paint.setTextAlign(Align.LEFT);
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);
            if (i != horlabels.length)
                canvas.drawText(horlabels[i], x + 30, height - 20, paint);
        }

        Bitmap emoji;
        //paint.setTextSize(50);
        //paint.setTextAlign(Align.CENTER);
        //canvas.drawText("사용량", horstart + 60, border - 15, paint);// 4>8
        //canvas.drawText("기분", width - 140, border - 15, paint);

        double yee = (border - (height - (2 * border)) * ((getEverage() - 1) / diff)) + height - (2 * border);
        Log.i("평균", Double.toString(yee));
        paint.setColor(Color.rgb(255,217,0));
        paint.setStrokeWidth(10);
        paint.setTextSize(40);
        canvas.drawText("평균", 40, (float) yee - 8, paint);
        paint.setColor(Color.rgb(255,236,166));
        canvas.drawLine(horstart + 10, (float) yee, width - horstart, (float) yee, paint);

        if (max != min) {
            //if (type == BAR) {
            float datalength = values.length;
            float colwidth = (width - (2 * border)) / datalength;
            double ever = getEverage();
            for (int i = 0; i < values.length; i++) {

                if (values[i] >= ever)
                    paint.setColor(Color.rgb(189,224,145));
                else
                    paint.setColor(Color.rgb(234,167,145));

                double val = values[i] - 1;
                double rat = val / diff;
                double h = graphheight * rat;
                canvas.drawRect((i * colwidth) + horstart + 15, (float) ((border - h) + graphheight), ((i * colwidth) + horstart) + (colwidth - 1 - 10), height - (border - 1), paint);
            }

            //} else {

            /*paint.setColor(Color.YELLOW);
            paint.setStrokeWidth(10);

            float ldatalength = GraphActivity.emojis.length;
            float lcolwidth = (width - (2 * border)) / ldatalength;
            float lhalfcol = lcolwidth / 2;
            double lasth = 0;
            for (int i = 0; i < GraphActivity.emojis.length; i++) {
                //double val = graphActivity.emojis[i] - min;
                double val = GraphActivity.emojis[i] - 1;
                //double rat = val / diff;
                double rat = val / 4;
                double h = graphheight * rat;
                if (i > 0)
                    canvas.drawLine(((i - 1) * lcolwidth) + (horstart + 1) + lhalfcol, (float) ((border - lasth) + graphheight), (i * lcolwidth) + (horstart + 1) + lhalfcol, (float) ((border - h) + graphheight), paint);
                lasth = h;
                //	}
            }*/
        }

        paint.setStrokeWidth(10);
        paint.setTextAlign(Align.LEFT);
        vers = verlabels.length - 1;
        for (int i = 0; i < verlabels.length; i++) { //가로줄 기입
            paint.setColor(Color.DKGRAY);
            float y = ((graphheight / vers) * i) + border;
            canvas.drawLine(horstart, y, width - horstart, y, paint);
        }

        /*int evers = 4;
        for (int i = 0; i <= 4; i++) {
            //paint.setColor(Color.DKGRAY);
            float y = (graphheight / evers * i) + border - 40;
            //canvas.drawLine(horstart, y, width - 10, y, paint);
            //paint.setTextSize(40);
            //paint.setColor(Color.BLACK);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            if (i == 4)
                emoji = BitmapFactory.decodeResource(getResources(), R.drawable.sample1, options);
            else if (i == 3)
                emoji = BitmapFactory.decodeResource(getResources(), R.drawable.sample2, options);
            else if (i == 2)
                emoji = BitmapFactory.decodeResource(getResources(), R.drawable.sample3, options);
            else if (i == 1)
                emoji = BitmapFactory.decodeResource(getResources(), R.drawable.sample4, options);
            else
                emoji = BitmapFactory.decodeResource(getResources(), R.drawable.sample5, options);
            canvas.drawBitmap(emoji, width - 110, y, null);
        }*/


        /*paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.rgb(244, 238, 224));
        canvas.drawRect(30, height, getWidth() - 30, getHeight() - 110, paint);
        paint.setTextSize(70);
        paint.setColor(Color.BLACK);
        canvas.drawText("감정 지수", getWidth() / 2, height + 80, paint);
        paint.setTextSize(90);
        canvas.drawText("0.56", getWidth() / 2, height + 180, paint);*/
    }

    private double getMax() {
        /*double largest = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] > largest)
                largest = values[i];
        return largest;*/

        double largest = getEverage();
        return largest * 2;
    }

    private double getMin() {
        double smallest = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] < smallest)
                smallest = values[i];
        return 0;
    }

    private double getEverage() {
        double sum = 0;
        for (int i = 0; i < 15; i++)
            sum += values[i];
        double evr = sum / 15;
        return evr;
    }
}

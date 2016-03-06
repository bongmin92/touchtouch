package org.telegram.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.messenger.R;

import java.util.Calendar;
import java.util.List;

public class cal_CalView extends View {

    Calendar calendarCheck;
    Bitmap bitmap;


    private float width;    // width of one tile
    private float height;   // height of one tile
    private float height2;   // height of one tile
    private int selX;       // X index of selection
    private int selY;       // Y index of selection
    private final Rect selRect = new Rect();

    private int[][] cal = new int[7][6];
    private String[][] lunarcal = new String[7][6];

    private int column;

    private int textSize;
    private int backColor;
    private int textColor;
    private int gridLineColor1;
    private int gridLineColor2;
    private int selectColor;

    private int change;

    private int checkwidthTriangle;
    private int checkheightTriangle;

    private static int oldheight;

    private static int touchtemp1 = 0;
    private static int touchtemp2 = 0;

    private final cal_Calend Cal;

    public cal_CalView(Context context) {
        super(context);
        this.Cal = (cal_Calend) context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        calendarCheck = Calendar.getInstance();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        int nam = 0;
        if ((Cal.iDate[0] + Cal.iDate[1]) % 7 != 0)
            nam = 1;
        column = ((Cal.iDate[0] + Cal.iDate[1]) / 7) + nam;

        width = w / 7f;
        height2 = (h / column) / 2f;
        oldheight = h;

        getRect(selX, selY, selRect);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //invalidate(selRect);
        //cal[col][row] = 0;

        int nam = 0;
        if ((Cal.iDate[0] + Cal.iDate[1]) % 7 != 0)
            nam = 1;

        change = ((Cal.iDate[0] + Cal.iDate[1]) / 7) + nam;


        float trianglex = width - (width / 4);
        float triangley = 0;

        if (change == 5) {
            height = (oldheight - height2 * 2) / (change);
            //	checkwidthTriangle = (int) (width / 6);
            checkheightTriangle = (int) (height / 6);
            triangley = height + (height / 2);
        } else if (change == 6) {
            height = (oldheight - height2 * 2) / (change);
            //	checkwidthTriangle = (int) (width / 5);
            checkheightTriangle = (int) (height / 6);
            triangley = height + (height / 2) + (height / 8);
        }
        //checkTriangle = (int) (height / 8);
        checkwidthTriangle = (int) (width / 4);
        //checkheightTriangle = (int) (height / 6);

        // Draw the background
        Paint background = new Paint();
        background.setColor(getBackColor());
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // Draw the board...
        Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        foreground.setColor(getTextColor());

        foreground.setTextSize(gettextSize(height));
        foreground.setTextAlign(Paint.Align.CENTER);

        Paint foregroundbottom = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundbottom.setColor(getTextColor());
        //foregroundbottom.setTextSize(gettextSize( height ) );
        foregroundbottom.setTextSize((float) (gettextSize(height) / 1.5));
        foregroundbottom.setTextAlign(Paint.Align.LEFT);

        // Define colors for the grid lines 1,2
        Paint hilite = new Paint();
        hilite.setColor(getGridLineColor1());

        Paint light = new Paint();
        light.setColor(getGridLineColor2());

        float w = width / 2;
        float h = (float) (height2 / 1.5);

        // Draw the minor grid lines
        for (int i = 0; i < 7; i++) {
            if (i == 0) {
                canvas.drawLine(0, i * height2, getWidth(), i * height2, light);
                canvas.drawLine(0, (i * height2 + 1), getWidth(), (i * height2 + 1), hilite);
                canvas.drawLine(0, height2 + i * height, getWidth(), height2 + i * height, light);
                canvas.drawLine(0, height2 + (i * height + 1), getWidth(), height2 + (i * height + 1), hilite);
                foreground.setColor(Color.RED);
                canvas.drawText(Cal.sWeek[i], w + width * i, h, foreground);
            } else if (i == 6) {
                canvas.drawLine(0, height2 + i * height, getWidth(), height2 + i * height, light);
                canvas.drawLine(0, height2 + (i * height + 1), getWidth(), height2 + (i * height + 1), hilite);
                canvas.drawLine(i * width, 0, i * width, getHeight() - height2, light);
                canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight() - height2, hilite);
                foreground.setColor(Color.BLUE);
                canvas.drawText(Cal.sWeek[i], w + width * i, h, foreground);
            } else {
                canvas.drawLine(0, height2 + i * height, getWidth(), height2 + i * height, light);
                canvas.drawLine(0, height2 + (i * height + 1), getWidth(), height2 + (i * height + 1), hilite);
                canvas.drawLine(i * width, 0, i * width, getHeight() - height2, light);
                canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight() - height2, hilite);
                foreground.setColor(getTextColor());
                canvas.drawText(Cal.sWeek[i], w + width * i, h, foreground);


                //canvas.drawBitmap(bitmap2,110,200,foreground);
            }
        }

        foreground.setTextAlign(Paint.Align.LEFT);

        int dayCounut = 1;
        int col, row;
        col = Cal.iDate[0];
        row = 0;

        float x = width / 4;
        float y = height - (height / 40);
        //Cal.showToast(0,""+y  + "       "+(height - (height/7)));

        float lunarx = x;
        float lunary = (float) (height * 1.5);

        Rect triang = new Rect();
        Path triangpath = new Path();

        for (int b = 0; b < Cal.iDate[1]; b++) {
            String numTemp = Integer.toString(dayCounut);
            Log.i("numTemp", numTemp);

            if ((Cal.sltDate[0] == calendarCheck.get(Calendar.YEAR) &&
                    Cal.sltDate[1] == calendarCheck.get(Calendar.MONTH) && Cal.iDate[2] == dayCounut)) {
                foreground.setTextSize(20);
            } else if ((Cal.iDate[0] + dayCounut) % 7 == 0) {
                foreground.setColor(Color.BLUE);
            } else if ((Cal.iDate[0] + dayCounut) % 7 == 1) {
                foreground.setColor(Color.RED);
            } else {
                foreground.setColor(getTextColor());
                foreground.setTextSize(gettextSize(height));
            }


            canvas.drawText(numTemp, x + width * col, y, foreground);

            try {
                List<resultContact> calcontacts = ChangeEmojiService.usedb.getAllContacts();
                Log.i("check", "11111111111111111111111111111111111111111111111");
                for (resultContact cn : calcontacts) {
                    //  Log.i("CalContact", Integer.toString(cn.getEmoji()));
                    if (cn.getDay() == Integer.parseInt(numTemp)) {
                        //Log.d("Name: ", log);
                        Log.i("CalContact", Integer.toString(cn.getEmoji()));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;

                        switch (cn.getEmoji()) {
                            case 1:
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample1, options);
                                break;
                            case 2:
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample2, options);
                                break;
                            case 3:
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample3, options);
                                break;
                            case 4:
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample4, options);
                                break;
                            case 5:
                                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample5, options);
                                break;
                        }
                        canvas.drawBitmap(bitmap, x + (width * col) - 15, y + 30, foreground);

                    }
                }
            } catch (Exception e) {
                Log.i("ERROR", "메시지디비 비었음");
                //canvas.drawBitmap(bitmap ,x + (width * col)-15 ,y+20, foreground);
                //canvas.drawBitmap(bitmap ,x + (width * col)-15 ,y+20, foreground);
            }
            //canvas.drawBitmap(bitmap, x + (width * col) - 15, y + 30, foreground);

            String tempy = toString(Cal.sltDate[0]);
            String tempm = toString(Cal.sltDate[1]);
            String tempd = "0";
            if (numTemp.length() == 1)
                tempd += numTemp;
            else
                tempd = numTemp;

            cal[col][row] = dayCounut;

            int wow = 0;
            if (wow == 1) {

                triang.top = (int) triangley;

                triang.left = (int) (trianglex + width * col);
                triang.right = triang.left + checkwidthTriangle;

                triang.bottom = triang.top + checkheightTriangle;

                triangpath.moveTo(triang.left + triang.width(), triang.top);
                triangpath.lineTo(triang.left, triang.bottom);
                triangpath.lineTo(triang.right, triang.bottom);
                canvas.drawPath(triangpath, foreground);
            }

            col++;
              /*
          	if( (Cal.iDate[0] + dayCounut) % 7 == 1){
          		foreground.setColor(Color.BLUE);
          	}
          */
            if ((Cal.iDate[0] + dayCounut) % 7 == 0) {
                y += height;
                lunary += height;
                row++;
                triangley += height;
                col = 0;
            }
            dayCounut++;
        }
        dayCounut = 1;

        // Draw the selection...
        Paint selected = new Paint();
        selected.setColor(getSelectColor());
        canvas.drawRect(selRect, selected);

        //if(Cal.iDate[1]!=0)
        //canvas.drawText(""+33, width /2, y , foreground);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return super.onTouchEvent(event);
		/*
			Cal.showToast( Cal.getDateYear(),"" + Cal.getDateMonth()+ " " +
					cal[(int) (event.getX() / width)][(int) (((event.getY()-height2) / height)) ]);
					*/
        //Cal.showToast( (int)(event.getX() ), "" + (int)(event.getY() ));
        select((int) (event.getX() / width), (int) (((event.getY() - height2) / height)));
        return true;
    }

    public String toString(int a) {

        String temp = Integer.toString(a);
        if (temp.length() == 1)
            temp = "0" + temp;

        return temp;
    }

    private void select(int x, int y) {
        selX = Math.min(Math.max(x, 0), 7);
        selY = Math.min(Math.max(y, 0), 7);

        if (change != selY) {
            invalidate(selRect);


            //Cal.showToast( touchtemp1 , ""+ touchtemp2 );

            if (touchtemp1 != cal[selX][selY])
                touchtemp1 = cal[selX][selY];
            else if (touchtemp1 == cal[selX][selY])
                touchtemp2 = cal[selX][selY];

            if (touchtemp1 == touchtemp2) {

                Cal.startIntent(Cal.getDateYear(), Cal.getDateMonth(), cal[selX][selY], Cal.getDateWeek(Cal.getDateYear(), Cal.getDateMonth(), cal[selX][selY]));

            }

            getRect(selX, selY, selRect);


            invalidate(selRect);
		   /*
		   Intent i = new Intent(this, GameActivity.class);
		   startActivity(i);
		   */
        }
    }


    private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * width), (int) (height2 + (y * height)),
                (int) (x * width + width), (int) (height2 + (y * height + height)));
    }

    public void setTextSize(int size) {
        textSize = size;
    }

    private float gettextSize(float h) {
        return (h / textSize);
    }

    public void setBackColor(int c) {
        backColor = c;
    }

    private int getBackColor() {
        return backColor;
    }

    public void settextColor(int t) {
        textColor = t;
    }

    private int getTextColor() {
        return textColor;
    }

    public void setGridLineColor1(int g) {
        gridLineColor1 = g;
    }

    private int getGridLineColor1() {
        return gridLineColor1;
    }

    public void setGridLineColor2(int g1) {
        gridLineColor2 = g1;
    }

    private int getGridLineColor2() {
        return gridLineColor2;
    }

    public void setSelectColor(int s) {
        selectColor = s;
    }

    private int getSelectColor() {
        return selectColor;
    }


}

package com.example.baopengjian.ray_dailywork.may.bar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.example.baopengjian.ray_dailywork.R;
import com.example.baopengjian.ray_dailywork.util.UtilsDensity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ray on 2018/5/22.
 * 柱状图
 * 从上往下： 上部内容区 topHeight  + 底部标注区 bottomHeight
 * 从左往右：间宽*1/2 + 柱状图宽*count + 间宽*(count-1) + 间宽/2;
 * <p>
 *
 * 数据设置 柱状图List<Bar>  顶部标题（leftTitle + rightTitle）
 *          Bar(每个柱子 desc 描述 + data 数值)
 */

public class BarChartView extends View {

    private static int TIME_ANIM = 1000;

    /**
     * 柱顶文字和柱状图的距离
     */
    public static int TEXT_MARGINING = 5;

    /**
     * 柱底说明文字到柱底的距离
     */
    public static int BOTTOM_TEXT_MARGINNG = 10;

    /**
     * z柱状图默认颜色，以%的方式依次显示
     */
    public static int[] BAR_COLORS = {R.color.bar_color_1, R.color.bar_color_2};


    private int mTopHeight, mButtomHeight, mTitlePadding;
    /** 柱子的宽度和间距*/
    private int mBarWidth, mBarSpace;

    /**
     * 最高柱状图距离middleArea顶部值
     */
    private int mPaddingTop;
    private int mHeight, mWidth;

    private int mLineWidth;

    private String mLeftTitle, mRightTilte;
    private List<Bar> mBars;
    private int[] mBarColors;

    private Paint mTextPaint;
    private Paint mBarPaint;
    private int mTextColor;
    /** 开始时间戳*/
    private long timeStart;


    public BarChartView(Context context) {
        this(context, null);

    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mLineWidth = 1;
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setTextSize(28);
        mTextColor = R.color.text_color;
        mTextPaint.setColor(getResources().getColor(mTextColor));

        mBarColors = BAR_COLORS;

        mBarPaint = new Paint();
        mBarPaint.setColor(Color.GRAY);
        mBarPaint.setTextSize(28);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        initWeight();
        drawBackGroundLine(canvas);
        drawLeftTitle(canvas);
        drawRightTitle(canvas);
        getBarsData(canvas);
    }

    private void initWeight() {
        mHeight = getHeight();
        mWidth = getWidth();
        mButtomHeight = UtilsDensity.dp2px(getContext(), 30);
        mTopHeight = mHeight - mButtomHeight;
        mPaddingTop = UtilsDensity.dp2px(getContext(), 30);
        mTitlePadding = UtilsDensity.dp2px(getContext(), 5);


    }

    private void drawRightTitle(Canvas canvas) {
        if (mRightTilte == null || TextUtils.isEmpty(mRightTilte)) {
            return;
        }

        Rect rect = new Rect();
        mTextPaint.getTextBounds(mRightTilte, 0, mRightTilte.length(), rect);
        int startX = mWidth - mTitlePadding - rect.width();
        canvas.drawText(mRightTilte, startX, mTitlePadding + rect.height(), mTextPaint);
    }


    private void drawLeftTitle(Canvas canvas) {
        if (mLeftTitle == null || TextUtils.isEmpty(mLeftTitle)) {
            return;
        }
        Rect rect = new Rect();
        mTextPaint.getTextBounds(mLeftTitle, 0, mLeftTitle.length(), rect);
        canvas.drawText(mLeftTitle, mTitlePadding, mTitlePadding + rect.height(), mTextPaint);
    }


    private void getBarsData(Canvas canvas) {
        if (mBars == null || mBars.size() == 0) {
            return;
        }

        mBarWidth = mWidth / (2 * mBars.size());
        mBarSpace = mBarWidth;
        mBarPaint.setStrokeWidth(mBarWidth);

        float maxY = getMaxY();
        float rateY = 0;
        if (maxY > 0) {
            rateY = (mTopHeight - mPaddingTop) / maxY;
        }

        int maxDescLength = mBarWidth + mBarSpace / 4;
        textPaint = new TextPaint(mTextPaint);


        long timeCurrent = System.currentTimeMillis();
        if (timeCurrent - timeStart < TIME_ANIM) {

            drawBars(canvas, rateY, maxDescLength, false, timeCurrent);
            invalidate();
        } else {
            drawBars(canvas, rateY, maxDescLength, true, timeCurrent);
        }
    }

    private void drawBars(Canvas canvas, float rateY, int maxDescLength, boolean isEnd, long timeCurrent) {
        Bar bar;

        int x;
        int startY = mTopHeight;
        float stopY;
        int middleX;

        for (int i = 0; i < mBars.size(); i++) {
            bar = mBars.get(i);

            x = mBarSpace / 2 + (mBarWidth + mBarSpace) * i + mBarWidth / 2;
            if (isEnd) {
                stopY = startY - bar.getDataFloat() * rateY;
            } else {
                stopY = startY - (bar.getDataFloat() * rateY * (timeCurrent - timeStart)) / (float) (TIME_ANIM);
            }

            int color = getResources().getColor(mBarColors[i % mBarColors.length]);
            mBarPaint.setColor(color);
            canvas.drawLine(x, startY, x, stopY, mBarPaint);

            middleX = mBarSpace / 2 + (mBarWidth + mBarSpace) * i + mBarWidth / 2;
            drawHint(canvas, middleX, bar.getData(), stopY, mBarPaint);
            drawDesc(canvas, middleX, bar.getDesc(), startY, maxDescLength);
        }
    }


    private void drawHint(Canvas canvas, int middleX, String data, float stopY, Paint paint) {
        Rect bound = new Rect();
        mTextPaint.getTextBounds(data, 0, data.length(), bound);
        int width = bound.width();
        canvas.drawText(data, middleX - width / 2, stopY - TEXT_MARGINING, paint);
    }

    private void drawDesc(Canvas canvas, int middleX, String desc, int startY, int maxDescLength) {
        Rect bound2 = new Rect();
        mTextPaint.getTextBounds(desc, 0, desc.length(), bound2);
        if (bound2.width() < maxDescLength) {
            canvas.drawText(desc, middleX - bound2.width() / 2, startY + BOTTOM_TEXT_MARGINNG + bound2.height(), mTextPaint);
        } else {
            int startX = middleX - mBarWidth / 2 - mBarSpace / 8;
            startY += BOTTOM_TEXT_MARGINNG;
            canvas.save();
            canvas.translate(startX, startY);
            StaticLayout layout = new StaticLayout(desc, textPaint, maxDescLength, Layout.Alignment.ALIGN_NORMAL, 0.8F, 0, false);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    private TextPaint textPaint;

    private void clear() {
        if (mBars != null) {
            mBars.clear();
        }
        invalidate();
    }

    private void drawBackGroundLine(Canvas canvas) {

        Paint paint = new Paint();
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(Color.GRAY);

        int horizontalLineNumber = 5;
        int horizontalSpace = mTopHeight / (horizontalLineNumber - 1);
        float[] dash = new float[]{1, 5};
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(dash, 0));
        for (int i = 1; i < horizontalLineNumber; i++) {
            drawHorizontalLine(canvas, paint, horizontalSpace, i, horizontalLineNumber - 1);
        }
    }


    private void drawHorizontalLine(Canvas canvas, Paint paint, int horizontalSpace, int index, int maxIndex) {
        int startX = 0;
        int stopX = mWidth;

        int Y;
        if (index == 0) {
            Y = 0;
        } else if (index == maxIndex) {
            Y = mTopHeight;
        } else {
            Y = horizontalSpace * index;
        }
        canvas.drawLine(startX, Y, stopX, Y, paint);
    }

    public float getMaxY() {
        float max = mBars.get(0).getDataFloat();
        for (Bar bar : mBars) {
            if (bar.getDataFloat() > max) {
                max = bar.getDataFloat();
            }
        }
        return max;
    }

    public void setBarColors(int[] colors) {
        if (colors == null || colors.length == 0) {
            return;
        }
        mBarColors = colors;
    }

    public void setBars(List<Bar> list) {

        if (list == null || list.isEmpty()) {
            return;
        }
        clear();

        try {
            if (list.get(0).getDataFloat() < 0) {
                for (Bar bar : list) {
                    if (bar.getData() != null && !TextUtils.isEmpty(bar.getData())) {
                        bar.setDataFloat(Float.parseFloat(bar.getData()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBars = list;
        this.timeStart = System.currentTimeMillis();
        postInvalidate();
    }

    public void setTitles(String leftTitle, String rightTitle) {
        mLeftTitle = leftTitle;
        mRightTilte = rightTitle;
        postInvalidate();
    }

    public static class Bar implements Serializable {

        private String desc;

        private String data;

        private float dataFloat = -1;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public float getDataFloat() {
            return dataFloat;
        }

        public void setDataFloat(float dataFloat) {
            this.dataFloat = dataFloat;
        }

        @Override
        public String toString() {
            return "Bar{ dataFloat=" + dataFloat +
                    '}';
        }
    }
}

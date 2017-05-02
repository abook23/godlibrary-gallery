package com.god.gallery.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.god.gallery.R;

public class VideoProgress extends View implements View.OnClickListener {

    private boolean isLongClick;
    private boolean isPerformLong;
    private float mCentre;
    private float mMax;
    private float mProgress;
    private float mRadius;
    private float mRadius1;
    private float mRadius2;
    private float mRadius20;
    private float mRadius21;
    private float mRoundWidth;
    private float mStartAngle;
    private float mSweepAngle;
    private RectF oval;
    private int color1;
    private int color2;
    private int color3;
    private Paint paint1;
    private Paint paint2;
    private Paint paint3;
    private OnClickListener mListener;


    public VideoProgress(Context paramContext) {
        this(paramContext, null);
    }

    public VideoProgress(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, 0);
    }

    public VideoProgress(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext, paramAttributeSet);
    }

    private int dp2px(Context paramContext, float paramFloat) {
        return (int) (paramFloat * paramContext.getResources().getDisplayMetrics().density + 0.5F);
    }

    private void drawCanvas(Canvas paramCanvas) {
        paramCanvas.drawCircle(this.mCentre, this.mCentre, this.mRadius1, this.paint1);
        paramCanvas.drawCircle(this.mCentre, this.mCentre, this.mRadius2, this.paint2);
        if (isLongClick) {
            if (oval == null) {
                oval = new RectF(this.mCentre - this.mRadius1, this.mCentre - this.mRadius1, this.mCentre + this.mRadius1, this.mCentre + this.mRadius1);
            }
            paramCanvas.drawArc(this.oval, this.mStartAngle, this.mSweepAngle, false, this.paint3);
        }
    }

    public void init(Context context, AttributeSet attributeSet) {
        setOnClickListener(this);
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.VideoProgress);
        mMax = array.getDimension(R.styleable.VideoProgress_vp_max, 100.0F);
        mProgress = array.getFloat(R.styleable.VideoProgress_vp_progress, 0.0F);
        mRadius20 = array.getFloat(R.styleable.VideoProgress_vp_radius1, -1.0F);
        mRadius21 = array.getFloat(R.styleable.VideoProgress_vp_radius2, -1.0F);
        mRoundWidth = array.getDimension(R.styleable.VideoProgress_vp_roundWidth, dp2px(context, 6.0F));
        mStartAngle = array.getFloat(R.styleable.VideoProgress_vp_startAngle, 180.0F);
        color1 = array.getColor(R.styleable.VideoProgress_vp_color1, 0xFFDCDCDC);
        color2 = array.getColor(R.styleable.VideoProgress_vp_color2, 0xFFFDFDFE);
        color3 = array.getColor(R.styleable.VideoProgress_vp_color3, 0xFF007BBB);
        array.recycle();
        paint1 = new Paint();
        paint1.setColor(color1);
        paint1.setStyle(Paint.Style.FILL_AND_STROKE);
        paint1.setStrokeWidth(mRoundWidth);
        paint1.setAntiAlias(true);
        paint2 = new Paint();
        paint2.setColor(color2);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setStrokeWidth(mRoundWidth);
        paint2.setAntiAlias(true);
        paint3 = new Paint();
        paint3.setColor(color3);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(mRoundWidth);
        paint3.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthMeasureSpec = resolveSize(dp2px(getContext(), 100.0F), widthMeasureSpec);
        mCentre = (widthMeasureSpec / 2);
        mRadius1 = mRadius = (widthMeasureSpec - getPaddingLeft() - getPaddingRight() - paint1.getStrokeWidth()) / 2.0F;
        mRadius1 -= dp2px(getContext(), 10.0F);
        if (mRadius1 == mCentre) {
            mRadius1 -= dp2px(getContext(), 10.0F);
        }
        if (mRadius20 <= 0) {
            mRadius2 = mRadius20 = mRadius1 - dp2px(getContext(), 10.0F);
        }
        if (mRadius21 <= 0) {
            mRadius21 = dp2px(getContext(), 15.0F);
        }
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUI(canvas);
    }

    private void drawUI(Canvas paramCanvas) {
        drawCanvas(paramCanvas);
    }


    public Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            onLongClick();
        }
    };

    @Override
    public void onClick(View view) {
        Log.d("onClick", "onClick");
        if (mListener != null) {
            mListener.onClick();
        }
    }

    private boolean onLongClick() {
        isLongClick = true;
        mRadius1 = mRadius;
        mRadius2 = mRadius21;
        postInvalidateDelayed(50L);
        if (mListener != null) {
            mListener.onLongClick();
        }
        Log.d("onLongClick", "onLongClick");
        return true;
    }

    private void onLongUpClick() {
        Log.d("onLongUpClick", "onLongUpClick");
        mRadius2 = mRadius20;
        postInvalidateDelayed(50L);
        if (mListener != null) {
            mListener.onLongUpClick();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isPerformLong) {
                    isPerformLong = true;
                    postDelayed(mLongClickRunnable, 180L);
                }
                break;
            case MotionEvent.ACTION_UP:
                isPerformLong = false;
                if (isLongClick) {
                    isLongClick = false;
                    onLongUpClick();
                    return false;
                } else {
                    removeCallbacks(mLongClickRunnable);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    public int getColor1() {
        return this.color1;
    }

    public int getColor2() {
        return this.color2;
    }

    public int getColor3() {
        return this.color3;
    }

    public float getProgress() {
        return this.mProgress;
    }

    public void setColor1(int paramInt) {
        this.color1 = paramInt;
    }

    public void setColor2(int paramInt) {
        this.color2 = paramInt;
    }

    public void setColor3(int paramInt) {
        this.color3 = paramInt;
    }

    public void setMax(float paramFloat) {
        if (this.mMax == paramFloat) {
            return;
        }
        this.mMax = paramFloat;
    }

    public void setOnCameraVideoListener(OnClickListener paramOnClickListener) {
        this.mListener = paramOnClickListener;
    }

    public void setProgress(float paramFloat) {
        if (this.mProgress == paramFloat) {
            return;
        }
        this.mProgress = paramFloat;
        this.mSweepAngle = (360.0F * this.mProgress / this.mMax);
        invalidate();
    }

    public interface OnClickListener {
        void onClick();

        void onLongClick();

        void onLongUpClick();
    }
}
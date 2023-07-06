package good.damn.scriptengine.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColorRevealView extends View {

    private static final String TAG = "ColorRevealView";

    private float mCenterX = 0f;
    private float mCenterY = 0f;
    private float mRadius = 2f;
    private float mRadiusAnimation = mRadius;

    private Paint mPaint;

    private ValueAnimator mAnimator;

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(0);

        mAnimator = new ValueAnimator();

        mAnimator.setIntValues(0,1);
        mAnimator.setDuration(1500);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                mRadiusAnimation = mRadius * valueAnimator.getAnimatedFraction();
                invalidate();
            }
        });
    }

    public ColorRevealView(Context context) {
        super(context);
        init();
    }

    public ColorRevealView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorRevealView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCenterPoint(float cx, float cy) {
        mCenterX = cx;
        mCenterY = cy;
    }

    public void start(int withColor) {
        setBackgroundColor(mPaint.getColor());
        mPaint.setColor(withColor);
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mCenterX,mCenterY,mRadiusAnimation,mPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterX = getWidth() * 0.5f;
        mCenterY = getHeight() * 0.85f;

        mRadius = (float) Math.hypot(getWidth(),getHeight());
        Log.d(TAG, "onLayout: RADIUS: " + mRadius + " C_X: " + mCenterX + " C_Y: " + mCenterY);
    }
}

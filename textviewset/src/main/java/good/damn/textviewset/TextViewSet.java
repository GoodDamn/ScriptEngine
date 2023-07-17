package good.damn.textviewset;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import good.damn.textviewset.interfaces.TextViewSetListener;
import good.damn.textviewset.models.Text;

public class TextViewSet extends View {

    private static final String TAG = "TextViewSet";

    public static final byte ANIMATION_SEQUENCE = 0;
    public static final byte ANIMATION_ALPHA = 1;

    private Paint mPaint;
    private Paint mPaintAnimate;

    private Text[] mTexts;

    private ValueAnimator mAnimatorAlpha;

    private TextViewSetListener mSetListener;
    private OnDrawAnimation mOnDrawAnimation;

    private byte mCurrentAnimationIndex;
    private byte mOffset;

    private float mBeginY;
    private float midWidth;
    private float midHeight;

    private float mTextInterval;

    private boolean mIsManualPlay = false;

    private void setBeginY() {
        if (mTexts == null) {
            return;
        }

        mBeginY = midHeight - (mPaint.getTextSize()+mTextInterval) * mTexts.length / 2;
    }

    private void init() {
        mTextInterval = 0;
        mOffset = 0;

        mPaint = new Paint();
        mPaint.setColor(0xffff0000);
        mPaint.setTextSize(18.0f);

        mPaintAnimate = new Paint();
        mPaintAnimate.setColor(0xffff0000);
        mPaintAnimate.setTextSize(18.0f);

        mAnimatorAlpha = new ValueAnimator();

        mAnimatorAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                mPaintAnimate.setAlpha((int) (valueAnimator.getAnimatedFraction() * 255));
                invalidate();
            }
        });
        mAnimatorAlpha.setIntValues(0,1);
        mAnimatorAlpha.setDuration(1000);

        mAnimatorAlpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                mCurrentAnimationIndex++;
                if (mCurrentAnimationIndex >= mTexts.length) {
                    if (mSetListener != null) {
                        mSetListener.onFinish();
                    }
                    return;
                }

                if (mIsManualPlay) {
                    mIsManualPlay = false;
                    mCurrentAnimationIndex--;
                    return;
                }

                mAnimatorAlpha.start();
            }
            @Override public void onAnimationStart(@NonNull Animator animator) {
                Log.d(TAG, "onAnimationStart: ANIM_INDEX: " + mCurrentAnimationIndex + " OFFSET: " +mOffset );
            }
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
        });

        setAnimation(ANIMATION_SEQUENCE);
    }

    public TextViewSet(Context context) {
        super(context);
        init();
    }

    public TextViewSet(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewSet(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(TextViewSetListener listener) {
        mSetListener = listener;
    }

    public void setAnimation(byte animation) {

        switch (animation) {
            case ANIMATION_ALPHA:
                mOnDrawAnimation = new OnDrawAnimation() {
                    @Override
                    public void onDraw(Canvas canvas) {
                        canvas.drawText(mTexts[mCurrentAnimationIndex].getText(),
                                midWidth-mTexts[mCurrentAnimationIndex].getWidth() / 2,
                                midHeight-mPaintAnimate.getTextSize()/2,
                                mPaintAnimate);
                        if (mCurrentAnimationIndex-1 >= mOffset) {
                            mPaintAnimate.setAlpha(255-mPaintAnimate.getAlpha());
                            canvas.drawText(mTexts[mCurrentAnimationIndex - 1].getText(),
                                    midWidth - mTexts[mCurrentAnimationIndex-1].getWidth() / 2,
                                    midHeight - mPaintAnimate.getTextSize() / 2,
                                    mPaintAnimate);
                        }
                    }
                };
                break;
            default:
                mOnDrawAnimation = new OnDrawAnimation() {
                    @Override
                    public void onDraw(Canvas canvas) {
                        float y = mPaint.getTextSize();
                        for (byte i = mOffset; i < mCurrentAnimationIndex; i++) {
                            canvas.drawText(mTexts[i].getText(),midWidth-mTexts[i].getWidth()/ 2,mBeginY+y,mPaint);
                            y += mPaint.getTextSize() + mTextInterval;
                        }

                        canvas.drawText(mTexts[mCurrentAnimationIndex].getText(),
                                midWidth-mTexts[mCurrentAnimationIndex].getWidth() / 2,
                                mBeginY+y,
                                mPaintAnimate);
                    }
                };
        }
    }

    public void setSourceOffset(int offset) {
        mOffset = (byte) offset;
        mCurrentAnimationIndex = mOffset;
    }

    public void setTextInterval(float interval) {
        mTextInterval = interval;
        setBeginY();
    }

    public void setTextSize(float size) {
        mPaint.setTextSize(size);
        mPaintAnimate.setTextSize(size);
    }

    public void setTextColor(@ColorInt int color) {
        mPaint.setColor(color);
        mPaintAnimate.setColor(color);
    }

    public void setTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
        mPaintAnimate.setTypeface(typeface);
    }

    public void setSource(String[] arr) {
        mTexts = new Text[arr.length];
        for (byte i = 0; i < arr.length; i++) {
            mTexts[i] = new Text(arr[i], mPaint.measureText(arr[i]));
        }
    }

    public void next() {
        next((byte) (mCurrentAnimationIndex+1));
    }

    public void next(int from) {

        if (from >= mTexts.length) {
            Log.d(TAG, "next: INDEX NOT IN LIMITS [0;"+mTexts.length+"]. IT HAS CHANGED TO -> 0");
            mCurrentAnimationIndex = mOffset;
            return;
        }

        Log.d(TAG, "next: ANIMATION_INDEX: " + mCurrentAnimationIndex + " FROM: " + from);

        mIsManualPlay = true;
        mCurrentAnimationIndex = (byte) (from+mOffset);
        mAnimatorAlpha.start();
    }

    public void setAntiAlias(boolean aa) {
        mPaint.setAntiAlias(aa);
        mPaintAnimate.setAntiAlias(aa);
    }

    public void start() {
        mIsManualPlay = false;
        mCurrentAnimationIndex = mOffset;
        mAnimatorAlpha.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCurrentAnimationIndex >= mTexts.length) {
            mCurrentAnimationIndex = (byte) (mTexts.length-1);
        }
        mOnDrawAnimation.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        midWidth = getWidth() >> 1;
        midHeight = getHeight() >> 1;

        setBeginY();
    }

    private interface OnDrawAnimation{
        void onDraw(Canvas canvas);
    }
}

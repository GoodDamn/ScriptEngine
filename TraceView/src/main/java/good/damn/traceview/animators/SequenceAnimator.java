package good.damn.traceview.animators;

import android.animation.Animator;
import android.graphics.Canvas;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import good.damn.traceview.graphics.Entity;

public class SequenceAnimator extends EntityAnimator{

    private static final String TAG = "SequenceAnimator";
    
    private byte mCurrentEntityIndex;
    private Entity mCurrentEntity;

    public SequenceAnimator() {

        setInterpolator(new LinearInterpolator());

        addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                if (mOnVectorAnimationListener != null) {
                    mOnVectorAnimationListener.onFinish(mCurrentEntityIndex);
                }

                mCurrentEntityIndex++;
                if (mCurrentEntityIndex >= mEntities.length) {
                    if (mOnTraceFinishListener != null) {
                        mOnTraceFinishListener.onFinish();
                    }
                    return;
                }

                mCurrentEntity = mEntities[mCurrentEntityIndex];
                mCurrentEntity.onPrepareAnimation();
                setDuration(mCurrentEntity.getDuration());
                SequenceAnimator.super.start();
            }
            @Override public void onAnimationStart(@NonNull Animator animator) {
                if (mOnVectorAnimationListener != null) {
                    mOnVectorAnimationListener.onStart(mCurrentEntityIndex);
                }
            }
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
        });
    }

    @Override
    public void start() {
        mCurrentEntityIndex = 0;
        mCurrentEntity = mEntities[mCurrentEntityIndex];
        mCurrentEntity.onPrepareAnimation();
        setDuration(mEntities[0].getDuration());
        super.start();
    }

    @Override
    public void onUpdateDrawing(Canvas canvas) {
        for (Entity entity: mEntities) {
            entity.onDraw(canvas);
        }

        mCurrentEntity.onAnimate(mProgress);
        mCurrentEntity.onDraw(canvas);
    }
}

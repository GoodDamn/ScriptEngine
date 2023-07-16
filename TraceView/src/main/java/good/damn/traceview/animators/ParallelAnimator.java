package good.damn.traceview.animators;

import android.animation.Animator;
import android.graphics.Canvas;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;

import good.damn.traceview.graphics.Entity;

public class ParallelAnimator extends EntityAnimator {

    public ParallelAnimator() {
        setInterpolator(new OvershootInterpolator());

        addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                for (Entity entity: mEntities) {
                    entity.onPrepareAnimation();
                }
                setDuration(mEntities[0].getDuration());
            }
            @Override public void onAnimationEnd(@NonNull Animator animator) {
                if (mOnTraceFinishListener != null) {
                    mOnTraceFinishListener.onFinish();
                }
            }
            @Override public void onAnimationCancel(@NonNull Animator animator) {}
            @Override public void onAnimationRepeat(@NonNull Animator animator) {}
        });
    }

    @Override
    public void onUpdateDrawing(Canvas canvas) {
        for (Entity entity: mEntities) {
            entity.onAnimate(mProgress);
            entity.onDraw(canvas);
        }
    }
}
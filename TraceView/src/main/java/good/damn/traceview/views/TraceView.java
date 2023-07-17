package good.damn.traceview.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import good.damn.traceview.animators.EntityAnimator;
import good.damn.traceview.animators.ParallelAnimator;
import good.damn.traceview.graphics.Entity;
import good.damn.traceview.graphics.Line;
import good.damn.traceview.interfaces.OnDrawTracesListener;
import good.damn.traceview.interfaces.OnTraceFinishListener;
import good.damn.traceview.interfaces.OnVectorAnimationListener;
import good.damn.traceview.models.FileSVC;

public class TraceView extends View implements View.OnTouchListener {

    private static final String TAG = "MarqueeView";

    protected final float COMPLETE_PROGRESS_TRIGGER = 0.95f;

    private Entity[] mEntities;
    private final OnDrawTracesListener INTERACTIVE_DRAW = canvas -> {
        for (Entity c : mEntities) {
            c.onDraw(canvas);
        }
    };

    private OnVectorAnimationListener mOnVectorAnimationListener;
    private OnTraceFinishListener mOnTraceFinishListener;
    private OnDrawTracesListener mOnDrawTracesListener;

    private Entity mCurrentEntityTouch;
    private EntityAnimator mEntityAnimator;

    private boolean mIsFinished = false;

    private void calculate() {

        if (getWidth() <= 10 && getHeight() <= 10) {
            return;
        }

        for (Entity e : mEntities) {
            e.onLayout(getWidth(), getHeight());
        }
    }

    private void init() {
        mOnDrawTracesListener = INTERACTIVE_DRAW;
    }

    public TraceView(Context context) {
        super(context);
        init();
    }

    public TraceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TraceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void restart() {
        if (mEntities == null) {
            throw new IllegalStateException("ARRAY OF LINES IS NULL");
        }

        for (Entity e: mEntities) {
            e.reset();
        }
        mIsFinished = false;
        invalidate();
    }

    public void setVectorsSource(FileSVC fileSVC) {

        setOnTouchListener(null);
        mEntities = fileSVC.entities;

        if (fileSVC.isInteractive) {
            mOnDrawTracesListener = INTERACTIVE_DRAW;
            setOnTouchListener(this);

            calculate();
            invalidate();
            return;
        }

        if (fileSVC.animator == null) {
            fileSVC.animator = new ParallelAnimator();
        }

        Log.d(TAG, "setVectorsSource: TRACE_FINISH_LISTENER: " + mOnTraceFinishListener);
        mEntityAnimator = fileSVC.animator;
        mEntityAnimator.setTraceView(this);
        mEntityAnimator.setEntities(mEntities);
        mEntityAnimator.setOnTraceFinishListener(mOnTraceFinishListener);
        mEntityAnimator.setOnVectorAnimationListener(mOnVectorAnimationListener);

        calculate();
    }

    public void setOnVectorAnimationListener(OnVectorAnimationListener finishListener) {
        mOnVectorAnimationListener = finishListener;
        if (mEntityAnimator == null) {
            return;
        }
        mEntityAnimator.setOnVectorAnimationListener(finishListener);
    }

    public void setOnTraceFinishListener(OnTraceFinishListener finishListener) {
        mOnTraceFinishListener = finishListener;
        if (mEntityAnimator == null) {
            return;
        }
        mEntityAnimator.setOnTraceFinishListener(finishListener);
    }

    public void startAnimation() {
        if (mEntityAnimator == null) {
            Log.d(TAG, "startAnimation: ENTITY_ANIMATOR == NULL");
            return;
        }
        mOnDrawTracesListener = canvas -> mEntityAnimator.onUpdateDrawing(canvas);

        mEntityAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mEntities == null) {
            return;
        }

        mOnDrawTracesListener.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mEntities == null) {
            return;
        }

        calculate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        Log.d(TAG, "onTouch: X: " + x + " Y: " + y);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mCurrentEntityTouch = null;

                for (Entity entity : mEntities) {

                    if (!entity.hasPivot()) {
                        entity.onSetupPivotPoint(x,y);
                        invalidate();
                    }

                    if (entity.checkCollide(x,y)) {
                        mCurrentEntityTouch = entity;
                        break;
                    }
                }

                if (mCurrentEntityTouch == null) {
                    return false;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                byte state = mCurrentEntityTouch.onTouch(x,y);
                if (state == Line.DRAW_INVALIDATE_WITH_FALSE) {
                    invalidate();
                    return false;
                }

                if (state == Line.DRAW_FALSE) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:

                mCurrentEntityTouch.onTouchUp();

                Log.d(TAG, "onTouch: ACTION_UP: IS_FINISHED: " + mIsFinished);

                if (mIsFinished) {
                    break;
                }

                // Check progress to finish

                for (Entity entity : mEntities) {
                    Log.d(TAG, "onTouch: MARQUEE_PROGRESS: " + entity.getProgress());
                    if (entity.getProgress() < COMPLETE_PROGRESS_TRIGGER)
                        return false;
                }

                mIsFinished = true;

                if (mOnTraceFinishListener != null) {
                    mOnTraceFinishListener.onFinish();
                }

                break;
        }

        return true;
    }
}

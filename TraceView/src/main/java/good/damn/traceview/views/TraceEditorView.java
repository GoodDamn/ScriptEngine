package good.damn.traceview.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.LinkedList;

import good.damn.traceview.R;
import good.damn.traceview.graphics.editor.CircleEditor;
import good.damn.traceview.graphics.editor.EntityEditor;
import good.damn.traceview.graphics.editor.LineEditor;
import good.damn.traceview.graphics.editor.RectEditor;

public class TraceEditorView extends View implements View.OnTouchListener {

    private static final String TAG = "MarqueeEditorView";

    private final Paint mPaintBackground = new Paint();
    private final Paint mPaintForeground = new Paint();

    private final LinkedList<EntityEditor> mEntities = new LinkedList<>();

    private Dialog mDialogDuration;

    private EntityEditor mEntity;

    private OnClickIconListener mOnStartClickListener;

    private float mFromX;
    private float mFromY;

    private float mToX;
    private float mToY;

    private float mMinStrokeWidthY = 1;
    private float mMaxStrokeWidthY = 127;
    private float mCurrentStrokeWidthY = 0;

    private boolean mDoesStrokeEdit = false;
    private boolean mIsDurationMode = false;

    private void addEntity(String[] durations) {
        if (mEntity instanceof RectEditor) {
            RectEditor rect = (RectEditor) mEntity;
            byte countPoints = (byte) rect.getPoints().length;
            for (byte i = 0; i < countPoints; i++) {
                float[] p = rect.getPoints()[i];
                LineEditor line = new LineEditor(mPaintForeground,mPaintBackground);
                line.setStartNormalPoint(
                        p[0] / getWidth(),
                        p[1] / getHeight());

                line.setEndNormalPoint(
                        p[2] / getWidth(),
                        p[3] / getHeight()
                );

                if (durations != null && i < durations.length) {
                    line.setDuration(Integer.parseInt(durations[i]));
                }

                mEntities.add(line);
            }

            Log.d(TAG, "onTouch: COUNT OF LINE POS: "+ mEntities.size());
            return;
        }

        EntityEditor entityNew = mEntity.copy();

        entityNew.setStartNormalPoint(
                mFromX / getWidth(),
                mFromY / getHeight());

        entityNew.setEndNormalPoint(
                mToX / getWidth(),
                mToY / getHeight());

        if (durations != null) {
            entityNew.setDuration(Integer.parseInt(durations[0]));
        }

        mEntities.add(entityNew);
        Log.d(TAG, "onTouch: COUNT OF LINE POS: "+ mEntities.size());
    }

    private void init() {

        mPaintBackground.setColor(0x55ffffff);
        mPaintBackground.setStrokeWidth(15.0f);
        mPaintBackground.setStrokeCap(Paint.Cap.ROUND);

        mPaintForeground.setColor(0xff00ff59);

        mEntity = new LineEditor(mPaintForeground, mPaintBackground);

        mDialogDuration = new Dialog(getContext());
        mDialogDuration.setContentView(R.layout.dialog_save_as);
        mDialogDuration.setCancelable(false);

        EditText editText = mDialogDuration.findViewById(R.id.dialog_save_et_fileName);
        editText.setHint("Duration(-s)");

        mDialogDuration.findViewById(R.id.dialog_save_btn_save)
               .setOnClickListener(new OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       String t = editText.getText().toString().trim();
                       if (t.isEmpty()) {
                           return;
                       }
                       String[] durs = t.split("\\s+");
                       addEntity(durs);
                       mDialogDuration.dismiss();
                   }
               });

        setOnTouchListener(this);
    }

    public TraceEditorView(Context context) {
        super(context);
        init();
    }

    public TraceEditorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TraceEditorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnStartClickListener(OnClickIconListener onClickListener) {
        mOnStartClickListener = onClickListener;
    }

    public void setLineColor(@ColorInt int color) {
        mPaintForeground.setColor(color);
        mEntity.setColor(color);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int mid = getHeight() >> 1;
        mMinStrokeWidthY = mid - 200;
        mMaxStrokeWidthY = 200 + mid;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0,mMinStrokeWidthY, 50, mMaxStrokeWidthY, mPaintForeground);
        canvas.drawLine(50,mMaxStrokeWidthY, 0, mMaxStrokeWidthY, mPaintForeground);

        canvas.drawCircle(0,mCurrentStrokeWidthY, 15, mPaintForeground);

        for (EntityEditor entity: mEntities) { // already places entities
            float sX = entity.getStartNormalX() * getWidth();
            float sY = entity.getStartNormalY() * getHeight();

            entity.draw(canvas, sX, sY,
                    entity.getEndNormalX() * getWidth(),
                    entity.getEndNormalY() * getHeight());
        }

        // For new placing entity
        mEntity.draw(canvas,mFromX,mFromY, mToX, mToY);

        // Draw icons:
        // Triangle
        canvas.drawLine(25,25,25,75, mPaintForeground);
        canvas.drawLine(25,75,75,50, mPaintForeground);
        canvas.drawLine(75,50,25,25, mPaintForeground);

        // Circle
        canvas.drawCircle(150,50,25, mPaintForeground);

        // Line
        canvas.drawLine(225, 75,275,25, mPaintBackground);

        // Rectangle
        canvas.drawRect(300,25,350,75,mPaintForeground);

        // Duration mode
        canvas.drawLine(25,150,75,150, mPaintBackground);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (event.getX() > getWidth() - 100 && event.getY() < 100) { // Undo previous action
                    if (mEntities.size() != 0) {
                        mEntities.removeLast();
                    }
                    mFromX = 0;
                    mFromY = 0;
                    mToX = 0;
                    mToY = 0;

                    invalidate();
                    return false;
                }

                if (event.getX() > 100 && event.getX() < 200 && event.getY() < 100) { // draw circles
                    mEntity = new CircleEditor(mPaintForeground, mPaintBackground);
                    return false;
                }

                if (event.getX() > 200 && event.getX() < 300 && event.getY() < 100) { // draw Line
                    mEntity = new LineEditor(mPaintForeground, mPaintBackground);
                    return false;
                }

                if (event.getX() > 300 && event.getX() < 400 && event.getY() < 100) { // draw Rect
                    mEntity = new RectEditor(mPaintForeground,mPaintBackground);
                    return false;
                }

                if (event.getX() < 100 && event.getY() < 100) { // start preview mode
                    mOnStartClickListener.onClick(mEntities);
                    return false;
                }

                if (event.getX() < 100 && event.getY() > 100
                        && event.getY() < 200) { // duration mode
                    mIsDurationMode = !mIsDurationMode;
                    Toast.makeText(getContext(),"DURATION MODE: " + mIsDurationMode, Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (event.getX() < 50
                    && event.getY() > mMinStrokeWidthY
                    && event.getY() < mMaxStrokeWidthY) {
                    mDoesStrokeEdit = true;
                    return true;
                }

                mFromX = event.getX();
                mFromY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDoesStrokeEdit) {
                    float cur = (event.getY() - mMinStrokeWidthY) / (mMaxStrokeWidthY-mMinStrokeWidthY);
                    mCurrentStrokeWidthY = mMinStrokeWidthY + cur * 400;
                    mEntity.setStrokeWidth((byte) (cur * 127));
                    Log.d(TAG, "onTouch: STROKE_EDIT: " + mEntity.getStrokeWidth());
                    invalidate();
                    break;
                }

                mToX = event.getX();
                mToY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mDoesStrokeEdit) {
                    mDoesStrokeEdit = false;
                    break;
                }

                if (mIsDurationMode) {
                    mDialogDuration.show();
                    break;
                }
                addEntity(null);
                break;
        }

        return true;
    }

    public interface OnClickIconListener {
        void onClick(LinkedList<EntityEditor> entities);
    }
}

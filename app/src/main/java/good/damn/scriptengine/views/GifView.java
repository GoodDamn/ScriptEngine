package good.damn.scriptengine.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.InputStream;

public class GifView extends View {

    private Movie mMovieGif;

    private int mStartTime = 0;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSource(byte[] byteArray) {
        mMovieGif = Movie.decodeByteArray(byteArray,0, byteArray.length);
        invalidate();
    }

    public void setSource(String path) {
        mMovieGif = Movie.decodeFile(path);
        invalidate();
    }

    public void setSource(InputStream is) {
        mMovieGif = Movie.decodeStream(is);
        mStartTime = (int) System.currentTimeMillis();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMovieGif == null || mStartTime == 0)
            return;
        int currentTime = (int) (System.currentTimeMillis() - mStartTime);
        if (currentTime >= mMovieGif.duration()) {
            mStartTime = (int) System.currentTimeMillis();
        }
        mMovieGif.setTime(currentTime);
        mMovieGif.draw(canvas,0,0);
        invalidate();
    }
}

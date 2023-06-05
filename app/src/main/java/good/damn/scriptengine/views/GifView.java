package good.damn.scriptengine.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.InputStream;

public class GifView extends View {

    private static final String TAG = "GifView";

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
        Log.d(TAG, "setSource: GIF_FILE_LENGTH:" + byteArray.length);
        mMovieGif = Movie.decodeByteArray(byteArray,0, byteArray.length);
    }

    public void setSource(String path) {
        mMovieGif = Movie.decodeFile(path);
    }

    public void setSource(InputStream is) {
        mMovieGif = Movie.decodeStream(is);
    }

    public void play() {
        mStartTime = (int) System.currentTimeMillis();
        invalidate();
    }

    public int width() {
        Log.d(TAG, "width: GIVING A WIDTH: " + mMovieGif.width());
        if (mMovieGif == null) {
            return -1;
        }
        return mMovieGif.width();
    }

    public int height() {
        Log.d(TAG, "width: GIVING A HEIGHT: " + mMovieGif.height());
        if (mMovieGif == null) {
            return -1;
        }
        return mMovieGif.height();
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

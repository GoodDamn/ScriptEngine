package good.damn.scriptengine.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.ViewManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Random;

public class TextViewPhrase extends AppCompatTextView {
    public TextViewPhrase(@NonNull Context context) {
        super(context);
    }

    public void fadeOutTransition(Random random,float density) {
        animate().alpha(0.0f)
                .translationY((215 + random.nextInt(100))*density)
                .setDuration(1500+getText().length()*15)
                .withEndAction(() -> ((ViewManager) getParent()).removeView(this)).start();
    }
}

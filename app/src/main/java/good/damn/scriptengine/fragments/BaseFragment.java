package good.damn.scriptengine.fragments;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public abstract class BaseFragment extends Fragment {

    private ActivityResultLauncher<String> mLauncherContent;

    @Nullable
    public final ActivityResultLauncher<String> getContentBrowser() {
        return mLauncherContent;
    }

    public final void setContentBrowser(ActivityResultLauncher<String> m) {
        mLauncherContent = m;
    }

    public abstract void onBrowsedContent(List<Uri> result);
}

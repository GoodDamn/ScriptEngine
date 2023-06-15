package good.damn.scriptengine.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import good.damn.scriptengine.adapters.AddFilesAdapter;
import good.damn.scriptengine.adapters.FilesAdapter;

public class ResourcesFragment extends Fragment {

    private static final String TAG = "ResourcesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Context context = getContext();

        if (context == null)
            return null;

        File dirResources = new File(context.getCacheDir()+"/resources");
        if (dirResources.mkdir()) {
            Log.d(TAG, "onCreateView: RESOURCES FOLDER HAS BEEN CREATED");
        }
        
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new AddFilesAdapter(new FilesAdapter.OnFileClickListener() {
            @Override
            public void onClickedFolder(String prevFolder, String currentFolder) {

            }

            @Override
            public void onAudioFile(File file) {

            }

            @Override
            public void onImageFile(File file) {

            }
        }, dirResources.getAbsolutePath(), getActivity()));

        return recyclerView;
    }
}

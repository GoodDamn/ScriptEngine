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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import good.damn.scriptengine.adapters.AddFilesAdapter;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.Utilities;

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

        AddFilesAdapter addFilesAdapter = new AddFilesAdapter(new FilesAdapter.OnFileClickListener(),
                dirResources.getAbsolutePath(), getActivity());

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.setAdapter(addFilesAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int holderPos = viewHolder.getAdapterPosition();
                if (holderPos == addFilesAdapter.getItemCount()) {
                    addFilesAdapter.notifyItemChanged(holderPos);
                    return;
                }

                FilesAdapter.FileItem fileItem = (FilesAdapter.FileItem) viewHolder;

                String fullName = fileItem.getFileFullName();

                File file = new File(context.getCacheDir()+ FileUtils.RES_DIR+"/"+fullName);
                if (file.delete()) {
                    Utilities.showMessage(fullName + " HAS BEEN DELETED!", context);
                }
                addFilesAdapter.notifyDataSet();
            }
        }).attachToRecyclerView(recyclerView);

        return recyclerView;
    }
}

package good.damn.scriptengine.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import good.damn.scriptengine.R;
import good.damn.scriptengine.adapters.FilesAdapter;

public class ToolsUtilities {

    public static void startFileManager(Activity activity,
                                        FilesAdapter.OnFileClickListener onFileClickListener,
                                        String path) {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    200);
            return;
        }

        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.file_manager);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView tv_currentFolder = dialog.findViewById(R.id.file_manager_tv_current_folder),
                tv_back = dialog.findViewById(R.id.file_manager_tv_back);

        FilesAdapter adapter = new FilesAdapter(new FilesAdapter.OnFileClickListener() {
            @Override
            public void onClickedFolder(String prevFolder, String currentFolder) {
                tv_back.setText(prevFolder);
                tv_currentFolder.setText(currentFolder);
                onFileClickListener.onClickedFolder(prevFolder,currentFolder);
            }

            @Override
            public void onAudioFile(File file) {
                onFileClickListener.onAudioFile(file);
                dialog.dismiss();
            }

            @Override
            public void onImageFile(File file) {
                onFileClickListener.onImageFile(file);
                dialog.dismiss();
            }

            @Override
            public void onVectorFile(File file) {
                onFileClickListener.onVectorFile(file);
                dialog.dismiss();
            }

            @Override
            public void onFile(File file, String extension) {
                onFileClickListener.onFile(file,extension);
                dialog.dismiss();
            }
        }, path);

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.mPages.size()== 0){
                    dialog.dismiss();
                    return;
                }
                adapter.mPages.remove(adapter.mPages.size()-1);
                adapter.updatePath();
            }
        });

        RecyclerView recyclerView = dialog.findViewById(R.id.file_manager_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    public static void startFileManager(Activity activity, FilesAdapter.OnFileClickListener onFileClickListener){
        startFileManager(activity,
                onFileClickListener,
                Environment.getExternalStorageDirectory().getAbsolutePath());
    }
}

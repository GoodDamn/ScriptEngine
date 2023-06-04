package good.damn.scriptengine.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
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

    public static void startFileManager(Activity context, FilesAdapter.Click onClick){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Dialog dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.file_manager);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            TextView tv_currentFolder = dialog.findViewById(R.id.file_manager_tv_current_folder),
                    tv_back = dialog.findViewById(R.id.file_manager_tv_back);

            FilesAdapter adapter = new FilesAdapter(new FilesAdapter.Click() {
                @Override
                public void onClickedFolder(String prevFolder, String currentFolder) {
                    tv_back.setText(prevFolder);
                    tv_currentFolder.setText(currentFolder);
                    onClick.onClickedFolder(prevFolder,currentFolder);
                }

                @Override
                public void onAudioFile(File file) {
                    onClick.onAudioFile(file);
                    dialog.dismiss();
                }

                @Override
                public void onImageFile(File file) {
                    onClick.onImageFile(file);
                    dialog.dismiss();
                }
            });

            tv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter.pages.size()== 0){
                        dialog.dismiss();
                        return;
                    }
                    adapter.pages.remove(adapter.pages.size()-1);
                    adapter.updatePath();
                }
            });

            RecyclerView recyclerView = dialog.findViewById(R.id.file_manager_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);

            dialog.show();
            return;
        }
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
    }
}

package good.damn.scriptengine.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import good.damn.scriptengine.adapters.AddFilesAdapter;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.Utilities;

public class ResourcesFragment extends BaseFragment {

    private static final String TAG = "ResourcesFragment";

    private AddFilesAdapter mAddFilesAdapter;

    @Override
    public void onBrowsedContent(List<Uri> uris) {
        try {
            Context context = getContext();
            if (context == null) {
                return;
            }

            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return;
            }

            for (Uri result: uris) {
                InputStream is = resolver.openInputStream(result);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) {
                    baos.write(buf, 0, n);
                }
                is.close();

                buf = baos.toByteArray();
                baos.close();

                String scheme = result.getEncodedSchemeSpecificPart();
                String extension = MimeTypeMap.getFileExtensionFromUrl(scheme);

                Cursor cursor = resolver.query(result, null, null, null, null);
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                String fileName = cursor.getString(index);
                cursor.close();

                Log.d(TAG, "onBrowsedContent: SCHEME: " + scheme);
                Log.d(TAG, "onBrowsedContent: EXTENSION: " + extension);

                mAddFilesAdapter.copyFile(buf, fileName);
            }
            mAddFilesAdapter.notifyDataSet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        mAddFilesAdapter = new AddFilesAdapter(
                new FilesAdapter.OnFileClickListener(),
                dirResources.getAbsolutePath(),
                getActivity(),
                getContentBrowser());

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAddFilesAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int holderPos = viewHolder.getAdapterPosition();
                if (holderPos == mAddFilesAdapter.getItemCount()) {
                    mAddFilesAdapter.notifyItemChanged(holderPos);
                    return;
                }

                FilesAdapter.FileItem fileItem = (FilesAdapter.FileItem) viewHolder;

                String fullName = fileItem.getFileFullName();

                File file = new File(context.getCacheDir()+ FileUtils.RES_DIR+"/"+fullName);
                if (file.delete()) {
                    Utilities.showMessage(fullName + " HAS BEEN DELETED!", context);
                }
                mAddFilesAdapter.notifyDataSet();
            }
        }).attachToRecyclerView(recyclerView);

        return recyclerView;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            mAddFilesAdapter.notifyDataSet();
        }
    }
}

package good.damn.scriptengine.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.fragments.PiecesListFragment;
import good.damn.scriptengine.fragments.ResourcesFragment;
import good.damn.scriptengine.fragments.ScriptEditorFragment;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.views.BlockedViewPager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static String PATH_SCRIPT_PROJECTS;

    private BlockedViewPager mViewPager;

    private long mCurrentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new BlockedViewPager(this);
        mViewPager.setId(ViewCompat.generateViewId());

        mViewPager.setOffscreenPageLimit(3);

        PATH_SCRIPT_PROJECTS = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/ScriptProjects";

        File projsDir = new File(PATH_SCRIPT_PROJECTS);
        if (!projsDir.exists() && projsDir.mkdir()) {
            Log.d(TAG, "onCreate: SCRIPT_PROJ_DIR: CREATED:" + projsDir);
        }

        PiecesListFragment piecesListFragment = new PiecesListFragment();
        ScriptEditorFragment scriptEditorFragment = new ScriptEditorFragment();
        ResourcesFragment resourcesFragment = new ResourcesFragment();

        ActivityResultLauncher<String> launcher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                new ActivityResultCallback<List<Uri>>() {
                    @Override
                    public void onActivityResult(List<Uri> result) {
                        if (result == null) {
                            return;
                        }
                        resourcesFragment.onBrowsedContent(result);
                    }
                });

        resourcesFragment.setContentBrowser(launcher);

        piecesListFragment.setOnClickResFolderListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(2);
            }
        });

        piecesListFragment.setOnClickTextPieceListener(new OnClickTextPiece() {
            @Override
            public void onClick(Piece piece, int position) {
                scriptEditorFragment.startScript(piece,position);
                mViewPager.setCurrentItem(1);
            }
        });

        final Fragment[] fragments = new Fragment[]{
                piecesListFragment,
                scriptEditorFragment,
                resourcesFragment
        };

        setContentView(mViewPager);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            if (mCurrentTime + 1200 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            }
            mCurrentTime = System.currentTimeMillis();
            Utilities.showMessage("Press again to exit", this);
            return;
        }
        mViewPager.setCurrentItem(0);
    }
}
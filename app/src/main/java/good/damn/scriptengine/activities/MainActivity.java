package good.damn.scriptengine.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.app.Application;
import android.os.Bundle;
import android.view.View;

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

    private BlockedViewPager mViewPager;

    private long mCurrentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new BlockedViewPager(this);
        mViewPager.setId(ViewCompat.generateViewId());

        mViewPager.setOffscreenPageLimit(3);

        PiecesListFragment piecesListFragment = new PiecesListFragment();

        ScriptEditorFragment scriptEditorFragment = new ScriptEditorFragment();

        ResourcesFragment resourcesFragment = new ResourcesFragment();

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
        setContentView(mViewPager);
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
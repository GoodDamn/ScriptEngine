package good.damn.scriptengine.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import good.damn.scriptengine.fragments.PiecesListFragment;
import good.damn.scriptengine.fragments.ResourcesFragment;
import good.damn.scriptengine.fragments.ScriptEditorFragment;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.views.BlockedViewPager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BlockedViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new BlockedViewPager(this);
        mViewPager.setId(ViewCompat.generateViewId());

        mViewPager.setOffscreenPageLimit(3);

        PiecesListFragment piecesListFragment = new PiecesListFragment();

        ScriptEditorFragment scriptEditorFragment = new ScriptEditorFragment();

        ResourcesFragment resourcesFragment = new ResourcesFragment();

        piecesListFragment.setOnClickTextPiece(new OnClickTextPiece() {
            @Override
            public void onClick(Piece piece, int position) {
                scriptEditorFragment.startScript(piece);
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
            super.onBackPressed();
            return;
        }
        mViewPager.setCurrentItem(0);
    }
}
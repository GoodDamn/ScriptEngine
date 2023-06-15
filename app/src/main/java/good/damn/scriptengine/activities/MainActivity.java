package good.damn.scriptengine.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import good.damn.scriptengine.adapters.FragmentPagerAdapter;
import good.damn.scriptengine.fragments.PiecesListFragment;
import good.damn.scriptengine.fragments.ResourcesFragment;
import good.damn.scriptengine.fragments.ScriptEditorFragment;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager2(this);

        mViewPager.setOffscreenPageLimit(3);

        PiecesListFragment piecesListFragment = new PiecesListFragment();

        ScriptEditorFragment scriptEditorFragment = new ScriptEditorFragment();

        piecesListFragment.setOnClickTextPiece(new OnClickTextPiece() {
            @Override
            public void onClick(Piece piece, int position) {
                scriptEditorFragment.startScript(piece);
                mViewPager.setCurrentItem(1);
            }
        });

        mViewPager.setAdapter(new FragmentPagerAdapter(this, new Fragment[]{
                piecesListFragment,
                scriptEditorFragment,
                new ResourcesFragment()
        }));

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
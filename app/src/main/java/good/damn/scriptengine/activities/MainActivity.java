package good.damn.scriptengine.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import good.damn.scriptengine.R;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.adapters.FragmentPagerAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.fragments.PiecesListFragment;
import good.damn.scriptengine.fragments.ScriptEditorFragment;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPager2 viewPager = new ViewPager2(this);

        viewPager.setOffscreenPageLimit(3);

        PiecesListFragment piecesListFragment = new PiecesListFragment();

        ScriptEditorFragment scriptEditorFragment = new ScriptEditorFragment();

        piecesListFragment.setOnClickTextPiece(new OnClickTextPiece() {
            @Override
            public void onClick(Piece piece, int position) {
                scriptEditorFragment.startScript(piece);
                viewPager.setCurrentItem(1);
            }
        });

        viewPager.setAdapter(new FragmentPagerAdapter(this, new Fragment[]{
                piecesListFragment,
                scriptEditorFragment
        }));

        setContentView(viewPager);
    }
}
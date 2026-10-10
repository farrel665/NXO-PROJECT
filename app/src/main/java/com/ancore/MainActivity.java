package com.ancore;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.ancore.RoundedBottomNavigation;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private LinearLayout linear1;
	private LinearLayout linear2;
	private FrameLayout linear5;
	private LinearLayout linear3;
	private TextView textview1;
	private TextView textview2;
	private TextView textview3;
	private ViewPager viewpager1;
	private RoundedBottomNavigation bottomNav;
	
	private AdapterFragmentAdapter adapter;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id._main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		linear2 = findViewById(R.id.linear2);
		linear5 = findViewById(R.id.linear5);
		linear3 = findViewById(R.id.linear3);
		textview1 = findViewById(R.id.textview1);
		textview2 = findViewById(R.id.textview2);
		textview3 = findViewById(R.id.textview3);
		viewpager1 = findViewById(R.id.viewpager1);
		bottomNav = findViewById(R.id.bottomNav);
		adapter = new AdapterFragmentAdapter(getApplicationContext(), getSupportFragmentManager());
	}
	
	private void initializeLogic() {
		_setStatusbar(0xFF0F1515);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(Color.parseColor("#0F1515"));
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			View decorView = getWindow().getDecorView();
			int flags = decorView.getSystemUiVisibility();
			flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
			decorView.setSystemUiVisibility(flags);
		}
		
		adapter.setTabCount(3);
		viewpager1.setAdapter(adapter);
		com.ancore.RoundedBottomNavigation bottomNav = findViewById(R.id.bottomNav);
		
		bottomNav.tab("Start Game", getDrawable(R.drawable.games))
		.tab("Settings", getDrawable(R.drawable.ic_settings))
		.tab("Information", getDrawable(R.drawable.ic_person));
		
		bottomNav.setupWithViewPager(viewpager1);
		
		String[] titles = {"Start Game", "Settings", "Information"};
		
		viewpager1.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				textview2.setText(titles[position]);
			}
		});
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/gfregular.ttf");
		bottomNav.setTypeface(tf);
		
		textview3.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)SketchwareUtil.getDip(getApplicationContext(), (int)(8)), 0xFF1A2020));
		textview2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/gfbold.ttf"), 0);
		textview1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/gfregular.ttf"), 1);
		textview3.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/gfbold.ttf"), 0);
	}
	
	public class AdapterFragmentAdapter extends FragmentStatePagerAdapter {
		// This class is deprecated, you should migrate to ViewPager2:
		// https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
		Context context;
		int tabCount;
		
		public AdapterFragmentAdapter(Context context, FragmentManager manager) {
			super(manager);
			this.context = context;
		}
		
		public void setTabCount(int tabCount) {
			this.tabCount = tabCount;
		}
		
		@Override
		public int getCount() {
			return tabCount;
		}
		
		@Override
		public CharSequence getPageTitle(int _position) {
			return "";
		}
		
		
		@Override
		public Fragment getItem(int _position) {
			if (_position == 0) return new HomeFragmentActivity();
			if (_position == 1) return new MenuFragmentActivity();
			if (_position == 2) return new InfoFragmentActivity();
			return new Fragment();
		}
		
	}
	
	public void _setStatusbar(final int _color) {
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(_color);
		window.setNavigationBarColor(_color);
		
		int luminance = (int) (0.299 * Color.red(_color) + 0.587 * Color.green(_color) + 0.114 * Color.blue(_color));
		boolean isLight = luminance > 128;
		
		View decor = window.getDecorView();
		int flags = decor.getSystemUiVisibility();
		if (isLight) {
			flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
		} else {
			flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
		}
		decor.setSystemUiVisibility(flags);
	}
	
}
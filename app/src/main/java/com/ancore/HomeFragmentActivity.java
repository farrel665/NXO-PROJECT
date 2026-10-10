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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class HomeFragmentActivity extends Fragment {
	
	private ScrollView vscroll1;
	private LinearLayout linear1;
	private CardView cardview1;
	private TextView textview4;
	private CardView cardBtnFF;
	private CardView cardBtnFFMax;
	private LinearLayout linear3;
	private ImageView imageview2;
	private LinearLayout linear4;
	private ImageView imageview3;
	private TextView textview3;
	private TextView textview2;
	private LinearLayout linear5;
	private LinearLayout bg_ic_ff;
	private LinearLayout linear8;
	private LinearLayout pill1;
	private ImageView imgIconFF;
	private TextView txtTitleFF;
	private TextView txtPackageFF;
	private TextView txtBtnFF;
	private LinearLayout linear6;
	private LinearLayout bg_ic_max;
	private LinearLayout linear7;
	private LinearLayout pill2;
	private ImageView imgIconFFMax;
	private TextView txtTitleFFMax;
	private TextView txtPackageFFMax;
	private TextView txtBtnFFMax;
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
		View _view = _inflater.inflate(R.layout.home_fragment, _container, false);
		initialize(_savedInstanceState, _view);
		initializeLogic();
		return _view;
	}
	
	private void initialize(Bundle _savedInstanceState, View _view) {
		vscroll1 = _view.findViewById(R.id.vscroll1);
		linear1 = _view.findViewById(R.id.linear1);
		cardview1 = _view.findViewById(R.id.cardview1);
		textview4 = _view.findViewById(R.id.textview4);
		cardBtnFF = _view.findViewById(R.id.cardBtnFF);
		cardBtnFFMax = _view.findViewById(R.id.cardBtnFFMax);
		linear3 = _view.findViewById(R.id.linear3);
		imageview2 = _view.findViewById(R.id.imageview2);
		linear4 = _view.findViewById(R.id.linear4);
		imageview3 = _view.findViewById(R.id.imageview3);
		textview3 = _view.findViewById(R.id.textview3);
		textview2 = _view.findViewById(R.id.textview2);
		linear5 = _view.findViewById(R.id.linear5);
		bg_ic_ff = _view.findViewById(R.id.bg_ic_ff);
		linear8 = _view.findViewById(R.id.linear8);
		pill1 = _view.findViewById(R.id.pill1);
		imgIconFF = _view.findViewById(R.id.imgIconFF);
		txtTitleFF = _view.findViewById(R.id.txtTitleFF);
		txtPackageFF = _view.findViewById(R.id.txtPackageFF);
		txtBtnFF = _view.findViewById(R.id.txtBtnFF);
		linear6 = _view.findViewById(R.id.linear6);
		bg_ic_max = _view.findViewById(R.id.bg_ic_max);
		linear7 = _view.findViewById(R.id.linear7);
		pill2 = _view.findViewById(R.id.pill2);
		imgIconFFMax = _view.findViewById(R.id.imgIconFFMax);
		txtTitleFFMax = _view.findViewById(R.id.txtTitleFFMax);
		txtPackageFFMax = _view.findViewById(R.id.txtPackageFFMax);
		txtBtnFFMax = _view.findViewById(R.id.txtBtnFFMax);
	}
	
	private void initializeLogic() {
		linear3.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {0xFF513B37, 0xFF1A2020}));
		textview3.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfbold.ttf"), 0);
		textview2.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfregular.ttf"), 1);
		final String pkgFF = "com.dts.freefireth";
		
		txtTitleFF.setText("Free Fire");
		txtPackageFF.setText(pkgFF);
		
		try {
			android.graphics.drawable.Drawable iconFF = getActivity().getPackageManager().getApplicationIcon(pkgFF);
			BackgroundHelper.setBackground(getContext(), linear5, iconFF);
			imgIconFF.setImageDrawable(iconFF);
			
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			imgIconFF.setImageResource(R.drawable.ic_warning);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean isFFInstalled = getActivity().getPackageManager().getLaunchIntentForPackage(pkgFF) != null;
		
		if (isFFInstalled) {
			txtBtnFF.setText("Installed");
		} else {
			txtBtnFF.setText("Tap to install");
		}
		
		cardBtnFF.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				android.content.Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(pkgFF);
				if (intent != null) {
					startActivity(intent);
				} else {
					try {
						startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, 
						android.net.Uri.parse("market://details?id=" + pkgFF)));
					} catch (Exception e) {
						startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, 
						android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + pkgFF)));
					}
				}
			}
		});
		
		final String pkgFFMax = "com.dts.freefiremax";
		
		txtTitleFFMax.setText("Free Fire MAX");
		txtPackageFFMax.setText(pkgFFMax);
		
		try {
			android.graphics.drawable.Drawable iconFFMax = getActivity().getPackageManager().getApplicationIcon(pkgFFMax);
			BackgroundHelper.setBackground(getContext(), linear6, iconFFMax);
			imgIconFFMax.setImageDrawable(iconFFMax);
			
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			imgIconFFMax.setImageResource(R.drawable.ic_warning);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		boolean isFFMaxInstalled = getActivity().getPackageManager().getLaunchIntentForPackage(pkgFFMax) != null;
		
		if (isFFMaxInstalled) {
			txtBtnFFMax.setText("Installed");
		} else {
			txtBtnFFMax.setText("Tap to install");
		}
		
		cardBtnFFMax.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				android.content.Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(pkgFFMax);
				if (intent != null) {
					startActivity(intent);
				} else {
					try {
						startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, 
						android.net.Uri.parse("market://details?id=" + pkgFFMax)));
					} catch (Exception e) {
						startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, 
						android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + pkgFFMax)));
					}
				}
			}
		});
		
		pill1.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)SketchwareUtil.getDip(getContext().getApplicationContext(), (int)(50)), 0x90513B37));
		pill2.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)SketchwareUtil.getDip(getContext().getApplicationContext(), (int)(50)), 0x90513B37));
		txtTitleFF.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfbold.ttf"), 0);
		txtTitleFFMax.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfbold.ttf"), 0);
		txtPackageFF.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfregular.ttf"), 1);
		txtPackageFFMax.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfregular.ttf"), 1);
		txtBtnFF.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfsemibold.ttf"), 0);
		txtBtnFFMax.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfsemibold.ttf"), 0);
		textview4.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/gfsemibold.ttf"), 0);
	}
	
}
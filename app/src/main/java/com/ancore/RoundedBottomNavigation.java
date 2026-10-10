package com.ancore;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class RoundedBottomNavigation extends View {
	
	private final float density;
	private final ArgbEvaluator colorEval = new ArgbEvaluator();
	private final PathInterpolator m3Interpolator = new PathInterpolator(0.2f, 0f, 0f, 1f);
	
	private final List<String> tabs = new ArrayList<>();
	private final List<Drawable> tabIcons = new ArrayList<>();
	
	// WARNA SOLID
	private int barBackgroundColor = 0xFF1A2020;
	private int activeColor = 0xFFFFE0E2;
	private int inactiveColor = 0xFFBFACAA;
	private int indicatorColor = 0xFF513B37;
	
	private TextPaint textPaint;
	private Paint indicatorPaint;
	private Paint barBgPaint;
	
	private int selectedPosition = 0;
	private float scrollPosition = 0f;
	
	private int pressedTabIndex = -1;
	private ViewPager associatedViewPager;
	private ValueAnimator scrollAnimator;
	
	private float[] tabPositionsLeft;
	private float[] tabPositionsRight;
	
	// Radius sudut rounded
	private float cornerRadius;
	
	// Listener untuk Callback Pilihan Tab
	private OnTabSelectedListener onTabSelectedListener;
	
	public interface OnTabSelectedListener {
		void onTabSelected(int position, String title);
	}
	
	public RoundedBottomNavigation(Context context) {
		this(context, null);
	}
	
	public RoundedBottomNavigation(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		density = getResources().getDisplayMetrics().density;
		init();
	}
	
	private void init() {
		setClickable(true);
		
		// Radius default untuk sudut membulat (rounded)
		cornerRadius = 50f * density;
		
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(14f * density);
		textPaint.setTextAlign(Paint.Align.LEFT); 
		textPaint.setFakeBoldText(true);
		
		indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		indicatorPaint.setStyle(Paint.Style.FILL);
		
		barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barBgPaint.setStyle(Paint.Style.FILL);
	}
	
	/* ========================= TAB BUILDER ========================= */
	
	public RoundedBottomNavigation tab(String title) {
		tabs.add(title);
		tabIcons.add(null);
		updateArrays();
		requestLayout();
		return this;
	}
	
	public RoundedBottomNavigation tab(String title, Drawable icon) {
		tabs.add(title);
		tabIcons.add(icon);
		updateArrays();
		requestLayout();
		return this;
	}
	
	public void clearTabs() {
		tabs.clear();
		tabIcons.clear();
		selectedPosition = 0;
		scrollPosition = 0f;
		updateArrays();
		requestLayout();
	}
	
	private void updateArrays() {
		tabPositionsLeft = new float[tabs.size()];
		tabPositionsRight = new float[tabs.size()];
	}
	
	/* ========================= INTEGRASI VIEWPAGER ANDROIDX ========================= */
	
	public void setupWithViewPager(ViewPager viewPager) {
		associatedViewPager = viewPager;
		if (viewPager != null) {
			viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					scrollPosition = position + positionOffset;
					invalidate(); 
				}
				
				@Override
				public void onPageSelected(int position) {
					selectedPosition = position;
					invalidate();
					notifyTabSelected(position);
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					// Listener bawaan ViewPager
				}
			});
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredHeight = (int) (48f * density); 
		int desiredWidth = (int) (253f * density); 
		
		int resolvedWidth = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY 
				? MeasureSpec.getSize(widthMeasureSpec) : desiredWidth;
		
		setMeasuredDimension(resolvedWidth, resolveSize(desiredHeight, heightMeasureSpec));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (tabs.isEmpty() || tabPositionsLeft == null) return false;
		
		float touchX = event.getX();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pressedTabIndex = -1;
				for (int i = 0; i < tabs.size(); i++) {
					if (touchX >= tabPositionsLeft[i] && touchX <= tabPositionsRight[i]) {
						pressedTabIndex = i;
						break;
					}
				}
				break;
			
			case MotionEvent.ACTION_UP:
				if (pressedTabIndex != -1) {
					int upIndex = -1;
					for (int i = 0; i < tabs.size(); i++) {
						if (touchX >= tabPositionsLeft[i] && touchX <= tabPositionsRight[i]) {
							upIndex = i;
							break;
						}
					}
					
					if (upIndex == pressedTabIndex && upIndex >= 0 && upIndex < tabs.size()) {
						selectedPosition = upIndex;
						if (associatedViewPager != null) {
							associatedViewPager.setCurrentItem(selectedPosition, true);
						} else {
							animateToTab(selectedPosition);
							notifyTabSelected(selectedPosition);
						}
					}
				}
				pressedTabIndex = -1;
				break;
			
			case MotionEvent.ACTION_CANCEL:
				pressedTabIndex = -1;
				break;
		}
		return true;
	}
	
	private void animateToTab(int targetPosition) {
		if (scrollAnimator != null && scrollAnimator.isRunning()) {
			scrollAnimator.cancel();
		}
		scrollAnimator = ValueAnimator.ofFloat(scrollPosition, targetPosition);
		scrollAnimator.setDuration(300);
		scrollAnimator.setInterpolator(m3Interpolator);
		scrollAnimator.addUpdateListener(animation -> {
			scrollPosition = (float) animation.getAnimatedValue();
			invalidate();
		});
		scrollAnimator.start();
	}

	private void notifyTabSelected(int position) {
		if (onTabSelectedListener != null && position >= 0 && position < tabs.size()) {
			onTabSelectedListener.onTabSelected(position, tabs.get(position));
		}
	}
	
	/* ========================= GAMBAR (DRAW) ========================= */
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tabs.isEmpty() || tabPositionsLeft == null) return;
		
		float w = getWidth();
		float h = getHeight();
		
		float iconSize = 22f * density;
		float spacing = 8f * density;
		
		// 1. Gambar Background Utama Bar dengan Rounded Corner
		barBgPaint.setColor(barBackgroundColor);
		canvas.drawRoundRect(0, 0, w, h, cornerRadius, cornerRadius, barBgPaint);
		
		// 2. HITUNG DISTRIBUSI RASIO LEBAR ITEM
		float totalWeights = 0f;
		float[] weights = new float[tabs.size()];
		
		for (int i = 0; i < tabs.size(); i++) {
			float distance = Math.abs(scrollPosition - i);
			float activeWeight = Math.max(0f, Math.min(1f, 1f - distance));
			float interpWeight = m3Interpolator.getInterpolation(activeWeight);
			
			weights[i] = 1.0f + (1.0f * interpWeight);
			totalWeights += weights[i];
		}
		
		float currentX = 0f;
		for (int i = 0; i < tabs.size(); i++) {
			float calculatedTabWidth = (weights[i] / totalWeights) * w;
			
			tabPositionsLeft[i] = currentX;
			tabPositionsRight[i] = currentX + calculatedTabWidth;
			currentX += calculatedTabWidth;
		}
		
		/* ========================= MORPHING INDICATOR BOX ========================= */
		int leftIndex = (int) Math.floor(scrollPosition);
		int rightIndex = (int) Math.ceil(scrollPosition);
		leftIndex = Math.max(0, Math.min(leftIndex, tabs.size() - 1));
		rightIndex = Math.max(0, Math.min(rightIndex, tabs.size() - 1));
		
		float fraction = scrollPosition - leftIndex;
		float segmentWeight = m3Interpolator.getInterpolation(fraction);
		
		float finalLeft = tabPositionsLeft[leftIndex] + ((tabPositionsLeft[rightIndex] - tabPositionsLeft[leftIndex]) * segmentWeight);
		float finalRight = tabPositionsRight[leftIndex] + ((tabPositionsRight[rightIndex] - tabPositionsRight[leftIndex]) * segmentWeight);
		
		// Gambar indikator menggunakan Rounded Corner
		indicatorPaint.setColor(indicatorColor);
		canvas.drawRoundRect(finalLeft, 0, finalRight, h, cornerRadius, cornerRadius, indicatorPaint);
		
		/* ========================= RENDERING HORIZONTAL CONTENT ========================= */
		for (int i = 0; i < tabs.size(); i++) {
			float tLeft = tabPositionsLeft[i];
			float tRight = tabPositionsRight[i];
			float cx = (tLeft + tRight) / 2f;
			float cy = h / 2f; 
			
			float distance = Math.abs(scrollPosition - i);
			float activeWeight = Math.max(0f, Math.min(1f, 1f - distance));
			
			int currentTextColor = (Integer) colorEval.evaluate(activeWeight, inactiveColor, activeColor);
			Drawable icon = (i < tabIcons.size()) ? tabIcons.get(i) : null;
			String title = tabs.get(i);
			
			if (icon != null) {
				Drawable iconDrawable = icon.mutate();
				iconDrawable.setTint(currentTextColor);
				
				float textWidth = textPaint.measureText(title);
				float shiftDistance = (textWidth + spacing) / 2f;
				
				float iconLeft = cx - (iconSize / 2f) - (shiftDistance * activeWeight);
				float textLeft = iconLeft + iconSize + spacing;
				
				int finalIconTop = (int) (cy - (iconSize / 2f));
				iconDrawable.setBounds((int) iconLeft, finalIconTop, (int) (iconLeft + iconSize), (int) (finalIconTop + iconSize));
				iconDrawable.draw(canvas);
				
				textPaint.setColor(currentTextColor);
				textPaint.setTextAlign(Paint.Align.LEFT);
				
				float textAlphaProgress = Math.max(0f, (activeWeight - 0.3f) / 0.7f);
				textPaint.setAlpha((int) (255 * textAlphaProgress));
				
				Paint.FontMetrics fm = textPaint.getFontMetrics();
				float textY = cy - ((fm.ascent + fm.descent) / 2f);
				canvas.drawText(title, textLeft, textY, textPaint);
			} else if (title != null && !title.isEmpty()) {
				textPaint.setColor(currentTextColor);
				textPaint.setTextAlign(Paint.Align.CENTER);
				
				float textAlphaProgress = Math.max(0f, (activeWeight - 0.3f) / 0.7f);
				textPaint.setAlpha((int) (255 * textAlphaProgress));
				
				Paint.FontMetrics fm = textPaint.getFontMetrics();
				float textY = cy - ((fm.ascent + fm.descent) / 2f);
				canvas.drawText(title, cx, textY, textPaint);
			}
		}
	}
	
	/* ========================= SETTERS FOR LISTENERS & CONFIGS ========================= */
	
	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		this.onTabSelectedListener = listener;
	}

	public void setActiveColor(int color) {
		this.activeColor = color;
		invalidate();
	}
	
	public void setInactiveColor(int color) {
		this.inactiveColor = color;
		invalidate();
	}
	
	public void setIndicatorColor(int color) {
		this.indicatorColor = color;
		invalidate();
	}
	
	public void setBarBackgroundColor(int color) {
		this.barBackgroundColor = color;
		invalidate();
	}

	public void setCornerRadius(float dpSize) {
		this.cornerRadius = dpSize * density;
		invalidate();
	}

	public void setTypeface(@Nullable Typeface typeface) {
		if (typeface != null) {
			textPaint.setTypeface(typeface);
			invalidate();
			requestLayout();
		}
	}

	public void setFont(@FontRes int fontResId) {
		try {
			Typeface typeface = ResourcesCompat.getFont(getContext(), fontResId);
			if (typeface != null) {
				setTypeface(typeface);
			}
		} catch (Exception ignored) {
		}
	}
}

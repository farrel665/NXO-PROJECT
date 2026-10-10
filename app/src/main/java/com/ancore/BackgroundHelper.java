package com.ancore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import java.io.File;

public class BackgroundHelper {
	
	// Warna latar kartu
	private static final int DEFAULT_CARD_BG = Color.parseColor("#1A2020");
	
	// Alpha maksimal di sisi kiri (120 opacity)
	private static final int DEFAULT_MAX_ALPHA = 120;

	// ===== ARAH GRADIENT =====
	public enum GradientDirection {
		LEFT_RIGHT,
		RIGHT_LEFT,
		TOP_BOTTOM,
		BOTTOM_TOP,
		TL_BR,
		BL_TR
	}
	
	// ===== CONVERT DRAWABLE KE BITMAP =====
	private static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if (bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}
		
		Bitmap bitmap;
		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}
		
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	// ===== CROP & SCALE MEMENUHI LEBAR/TINGGI KARTU =====
	private static Bitmap scaleAndCropToCard(Bitmap src, int targetW, int targetH) {
		Bitmap out = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(out);
		
		float scale = Math.max((float) targetW / src.getWidth(), (float) targetH / src.getHeight());
		
		float scaledW = src.getWidth() * scale;
		float scaledH = src.getHeight() * scale;
		
		float dx = 0;
		float dy = (targetH - scaledH) * 0.5f;
		
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		matrix.postTranslate(dx, dy);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(src, matrix, paint);
		
		return out;
	}
	
	// ===== APPLY SMOOTH GRADIENT ERASE DENGAN KONTROL ALPHA =====
	private static Bitmap applyGradientErase(Bitmap src, GradientDirection dir, int maxAlpha) {
		int w = src.getWidth();
		int h = src.getHeight();
		
		Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(out);
		canvas.drawBitmap(src, 0, 0, null);
		
		float x0 = 0, y0 = 0, x1 = 0, y1 = 0;
		
		switch (dir) {
			case LEFT_RIGHT:
				x1 = w;
				break;
			case RIGHT_LEFT:
				x0 = w;
				break;
			case TOP_BOTTOM:
				y1 = h;
				break;
			case BOTTOM_TOP:
				y0 = h;
				break;
			case TL_BR:
				x1 = w;
				y1 = h;
				break;
			case BL_TR:
				x1 = w;
				y0 = h;
				break;
		}
		
		int midAlpha = (int) (maxAlpha * 0.4f);
		
		int[] colors = new int[] {
			(maxAlpha << 24),
			(maxAlpha << 24),
			(midAlpha << 24),
			(0 << 24),
			(0 << 24)
		};
		
		float[] positions = new float[] { 0.0f, 0.20f, 0.45f, 0.65f, 1.0f };

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setShader(new LinearGradient(
			x0, y0, x1, y1,
			colors,
			positions,
			Shader.TileMode.CLAMP
		));
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		
		canvas.drawRect(0, 0, w, h, paint);
		paint.setXfermode(null);
		
		return out;
	}

	// ===== GABUNGKAN DENGAN WARNA DASAR KARTU =====
	private static Bitmap createCardBitmap(Bitmap fadedBitmap, int width, int height, int cardBgColor) {
		Bitmap finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(finalBitmap);

		Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(cardBgColor);
		canvas.drawRect(0, 0, width, height, bgPaint);

		canvas.drawBitmap(fadedBitmap, 0, 0, null);

		return finalBitmap;
	}

	// =========================================================================
	// METHOD OVERLOAD: RESOURCE ID (R.drawable.xxx)
	// =========================================================================

	public static void setBackground(Context c, View v, int resId) {
		setBackgroundWithGradient(c, v, resId, GradientDirection.LEFT_RIGHT, DEFAULT_CARD_BG, DEFAULT_MAX_ALPHA);
	}

	public static void setBackgroundWithGradient(
		Context c,
		View v,
		int resId,
		GradientDirection direction,
		int cardBgColor,
		int maxAlpha
	) {
		if (c == null || v == null || resId == 0) return;
		try {
			Drawable drawable;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				drawable = c.getDrawable(resId);
			} else {
				drawable = c.getResources().getDrawable(resId);
			}
			setBackgroundWithGradient(c, v, drawable, direction, cardBgColor, maxAlpha);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	// METHOD OVERLOAD: FILE PATH
	// =========================================================================
	
	public static void setBackground(Context c, View v, String filePath) {
		setBackgroundWithGradient(c, v, filePath, GradientDirection.LEFT_RIGHT, DEFAULT_CARD_BG, DEFAULT_MAX_ALPHA);
	}

	public static void setBackgroundWithGradient(
		Context c,
		View v,
		String filePath,
		GradientDirection direction,
		int cardBgColor,
		int maxAlpha
	) {
		if (v == null || filePath == null || filePath.isEmpty()) return;
		
		File file = new File(filePath);
		if (!file.exists()) return;
		
		int w = v.getWidth();
		int h = v.getHeight();
		
		if (w == 0 || h == 0) {
			v.post(() -> setBackgroundWithGradient(c, v, filePath, direction, cardBgColor, maxAlpha));
			return;
		}
		
		try {
			Bitmap src = BitmapFactory.decodeFile(filePath);
			if (src == null) return;
			
			Bitmap rendered = scaleAndCropToCard(src, w, h);
			Bitmap faded = applyGradientErase(rendered, direction, maxAlpha);
			Bitmap finalCard = createCardBitmap(faded, w, h, cardBgColor);
			
			v.setBackground(new BitmapDrawable(c.getResources(), finalCard));
			
			if (src != rendered) src.recycle();
			if (rendered != faded) rendered.recycle();
			faded.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	// METHOD OVERLOAD: DRAWABLE
	// =========================================================================

	public static void setBackground(Context c, View v, Drawable drawable) {
		setBackgroundWithGradient(c, v, drawable, GradientDirection.LEFT_RIGHT, DEFAULT_CARD_BG, DEFAULT_MAX_ALPHA);
	}

	public static void setBackgroundWithGradient(
		Context c,
		View v,
		Drawable drawable,
		GradientDirection direction,
		int cardBgColor,
		int maxAlpha
	) {
		if (v == null || drawable == null) return;

		int w = v.getWidth();
		int h = v.getHeight();

		if (w == 0 || h == 0) {
			v.post(() -> setBackgroundWithGradient(c, v, drawable, direction, cardBgColor, maxAlpha));
			return;
		}

		try {
			Bitmap src = drawableToBitmap(drawable);
			if (src == null) return;

			Bitmap rendered = scaleAndCropToCard(src, w, h);
			Bitmap faded = applyGradientErase(rendered, direction, maxAlpha);
			Bitmap finalCard = createCardBitmap(faded, w, h, cardBgColor);

			v.setBackground(new BitmapDrawable(c.getResources(), finalCard));

			if (rendered != faded) rendered.recycle();
			faded.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

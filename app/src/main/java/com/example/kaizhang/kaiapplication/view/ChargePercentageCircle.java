package com.example.kaizhang.kaiapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.kaizhang.kaiapplication.R;

/**
 * Created by kai zhang on 2015/12/2.
 */
public class ChargePercentageCircle extends View {
    private float stroke1, stroke2, stroke3, stroke4;
    private int progress = 75;
    private float initAngle = -90;
    private float progressAngle;
    private RectF oval;
    private Paint paint;
    private int width, height;
    private int centerX, centerY, maxRadius;
    private float onePiece;
    private int color1, color2, color3, color4, color5;
    private float radius1, radius2, radius3;
    private RadialGradient radialGradient;
    private Canvas eraseCanvas;
    private Bitmap eraseBitmap;

    public ChargePercentageCircle(Context context) {
        this(context, null);
    }

    public ChargePercentageCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargePercentageCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        stroke1 = dp(1);
        stroke2 = dp(2);
        stroke3 = dp(2.5f);
        stroke4 = dp(18);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        onePiece = 360 / 20f;
        color1 = getResources().getColor(R.color.charge_percentage_circle_1);
        color2 = getResources().getColor(R.color.charge_percentage_circle_2);
        color3 = getResources().getColor(R.color.charge_percentage_circle_3);
        color4 = getResources().getColor(R.color.charge_percentage_circle_4);
        color5 = getResources().getColor(R.color.charge_percentage_circle_5);
    }

    private void refreshParams() {
        centerX = width / 2;
        centerY = height / 2;
        maxRadius = Math.min(width / 2, height / 2);
        oval = new RectF();
        radius1 = maxRadius - stroke1;
        radius2 = radius1 - stroke2 / 2 - dp(2);
//        radius3 = radius2 - stroke2 / 2 - stroke4 / 2 - dp(1);
        radius3 = radius1 - stroke4 / 2 - dp(1);
        radialGradient = new RadialGradient(centerX, centerY, radius3 * 1.5f, new int[]{Color.TRANSPARENT, Color.TRANSPARENT, color5, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        if (eraseBitmap != null) {
            eraseBitmap.recycle();
            eraseBitmap = null;
        }
        eraseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        eraseBitmap.eraseColor(Color.TRANSPARENT);
        eraseCanvas = new Canvas(eraseBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (getWidth() != width || getHeight() != height) {
            width = getWidth();
            height = getHeight();
            refreshParams();
        }
        drawCircle1(canvas);
        if (progress == 0) {
            drawCircle3(canvas);
        } else {
            progressAngle = 360 * progress / 100f;
            oval.left = centerX - radius3;
            oval.top = centerY - radius3;
            oval.right = centerX + radius3;
            oval.bottom = centerY + radius3;
            drawCircle3Cloud(eraseCanvas);
            eraseCircle3Cloud(eraseCanvas);
            paint.reset();
            canvas.drawBitmap(eraseBitmap, new Rect(0, 0, width, height), new Rect(0, 0, width, height), paint);
            paint.setXfermode(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            drawCircle3(canvas);
            drawCircle3Progress(canvas);
            drawCircle2(canvas);

        }
    }

    private float dp(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    private void drawCircle1(Canvas canvas) {
        paint.setColor(color1);
        paint.setStrokeWidth(stroke1);
        canvas.drawCircle(centerX, centerY, radius1, paint);
    }

    private void drawCircle2(Canvas canvas) {
        paint.setStrokeWidth(stroke2);
        paint.setColor(color2);
        oval.left = centerX - radius2;
        oval.top = centerY - radius2;
        oval.right = centerX + radius2;
        oval.bottom = centerY + radius2;
        canvas.drawArc(oval, initAngle, Math.min(progressAngle, onePiece), false, paint);
        if (progressAngle > onePiece * 2) {
            canvas.drawArc(oval, initAngle + onePiece * 2, Math.min(progressAngle - onePiece * 2, onePiece * 2), false, paint);
            if (progressAngle > onePiece * 5) {
                canvas.drawArc(oval, initAngle + onePiece * 5, Math.min(progressAngle - onePiece * 5, onePiece), false, paint);
                if (progressAngle > onePiece * 7) {
                    canvas.drawArc(oval, initAngle + onePiece * 7, Math.min(progressAngle - onePiece * 7, onePiece * 2), false, paint);
                    if (progressAngle > onePiece * 10) {
                        canvas.drawArc(oval, initAngle + onePiece * 10, Math.min(progressAngle - onePiece * 10, onePiece), false, paint);
                        if (progressAngle > onePiece * 12) {
                            canvas.drawArc(oval, initAngle + onePiece * 12, Math.min(progressAngle - onePiece * 12, onePiece * 2), false, paint);
                            if (progressAngle > onePiece * 15) {
                                canvas.drawArc(oval, initAngle + onePiece * 15, Math.min(progressAngle - onePiece * 15, onePiece), false, paint);
                                if (progressAngle > onePiece * 17) {
                                    canvas.drawArc(oval, initAngle + onePiece * 17, Math.min(progressAngle - onePiece * 17, onePiece * 2), false, paint);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawCircle3(Canvas canvas) {
        paint.setStrokeWidth(stroke3);
        paint.setColor(color4);
        canvas.drawCircle(centerX, centerY, radius3, paint);
    }

    private void drawCircle3Progress(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(null);
        paint.setStrokeWidth(stroke3);
        paint.setColor(color3);
        canvas.drawArc(oval, initAngle, progressAngle, false, paint);
    }

    private void drawCircle3Cloud(Canvas canvas) {
        paint.setStrokeWidth(stroke4);
        paint.setColor(Color.WHITE);
        paint.setShader(radialGradient);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius3 * 1.5f, paint);
    }

    private void eraseCircle3Cloud(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width/2);
        paint.setAlpha(0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawArc(oval, initAngle + progressAngle, 360 - progressAngle, false, paint);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        progress = Math.max(0, Math.min(progress, 100));
        this.progress = progress;
        invalidate();
    }
}

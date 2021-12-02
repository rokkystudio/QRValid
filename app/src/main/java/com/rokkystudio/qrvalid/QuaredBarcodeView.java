package com.rokkystudio.qrvalid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QuaredBarcodeView extends DecoratedBarcodeView
{
    private Drawable mAimDrawable;

    public QuaredBarcodeView(Context context) {
        super(context);
        init();
    }

    public QuaredBarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuaredBarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        setStatusText("");
        getViewFinder().setLaserVisibility(false);
        // mAimDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_aim);
        // Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_aim);
        // getViewFinder().drawResultBitmap(bitmap);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mAimDrawable.setBounds(0, 0, getWidth(), getHeight());
        mAimDrawable.draw(canvas);
    }
}

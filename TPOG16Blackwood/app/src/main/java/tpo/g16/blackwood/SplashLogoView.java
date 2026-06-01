package tpo.g16.blackwood;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class SplashLogoView extends View {

    private static final int COLOR_BG       = 0xFF1C2A21;
    private static final int COLOR_TRIANGLE = 0xFF2C3E50;
    private static final int COLOR_CROSS    = 0xFFC6A75E;

    private final Paint triPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float RADIUS_RATIO = 0.16f;
    private static final float TRAVEL_RATIO = 0.25f;

    private float phase1Progress = 0f;
    private float crossAlpha     = 0f;

    public SplashLogoView(Context c) { super(c); init(); }
    public SplashLogoView(Context c, AttributeSet a) { super(c, a); init(); }
    public SplashLogoView(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        triPaint.setStyle(Paint.Style.FILL);
        triPaint.setColor(COLOR_TRIANGLE);
        crossPaint.setStyle(Paint.Style.STROKE);
        crossPaint.setColor(COLOR_CROSS);
        crossPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    public AnimatorSet buildPhase1Animator(int duration) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(duration);
        anim.addUpdateListener(a -> {
            phase1Progress = (float) a.getAnimatedValue();
            invalidate();
        });
        AnimatorSet set = new AnimatorSet();
        set.play(anim);
        return set;
    }

    public AnimatorSet buildPhase2Animator(int duration) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(duration);
        anim.addUpdateListener(a -> {
            crossAlpha = (float) a.getAnimatedValue();
            invalidate();
        });
        AnimatorSet set = new AnimatorSet();
        set.play(anim);
        return set;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w  = getWidth();
        final float h  = getHeight();
        final float cx = w / 2f;
        final float r  = w * RADIUS_RATIO;

        // ── Centrar el bloque completo (rombo + texto) ───────────────────
        // Estimamos la altura total del bloque visual:
        //   rombo:          r * 2
        //   gap logo-texto: 32dp
        //   texto título:   ~30dp  (26sp)
        //   gap entre textos: 6dp
        //   subtítulo:      ~14dp  (11sp)
        // Total: r*2 + 82dp
        // El centro del rombo se ubica en: centro_pantalla - (82dp / 2)
        final float dp          = getResources().getDisplayMetrics().density;
        final float textHeight  = 82f * dp;
        final float cy          = h / 2f - textHeight / 2f;

        canvas.drawColor(COLOR_BG);

        final float travel = w * TRAVEL_RATIO;
        final float offset = travel * (1f - phase1Progress);

        drawTriangleUp   (canvas, cx,              cy - r - offset, r);
        drawTriangleDown (canvas, cx,              cy + r + offset, r);
        drawTriangleLeft (canvas, cx - r - offset, cy,              r);
        drawTriangleRight(canvas, cx + r + offset, cy,              r);

        if (crossAlpha > 0f) {
            crossPaint.setAlpha((int)(255 * crossAlpha));
            crossPaint.setStrokeWidth(Math.max(1f, w * (2f / 360f)));
            canvas.drawLine(cx, cy - r, cx, cy + r, crossPaint);
            canvas.drawLine(cx - r, cy, cx + r, cy, crossPaint);
        }
    }

    private void drawTriangleUp(Canvas canvas, float cx, float tipY, float r) {
        Path p = new Path();
        p.moveTo(cx,     tipY);
        p.lineTo(cx + r, tipY + r);
        p.lineTo(cx - r, tipY + r);
        p.close();
        canvas.drawPath(p, triPaint);
    }

    private void drawTriangleDown(Canvas canvas, float cx, float tipY, float r) {
        Path p = new Path();
        p.moveTo(cx,     tipY);
        p.lineTo(cx + r, tipY - r);
        p.lineTo(cx - r, tipY - r);
        p.close();
        canvas.drawPath(p, triPaint);
    }

    private void drawTriangleLeft(Canvas canvas, float tipX, float cy, float r) {
        Path p = new Path();
        p.moveTo(tipX,     cy);
        p.lineTo(tipX + r, cy - r);
        p.lineTo(tipX + r, cy + r);
        p.close();
        canvas.drawPath(p, triPaint);
    }

    private void drawTriangleRight(Canvas canvas, float tipX, float cy, float r) {
        Path p = new Path();
        p.moveTo(tipX,     cy);
        p.lineTo(tipX - r, cy - r);
        p.lineTo(tipX - r, cy + r);
        p.close();
        canvas.drawPath(p, triPaint);
    }
}
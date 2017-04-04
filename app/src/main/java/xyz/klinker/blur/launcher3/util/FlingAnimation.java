package xyz.klinker.blur.launcher3.util;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;

import xyz.klinker.blur.launcher3.DropTarget;
import xyz.klinker.blur.launcher3.DropTarget.DragObject;
import xyz.klinker.blur.launcher3.dragndrop.DragLayer;
import xyz.klinker.blur.launcher3.dragndrop.DragView;

public class FlingAnimation implements AnimatorUpdateListener {

    /**
     * Maximum acceleration in one dimension (pixels per milliseconds)
     */
    private static final float MAX_ACCELERATION = 0.5f;
    private static final int DRAG_END_DELAY = 300;

    protected final DropTarget.DragObject mDragObject;
    protected final Rect mIconRect;
    protected final DragLayer mDragLayer;
    protected final Rect mFrom;
    protected final int mDuration;
    protected final float mUX, mUY;
    protected final float mAnimationTimeFraction;
    protected final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);

    protected float mAX, mAY;

    /**
     * @param vel initial fling velocity in pixels per second.
     */
    public FlingAnimation(DropTarget.DragObject d, PointF vel, Rect iconRect, DragLayer dragLayer) {
        mDragObject = d;
        mUX = vel.x / 1000;
        mUY = vel.y / 1000;
        mIconRect = iconRect;

        mDragLayer = dragLayer;
        mFrom = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, mFrom);

        float scale = d.dragView.getScaleX();
        float xOffset = ((scale - 1f) * d.dragView.getMeasuredWidth()) / 2f;
        float yOffset = ((scale - 1f) * d.dragView.getMeasuredHeight()) / 2f;
        mFrom.left += xOffset;
        mFrom.right -= xOffset;
        mFrom.top += yOffset;
        mFrom.bottom -= yOffset;

        mDuration = Math.abs(vel.y) > Math.abs(vel.x) ? initFlingUpDuration() : initFlingLeftDuration();
        mAnimationTimeFraction = ((float) mDuration) / (mDuration + DRAG_END_DELAY);
    }

    /**
     * The fling animation is based on the following system
     *   - Apply a constant force in the y direction to causing the fling to decelerate.
     *   - The animation runs for the time taken by the object to go out of the screen.
     *   - Calculate a constant acceleration in x direction such that the object reaches
     *     {@link #mIconRect} in the given time.
     */
    protected int initFlingUpDuration() {
        float sY = -mFrom.bottom;

        float d = mUY * mUY + 2 * sY * MAX_ACCELERATION;
        if (d >= 0) {
            // sY can be reached under the MAX_ACCELERATION. Use MAX_ACCELERATION for y direction.
            mAY = MAX_ACCELERATION;
        } else {
            // sY is not reachable, decrease the acceleration so that sY is almost reached.
            d = 0;
            mAY = mUY * mUY / (2 * -sY);
        }
        double t = (-mUY - Math.sqrt(d)) / mAY;

        float sX = -mFrom.exactCenterX() + mIconRect.exactCenterX();

        // Find horizontal acceleration such that: u*t + a*t*t/2 = s
        mAX = (float) ((sX - t * mUX) * 2 / (t * t));
        return (int) Math.round(t);
    }

    /**
     * The fling animation is based on the following system
     *   - Apply a constant force in the x direction to causing the fling to decelerate.
     *   - The animation runs for the time taken by the object to go out of the screen.
     *   - Calculate a constant acceleration in y direction such that the object reaches
     *     {@link #mIconRect} in the given time.
     */
    protected int initFlingLeftDuration() {
        float sX = -mFrom.right;

        float d = mUX * mUX + 2 * sX * MAX_ACCELERATION;
        if (d >= 0) {
            // sX can be reached under the MAX_ACCELERATION. Use MAX_ACCELERATION for x direction.
            mAX = MAX_ACCELERATION;
        } else {
            // sX is not reachable, decrease the acceleration so that sX is almost reached.
            d = 0;
            mAX = mUX * mUX / (2 * -sX);
        }
        double t = (-mUX - Math.sqrt(d)) / mAX;

        float sY = -mFrom.exactCenterY() + mIconRect.exactCenterY();

        // Find vertical acceleration such that: u*t + a*t*t/2 = s
        mAY = (float) ((sY - t * mUY) * 2 / (t * t));
        return (int) Math.round(t);
    }

    public final int getDuration() {
        return mDuration + DRAG_END_DELAY;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float t = animation.getAnimatedFraction();
        if (t > mAnimationTimeFraction) {
            t = 1;
        } else {
            t = t / mAnimationTimeFraction;
        }
        final DragView dragView = (DragView) mDragLayer.getAnimatedView();
        final float time = t * mDuration;
        dragView.setTranslationX(time * mUX + mFrom.left + mAX * time * time / 2);
        dragView.setTranslationY(time * mUY + mFrom.top + mAY * time * time / 2);
        dragView.setAlpha(1f - mAlphaInterpolator.getInterpolation(t));
    }
}

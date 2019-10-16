package com.uan.hw4_friendsr


import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

open class OnSwipeTouchListener(context: Context) : View.OnTouchListener {

    private val mGestureDetector: GestureDetector

    private val mSwipeDistanceThreshold = 50f
    private val mSwipeVelocityThreshold = 100f

    private var mPrevX: Float = 0.toFloat()
    private var mPrevY: Float = 0.toFloat()

    private var mDx: Float = 0.toFloat()
    private var mDy: Float = 0.toFloat()

    init {
        mGestureDetector = GestureDetector(context, GestureListener())
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val dx = e2.rawX - e1.rawX
            val dy = e2.rawY - e1.rawY

            if (Math.abs(dx) > Math.abs(dy) &&
                Math.abs(dx) > mSwipeDistanceThreshold &&
                Math.abs(velocityX) > mSwipeVelocityThreshold
            ) {
                if (dx > 0) {
                    onSwipeRight(dx)
                } else {
                    onSwipeLeft(-dx)
                }

                return true
            } else if (Math.abs(dy) > Math.abs(dx) &&
                Math.abs(dy) > mSwipeDistanceThreshold &&
                Math.abs(velocityY) > mSwipeVelocityThreshold
            ) {
                if (dy > 0) {
                    onSwipeDown(dy)
                } else {
                    onSwipeUp(-dy)
                }

                return true
            }

            return false
        }
    }

    open fun onSwipeRight(dx: Float) {}

    open fun onSwipeLeft(dx: Float) {}

    fun onSwipeDown(dy: Float) {}

    fun onSwipeUp(dy: Float) {}

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        var gesture = false

        if (v.id == R.id.detailsImage) {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPrevX = event.rawX
                    mPrevY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val currX = event.rawX
                    val currY = event.rawY

                    mDx = currX - mPrevX
                    mDy = currY - mPrevY
                }

                MotionEvent.ACTION_UP -> if (Math.abs(mDx) > Math.abs(mDy) && Math.abs(mDx) > mSwipeDistanceThreshold) {
                    if (mDx > 0) {
                        onSwipeRight(mDx)
                    } else {
                        onSwipeLeft(-mDx)
                    }

                    return true
                } else if (Math.abs(mDy) > Math.abs(mDx) && Math.abs(mDy) > mSwipeDistanceThreshold) {
                    if (mDy > 0) {
                        onSwipeDown(mDy)
                    } else {
                        onSwipeUp(-mDy)
                    }

                    return true
                }
            }

            gesture = mGestureDetector.onTouchEvent(event)
        }

        return gesture
    }
}

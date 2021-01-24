package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var progress = 0.0f

    private var textDefaultColor = 0
    private var textActiveColor = 0
    private var backgroundActiveColor = 0
    private var backgroundDefaultColor = 0

    private var paintDefault: Paint

    private var paintInProgress: Paint

    private var paintText: Paint

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        Log.e("gds state", new.text)
        contentDescription = new.contentDescription
        invalidate()
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textDefaultColor = getColor(R.styleable.LoadingButton_textDefaultColor, 0)
            textActiveColor = getColor(R.styleable.LoadingButton_textActiveColor, 0)
            backgroundDefaultColor = getColor(R.styleable.LoadingButton_backgroundDefaultColor, 0)
            backgroundActiveColor = getColor(R.styleable.LoadingButton_backgroundActiveColor, 0)
        }

        paintDefault = Paint().apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = 55.0f
            typeface = Typeface.create("", Typeface.BOLD)
            color = backgroundDefaultColor
        }

        paintInProgress = Paint(paintDefault).apply {
            color = backgroundActiveColor
        }

        paintText = Paint(paintDefault).apply {
            color = textDefaultColor
        }
    }

    fun setProgress(newValue: Float) {
        synchronized(this) {
            progress = newValue
            Log.e("GDS", "progress $progress")
            if (progress >= 1f) {
                buttonState = ButtonState.Completed
                progress = 0f
                isClickable = true
            } else {
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //drawing background progress bar
        paintText.color =
            if (buttonState.downloadInProgress == DOWNLOAD_IN_PROGRESS) textActiveColor else textDefaultColor
        synchronized(this) {
            val progressWidth: Int = (widthSize * progress).toInt()
            canvas.drawRect(Rect(0, 0, progressWidth, heightSize), paintInProgress)
            canvas.drawRect(Rect(progressWidth, 0, widthSize, heightSize), paintDefault)
        }

        //drawing title
        val xPos = (canvas.getWidth() / 2.0).toFloat()
        val yPos = ((canvas.getHeight() / 2) - ((paintText.descent() + paintText.ascent()) / 2))
        canvas.drawText(buttonState.text, xPos, yPos, paintText)

        //Drawing progress circle
        val width = paintText.measureText(buttonState.text)
        val radius = heightSize * 0.25f
        val progressInDegrees = 360 * progress
        //defining an oval of 2 radius by 2 radius
        val left = xPos + width / 2 + radius
        val right = left + 2 * radius
        val top = radius
        val bottom = top + 2 * radius
        canvas.drawArc(left, top, right, bottom, 0f, progressInDegrees, true, paintText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        if (!isClickable) return true
        isClickable = false

        animateClick()

        return true
    }

    private fun animateClick() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.6f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.6f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            this, scaleX, scaleY
        ).apply {
            setAnimListener(this)
            repeatCount = 1
            repeatMode = ObjectAnimator.REVERSE
            disableViewDuringAnimation(this@LoadingButton)
            start()
        }
    }

    private fun ObjectAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }

    private fun setAnimListener(animator: ObjectAnimator) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                buttonState = ButtonState.Clicked
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                buttonState = ButtonState.Loading
                super@LoadingButton.performClick()
            }
        })
    }
}
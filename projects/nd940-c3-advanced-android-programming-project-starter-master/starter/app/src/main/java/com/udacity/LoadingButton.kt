package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var paintDefault = Paint().apply{
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
        color = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    }

    private var paintInProgress = Paint().apply{
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
        color = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
    }

    private var paintText = Paint().apply{
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
        color = ResourcesCompat.getColor(resources, R.color.white, null)
    }


    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        Log.e("gds state", new.text)
    }


    init {
        isClickable = true
    }

    private var progress = 0.5f

    fun setProgress(newValue:Float){
        synchronized(this) {
            progress = newValue

            if(progress >= 1f){
                buttonState = ButtonState.Completed
                progress = 0f
                isClickable = true
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(this) {
            val progressWidth: Int = (widthSize * progress).toInt()
            canvas.drawRect(Rect(0, 0, progressWidth, heightSize), paintInProgress)
            canvas.drawRect(Rect(progressWidth, 0, widthSize, heightSize), paintDefault)
        }

        val xPos = (canvas.getWidth() / 2.0).toFloat()
        val yPos = ((canvas.getHeight() / 2) - ((paintText.descent() + paintText.ascent()) / 2))
        canvas.drawText(buttonState.text, xPos, yPos, paintText)
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

        //get download progress
        //invalidate
    }

    override fun performClick(): Boolean {
        if(!isClickable) return true
        isClickable = false


        buttonState = ButtonState.Loading

        contentDescription = "Download in progress"

        invalidate()

        super.performClick()

        return true
    }
}
package com.scitrader.finance.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import com.scichart.charting.visuals.SciChartSurface
import com.scitrader.finance.R
import org.json.JSONObject

abstract class ResizeableStackLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val selectedDividerBounds = RectF()
    private val selectedDividerTouchBounds = RectF()
    private val draggableDividerBounds = RectF()

    private var isDragging = false
    private var lastTouchY = Float.NaN
    private var selectedDividerIndex = -1
    private var dividerTopLimit = Float.NaN
    private var dividerBottomLimit = Float.NaN

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private var isTouchWithinBounds = false

        override fun onDown(e: MotionEvent): Boolean {
            isTouchWithinBounds = isTouchWithinBounds(e.y)

            return isTouchWithinBounds
        }

        private fun isTouchWithinBounds(y: Float): Boolean {
            for (i in 0 until childCount - 1) {
                val topChild = getChildAt(i)

                val viewBottom = topChild.bottom

                val offset = dividerSize / 2f
                val top = viewBottom - offset
                val bottom = viewBottom + offset

                val verticalPadding = touchPadding
                val touchTop = top - verticalPadding
                val touchBottom = bottom + verticalPadding

                if (touchTop < y && touchBottom > y) {
                    val width = width.toFloat()

                    selectedDividerTouchBounds.set(0f, touchTop, width, touchBottom)
                    selectedDividerBounds.set(0f, top, width, bottom)
                    selectedDividerIndex = i

                    val bottomChild = getChildAt(selectedDividerIndex + 1)
                    val bottomLp = bottomChild.layoutParams as LayoutParams

                    val axisHeightRatio = getAxisHeightRatio(bottomChild)
                    dividerBottomLimit = bottomChild.bottom - (height * (bottomLp.minHeightRatio + axisHeightRatio))

                    return true
                }
            }

            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // handle touch down at original point of scroll and diff between current and original point
            handleTouchDown(e1.x, e1.y)
            handleTouchMove(e2.x, e2.y)

            return isTouchWithinBounds
        }
    } )

    private var selectedDividerDrawable: Drawable? = null
        set(value) {
            if (field == value) return

            field = value
            invalidate()
        }

    private var draggableDividerDrawable: Drawable? = null
        set(value) {
            if (field == value) return

            field = value
            invalidate()
        }

    private var draggableDividerIconDrawable: Drawable? = null
        set(value) {
            if (field == value) return

            field = value
            invalidate()
        }

    private var dividerSize: Float = Float.NaN
        set(value) {
            if (field == value) return

            field = value
            invalidate()
        }

    private var touchPadding: Float = 0f

    init {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(
                attrs,
                R.styleable.ResizeableStackLayout
            )
            try {
                selectedDividerDrawable =
                    attributes.getDrawable(R.styleable.ResizeableStackLayout_selectedDividerDrawable)
                        ?: ContextCompat.getDrawable(
                            context,
                            R.drawable.draggable_divider
                        )

                draggableDividerDrawable =
                    attributes.getDrawable(R.styleable.ResizeableStackLayout_draggableDividerDrawable)
                        ?: ContextCompat.getDrawable(
                            context,
                            R.drawable.draggable_divider
                        )

                draggableDividerIconDrawable =
                    attributes.getDrawable(R.styleable.ResizeableStackLayout_draggableIconDrawable)
                        ?: ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_resize_arrows
                        )

                val resources = context.resources
                val defaultDividerSize = resources.getDimension(R.dimen.dividerSize)
                dividerSize = attributes.getDimension(R.styleable.ResizeableStackLayout_dividerSize,
                    defaultDividerSize
                )

                val defaultTouchPadding = resources.getDimension(R.dimen.touchPadding)
                touchPadding = attributes.getDimension(R.styleable.ResizeableStackLayout_touchPadding,
                    defaultTouchPadding
                )

            } finally {
                attributes.recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // todo need to rewrite when we'll add reorganizing panes
        val height = b - t

        val childCount = childCount

        if (childCount > 0) {
            var remainingHeight = height

            var index = childCount - 1
            while (index > 0) {
                val view = getChildAt(index)
                if(view.visibility != View.GONE) {
                    val lp = view.layoutParams as LayoutParams
                    val viewHeight = ((lp.heightRatio + getAxisHeightRatio(view)) * height).toInt()

                    val childBottom = remainingHeight
                    remainingHeight -= viewHeight

                    view.layout(l, remainingHeight, r, childBottom)
                }

                index--
            }

            getChildAt(0).layout(l, 0, r, remainingHeight)
        }
    }

    private fun getAxisHeightRatio(view: View) : Float {
        //TODO: rewrite without casting to SciChartSurface
        return (view as? SciChartSurface)?.xAxes?.firstOrNull()?.let {
            if (visibility == View.VISIBLE) {
                (it.layoutHeight.toFloat() / height)
            } else 0F
        } ?: 0F
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev) || gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleTouchDown(x, y)
            MotionEvent.ACTION_MOVE -> handleTouchMove(x, y)
            MotionEvent.ACTION_UP -> handleTouchUp(x, y)
        }
        return true
    }

    private fun handleTouchDown(x: Float, y: Float) {
        if (selectedDividerTouchBounds.contains(x, y)) {
            isDragging = true

            val halfHeight = selectedDividerBounds.height() / 2f
            draggableDividerBounds.set(selectedDividerBounds.left, y - halfHeight, selectedDividerBounds.right, y + halfHeight)

            lastTouchY = y

            invalidate()

            handleTouchMove(selectedDividerBounds.centerX(), selectedDividerBounds.centerY())
        }
    }

    private fun handleTouchMove(x: Float, y: Float) {
        val firstChild = getChildAt(0)
        val firstLp = firstChild.layoutParams as LayoutParams

        dividerTopLimit = height * firstLp.minHeightRatio

        for (i in 1 until selectedDividerIndex + 1) {
            val child = getChildAt(i)

            dividerTopLimit += child.height
        }

        if (isDragging) {
            var needToUpdate = true
            draggableDividerBounds.offset(0f, y - lastTouchY)

            if(draggableDividerBounds.centerY() < dividerTopLimit) {
                draggableDividerBounds.offset(0f, dividerTopLimit - draggableDividerBounds.centerY())
                needToUpdate = false
            }

            if(draggableDividerBounds.centerY() > dividerBottomLimit) {
                draggableDividerBounds.offset(0f, dividerBottomLimit - draggableDividerBounds.centerY())
                needToUpdate = false
            }

            if(needToUpdate)
                lastTouchY = y

            invalidate()
        }
    }

    private fun handleTouchUp(x: Float, y: Float) {
        if (isDragging) {
            isDragging = false

            lastTouchY = Float.NaN

            val diff = draggableDividerBounds.centerY() - selectedDividerBounds.centerY()
            updateChildLayoutParams(diff)

            requestLayout()
            invalidate()

            selectedDividerBounds.setEmpty()
            selectedDividerTouchBounds.setEmpty()
            draggableDividerBounds.setEmpty()
        }
    }

    private fun updateChildLayoutParams(diff: Float) {
        if (childCount <= selectedDividerIndex + 1) return

        val viewToResize = getChildAt(selectedDividerIndex + 1)
        val layoutParams = viewToResize.layoutParams as LayoutParams

        layoutParams.heightRatio -= diff / height

        onChildLayoutParamsUpdate()
    }

    abstract fun onChildLayoutParamsUpdate()

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if(isDragging) {
            selectedDividerDrawable?.let {
                it.state = drawableState
                it.setBounds(
                    selectedDividerBounds.left.toInt(),
                    selectedDividerBounds.top.toInt(),
                    selectedDividerBounds.right.toInt(),
                    selectedDividerBounds.bottom.toInt()
                )

                it.draw(canvas)
            }

            draggableDividerDrawable?.let {
                it.state = drawableState
                it.setBounds(
                    draggableDividerBounds.left.toInt(),
                    draggableDividerBounds.top.toInt(),
                    draggableDividerBounds.right.toInt(),
                    draggableDividerBounds.bottom.toInt()
                )

                it.draw(canvas)
            }

            draggableDividerIconDrawable?.let {
                it.state = drawableState
                val iconHalfWidth = it.intrinsicWidth / 2
                val iconHalfHeight = it.intrinsicHeight / 2
                it.setBounds(
                    (draggableDividerBounds.centerX() - iconHalfWidth).toInt(),
                    (draggableDividerBounds.centerY() - iconHalfHeight).toInt(),
                    (draggableDividerBounds.centerX() + iconHalfWidth).toInt(),
                    (draggableDividerBounds.centerY() + iconHalfHeight).toInt()
                )

                it.draw(canvas)
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return super.checkLayoutParams(p) && p is LayoutParams
    }

    class LayoutParams : ViewGroup.LayoutParams {
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        constructor(width: Int, height: Int) : super(width, height)

        var heightRatio: Float = 0.1f
        var minHeightRatio = 0.1f

        companion object {
            fun convertToJsonString(layoutParams: LayoutParams) : String {
                val json = JSONObject()

                with(json) {
                    put("width", layoutParams.width)
                    put("height", layoutParams.height)
                    put("minHeightRatio", layoutParams.minHeightRatio.toDouble())
                    put("heightRatio", layoutParams.heightRatio.toDouble())
                }

                return json.toString()
            }

            fun assignLayoutParamsFromJsonString(layoutParams: LayoutParams, jsonString: String) {
                with(JSONObject(jsonString)) {
                    layoutParams.width = getInt("width")
                    layoutParams.height = getInt("height")

                    layoutParams.minHeightRatio = getDouble("minHeightRatio").toFloat()
                    layoutParams.heightRatio = getDouble("heightRatio").toFloat()
                }
            }
        }
    }
}

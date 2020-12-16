package lib.vaccae.opencv

import android.content.Context
import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-11-26 14:51
 * 功能模块说明：
 */
class ViewOverLay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var mText: String? = null
    private var mBmp: Bitmap? = null
    private var mRects: List<Rect>? = null

    //图片的与实际尺寸比
    private var mScaleWidth = 1.0f
    private var mScaleHeight = 1.0f

    private val textpaint = TextPaint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context!!, android.R.color.holo_blue_light)
        strokeWidth = 10f
        textSize = 60f
        isFakeBoldText = true
    }

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context!!, android.R.color.holo_red_light)
        strokeWidth = 5f
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {
            mBmp?.let {
                canvas?.drawBitmap(it, x, y, Paint())
            }
            mRects?.let {
                it.forEach { p ->
                    p.left = (p.left / mScaleWidth).toInt()
                    p.top = (p.top / mScaleHeight).toInt()
                    p.right = (p.right / mScaleWidth).toInt()
                    p.bottom = (p.bottom / mScaleHeight).toInt()
                    canvas?.drawRect(p, paint);
                }
            }
            mText?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val builder = StaticLayout.Builder.obtain(it, 0, it.length, textpaint, width)
                    val myStaticLayout = builder.build()
                    canvas?.let { t ->
                        t.translate(x, y)
                        myStaticLayout.draw(t)
                    }
                } else {
                    canvas?.drawText(it, x, y, textpaint)
                }
            }
        } catch (e: Exception) {
            e.message?.let {
                Snackbar.make(this, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun drawInit() {
        mBmp = null
        mText = ""
        mRects = null
        invalidate()
    }

    fun drawBitmap(bmp: Bitmap?, isScale: Boolean = true) {
        bmp?.let {
            mBmp = if (isScale)
                Bitmap.createScaledBitmap(bmp, width, height, true)
            else
                bmp
        }
        invalidate()
    }

    fun drawText(str: String?) {
        mText = str;
        invalidate()
    }

    fun drawRect(rect: List<Rect>?, w: Int = width, h: Int = height) {
        rect?.let {
            mRects = rect;
            mScaleWidth = w.toFloat() / width
            mScaleHeight = h.toFloat() / height
        }
        invalidate()
    }
}
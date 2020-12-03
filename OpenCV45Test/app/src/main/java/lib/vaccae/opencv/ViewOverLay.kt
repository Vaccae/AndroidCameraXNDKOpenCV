package lib.vaccae.opencv

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

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

    private val textpaint = TextPaint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context!!, android.R.color.holo_blue_light)
        strokeWidth = 10f
        textSize = 60f
        isFakeBoldText = true
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mBmp?.let {
            canvas?.drawBitmap(it, x, y, Paint())
        }
        mText?.let {
            val builder = StaticLayout.Builder.obtain(it, 0, it.length, textpaint, width)
            val myStaticLayout = builder.build()
            canvas?.let { t ->
                t.translate(x, y)
                myStaticLayout.draw(t)
            }
        }
    }

    fun drawBitmap(bmp: Bitmap?) {
        bmp?.let {
            mBmp = Bitmap.createScaledBitmap(bmp, width,height,true)
        }
        invalidate()
    }

    fun drawText(str: String?) {
        mText = str;
        invalidate()
    }

}
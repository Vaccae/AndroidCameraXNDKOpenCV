package lib.vaccae.opencv

import android.content.Context
import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
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

    private var mQrCodes: List<QrCode>? = null

    //人脸贴图
    private var mFaceBitmap = BitmapFactory.decodeResource(resources, R.drawable.vaccae)
    private var mFaceRect = Rect(0, 0, mFaceBitmap.width, mFaceBitmap.height)
    private var mFaceRects: List<Rect>? = null

    //图片的与实际尺寸比
    private var mScaleWidth = 1.0f
    private var mScaleHeight = 1.0f

    private val textpaint = TextPaint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context!!, android.R.color.holo_purple)
        strokeWidth = 10f
        textSize = 60f
        isFakeBoldText = true
    }



    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context!!, android.R.color.holo_purple)
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

            mFaceRects?.let {
                it.forEach { p ->
                    p.left = (p.left / mScaleWidth).toInt() - 10
                    p.top = (p.top / mScaleHeight).toInt() - 10
                    p.right = (p.right / mScaleWidth).toInt() + 10
                    p.bottom = (p.bottom / mScaleHeight).toInt() + 10

                    canvas?.drawBitmap(
                        mFaceBitmap, mFaceRect, p, Paint()
                    )
                }
            }

            //画二维码显示
            mQrCodes?.let {
                it.forEach { t ->
                    //Log.i("point:", "pt:${t.points} msg:"+t.msg)
                    t.points?.let { pt ->
                        //画坐标点的线
                        //Log.i("point:", "$pt")
                        for (i in 0 until pt.size) {
                            if (i == pt.size - 1) {
                                canvas?.drawLine(pt[i].x, pt[i].y, pt[0].x, pt[0].y, paint)
                            } else {
                                canvas?.drawLine(pt[i].x, pt[i].y, pt[i + 1].x, pt[i + 1].y, paint)
                            }
                        }

                        t.msg?.let { m ->
                            //输出识别的QRCode信息
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val builder =
                                    StaticLayout.Builder.obtain(
                                        m,
                                        0,
                                        m.length,
                                        textpaint,
                                        width - pt[pt.size - 1].x.toInt()
                                    )
                                val myStaticLayout = builder.build()
                                canvas?.translate(pt[pt.size - 1].x, pt[pt.size - 1].y + 10)
                                myStaticLayout.draw(canvas)
                            } else {
                                canvas?.drawText(
                                    m,
                                    pt[pt.size - 1].x,
                                    pt[pt.size - 1].y + 10,
                                    textpaint
                                )
                            }
                        }
                    }
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
        mQrCodes = null
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

    fun drawfaceBitmap(rect: List<Rect>?, w: Int = width, h: Int = height) {
        rect?.let {
            mFaceRects = rect
            mScaleWidth = w.toFloat() / width
            mScaleHeight = h.toFloat() / height
        }
        invalidate()
    }

    fun drawQrCodes(qrcodes: List<QrCode>?, w: Int = width, h: Int = height) {
        qrcodes?.let {
            mQrCodes = qrcodes
            mScaleWidth = w.toFloat() / width
            mScaleHeight = h.toFloat() / height

            //计算偏移坐标点
            mQrCodes?.forEach {
                it.points?.forEach { pt ->
                    pt.x = (pt.x / mScaleWidth)
                    pt.y = (pt.y / mScaleHeight)
                }
            }
        }
        invalidate()
    }
}
package lib.vaccae.opencv

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.material.snackbar.Snackbar


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-12-01 13:31
 * 功能模块说明：
 */
internal class AnalysisCvDetector(typeid: Int, view: ViewOverLay) : ImageAnalysis.Analyzer {

    //当前检测方式 0-灰度图  1-人脸检测
    private var mTypeId = typeid
    private var mView = view
    private var jni = OpenCVJNI()

    //设置检测方式 0-灰度显示  1-人脸检测
    fun setTypeId(int: Int) {
        mTypeId = int
        //清空当前画布
        //使用postDelayed替换Post是防止出现post加入缓存队列，但并未刷新UI的情况
        mView.postDelayed({
            mView.drawInit()
        }, 50)
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imgProxy: ImageProxy) {
        val image = imgProxy.image
        if (image == null) {
            imgProxy.close()
            return
        }


        try {
            //将ImageProxy图像转为ByteArray
            val buffer = ImageUtils.imageProxyToByteArray(imgProxy)
            var bytes: ByteArray? = buffer
            var w = image.width
            var h = image.height

            //判断如果是竖屏，图像旋转90度
            if (mView.width < mView.height) {
                //根据宽度和高度将图像旋转90度
                bytes = ImageUtils.rotateYUVDegree90(buffer, w, h)
                //设置变量当宽和高修改过来
                w = image.height
                h = image.width
            } else {
                //用的横屏PAD测试后，发布横屏的要将图像旋转180度
                //正常的横屏应该不用处理这个，如果遇到不对，可以屏蔽这一句
                bytes = ImageUtils.rotateYUVDegree180(buffer, w, h)
            }

            if (mTypeId == 0) {
                //调用Jni实现灰度图并返回图像的Pixels
                val grayPixels = jni.grayShow(bytes!!, w, h)
                //将Pixels转换为Bitmap然后画图
                grayPixels?.let {
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.setPixels(it, 0, w, 0, 0, w, h)
                    val str = "width:${w}" + " height:${h}"

                    mView.post {
                        mView.drawBitmap(bmp)
                        mView.drawText(str)
                    }
                }
            } else if (mTypeId == 1) {
                //调用人脸检测返回矩形
                val detectorRects = jni.facedetector(bytes!!, w, h)
                //判断如果检测到
                detectorRects?.let {
                    mView.post {
                        mView.drawRect(it, w, h)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("except", e.message.toString())
            Snackbar.make(mView, e.message.toString(), Snackbar.LENGTH_SHORT).show()
        } finally {
            imgProxy.close()
        }
    }

}
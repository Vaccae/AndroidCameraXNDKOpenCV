package lib.vaccae.opencv

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-12-01 13:31
 * 功能模块说明：
 */
internal class AnalysisCvDetector(typeid: Int, view: ViewOverLay) : ImageAnalysis.Analyzer {
    companion object {
        //加载动态库
        init {
            System.loadLibrary("opencv-lib")
        }
    }
    //JNI native
    //人脸检测
    external fun facedetector(byte: ByteArray, width: Int, height: Int):List<RectF>
    //灰度显示
    external fun grayShow(bytes: ByteArray, width: Int, height: Int): IntArray?

    //当前检测方式 0-灰度图  1-人脸检测
    private var mTypeId = typeid
    private var mView = view
    private var count=0;

    //设置检测方式
    fun setTypeId(int: Int){
        mTypeId = int
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
            //根据宽度和高度将图像旋转90度
            val bytes =ImageUtils.rotateYUVDegree90(buffer, image.width, image.height)

            if(mTypeId == 0){
                //调用Jni实现灰度图并返回图像的Pixels
                val grayPixels = grayShow(bytes!!, image.height, image.width)
                //将Pixels转换为Bitmap然后画图
                grayPixels?.let {
                    val bmp = Bitmap.createBitmap(image.height, image.width, Bitmap.Config.ARGB_8888)
                    bmp.setPixels(it, 0, image.height, 0, 0, image.height, image.width)
                    val str = "width:${image.width}"+" height:${image.height}"

                    mView.post {
                        mView.drawBitmap(bmp)
                        mView.drawText(str)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("except", e.message.toString())
            mView.post { mView.drawText(e.message) }
        } finally {
            imgProxy.close()
        }
    }


}
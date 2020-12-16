package lib.vaccae.opencv

import android.graphics.Rect
import android.graphics.RectF

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-12-04 09:34
 * 功能模块说明：
 */
class OpenCVJNI {
    companion object {
        //加载动态库
        init {
            System.loadLibrary("opencv-lib")
        }

    }

    //JNI native
    //人脸检测
    external fun initFaceDetector(modelBinary:String,modelDesc :String):Boolean
    external fun facedetector(byte: ByteArray, width: Int, height: Int): List<Rect>?
    //灰度显示
    external fun grayShow(bytes: ByteArray, width: Int, height: Int): IntArray?


}
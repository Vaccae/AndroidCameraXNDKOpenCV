#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "System.out"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;

//传入的图像转为Mat
Mat byteArrayToMat(JNIEnv *env, jbyteArray bytes, jint width, jint height) {
    try {
        Mat mBgr;
        //读取Yuv的图片数据
        jbyte *_yuv = env->GetByteArrayElements(bytes, 0);
        //加载为Mat
        Mat mYuv(height + height / 2, width, CV_8UC1, (uchar *) _yuv);

        //将Yuv420转为BGR的Mat
        cvtColor(mYuv, mBgr, COLOR_YUV2BGRA_I420);

        env->ReleaseByteArrayElements(bytes, _yuv, 0);
        mYuv.release();

        return mBgr;
    } catch (cv::Exception e) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
    }
}

//将Mat转为IntArray
jintArray matToIntArray(JNIEnv *env, Mat &img){
    int size = img.cols * img.rows;
    jintArray result = env->NewIntArray(size);
    uchar *output = img.data;
    env->SetIntArrayRegion(result, 0, size, (const jint *) output);
    return result;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_lib_vaccae_opencv_AnalysisCvDetector_facedetector(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                                       jint width, jint height) {
    Mat src = byteArrayToMat(env, bytes, width, height);

}

extern "C"
JNIEXPORT jintArray JNICALL
Java_lib_vaccae_opencv_AnalysisCvDetector_grayShow(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                                   jint width, jint height) {
    try {
        //根据传入的Byte生成Mat
        Mat src = byteArrayToMat(env, bytes, width, height);

        //灰度图
        Mat gray;
        cvtColor(src, gray, COLOR_BGRA2GRAY);

        Mat resultMat;
        cvtColor(gray, resultMat, COLOR_GRAY2BGRA);

        //转换生成图片的intArray
        jintArray result = matToIntArray(env, resultMat);
        //释放Mat
        resultMat.release();
        src.release();
        gray.release();
        return result;
    } catch (cv::Exception e) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
    }
}
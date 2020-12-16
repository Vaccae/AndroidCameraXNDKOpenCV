#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include "facedetect.h"

#define LOG_TAG "System.out"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;
using namespace std;

facedetect _faceDetect;

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
jintArray matToIntArray(JNIEnv *env, Mat &img) {
    int size = img.cols * img.rows;
    jintArray result = env->NewIntArray(size);
    uchar *output = img.data;
    env->SetIntArrayRegion(result, 0, size, (const jint *) output);
    return result;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_lib_vaccae_opencv_OpenCVJNI_grayShow(JNIEnv *env, jobject thiz, jbyteArray bytes,
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

extern "C"
JNIEXPORT jboolean JNICALL
Java_lib_vaccae_opencv_OpenCVJNI_initFaceDetector(JNIEnv *env, jobject thiz, jstring model_binary,
                                                  jstring model_desc) {
    try {
        string sbinary = env->GetStringUTFChars(model_binary, 0);
        string sdesc = env->GetStringUTFChars(model_desc, 0);
        //初始化DNN
        _faceDetect = facedetect();
        jboolean res = _faceDetect.InitDnnNet(sbinary, sdesc);

        return res;
    } catch (Exception e) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_lib_vaccae_opencv_OpenCVJNI_facedetector(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                              jint width, jint height) {
    try {
        Mat src = byteArrayToMat(env, bytes, width, height);

        //获取ArrayList类引用
        jclass list_jcls = env->FindClass("java/util/ArrayList");
        if (list_jcls == nullptr) {
            LOGI("ArrayList没找到相关类!");
            return 0;
        }
        //获取ArrayList构造函数id
        jmethodID list_init = env->GetMethodID(list_jcls, "<init>", "()V");
        //创建一个ArrayList对象
        jobject list_obj = env->NewObject(list_jcls, list_init);
        //获取ArrayList对象的add()的methodID
        jmethodID list_add = env->GetMethodID(list_jcls, "add", "(Ljava/lang/Object;)Z");

        //人脸检测
        vector<vector<int>> outRects = _faceDetect.Detect(src);
        if(outRects.size()>0){
            jclass rect_jcls = env->FindClass("android/graphics/Rect");
            jmethodID  rect_init = env->GetMethodID(rect_jcls,"<init>","(IIII)V");
            for(int i=0;i<outRects.size();++i){
                vector<int> point = outRects[i];
                jobject tmprect = env->NewObject(rect_jcls,rect_init,
                                                 (int)point[0],
                                                 (int)point[1],
                                                 (int)point[2],
                                                 (int)point[3]);
                env->CallBooleanMethod(list_obj, list_add, tmprect);
            }
        }
        return list_obj;
    } catch (Exception e) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch (...) {
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
    }
}

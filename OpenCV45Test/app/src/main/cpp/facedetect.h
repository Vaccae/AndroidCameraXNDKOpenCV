//
// Created by 36574 on 2020-12-04.
//

#ifndef OPENCV45TEST_FACEDETECT_H
#define OPENCV45TEST_FACEDETECT_H

#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

class facedetect {
private:
    string _modelbinary, _modeldesc;
    dnn::Net _net;
public:
    //置信阈值
    float confidenceThreshold;
    double inScaleFactor;
    int inWidth;
    int inHeight;
    Scalar meanVal;
    //初始化Dnn
    bool InitDnnNet(string modelbinary, string modeldesc);

    //人脸检测
    vector<Rect> DetectToRect(Mat frame);

    vector<vector<int>> Detect(Mat frame);

};


#endif //OPENCV45TEST_FACEDETECT_H

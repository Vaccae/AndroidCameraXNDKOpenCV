package lib.vaccae.opencv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    val TAG = "OpenCVDetector"
    lateinit var viewFinder: PreviewView
    lateinit var vOverLay: ViewOverLay
    private lateinit var analysisdetector: AnalysisCvDetector

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "未开启权限.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private lateinit var cameraExecutor: ExecutorService
    var cameraProvider: ProcessCameraProvider? = null//相机信息
    var preview: Preview? = null//预览对象
    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//当前相机
    var camera: Camera? = null//相机对象
    private var imageCapture: ImageCapture? = null//拍照用例
    var videoCapture: VideoCapture? = null//录像用例
    var imageAnalyzer: ImageAnalysis? = null//图片分析

    private val RATIO_4_3_VALUE = 4.0 / 3.0
    private val RATIO_16_9_VALUE = 16.0 / 9.0


    private fun aspectRatio(width: Int, height: Int): Int {
        val max = width.coerceAtLeast(height)
        val min = width.coerceAtMost(height)
        val previewRatio = max.toDouble() / min.toDouble()
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewFinder = findViewById(R.id.viewFinder)
        vOverLay = findViewById(R.id.viewOverlay)
        vOverLay.bringToFront()

        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d("screen:","width:${metrics.widthPixels}"+ " height:${metrics.heightPixels}")
        //宽高比
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        //旋转角度
        var rotation = viewFinder.display.rotation

        //实例化AnalysisCvDetector
        analysisdetector = AnalysisCvDetector(0, vOverLay)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()//获取相机信息

            //预览配置
            preview = Preview.Builder()
                //宽高比
                .setTargetAspectRatio(screenAspectRatio)
                //设置实际尺寸
                //.setTargetResolution(Size(metrics.widthPixels ,metrics.heightPixels))
                //旋转角度
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            //拍照用例配置
            imageCapture = ImageCapture.Builder()
                //宽高比
                .setTargetAspectRatio(screenAspectRatio)
                //设置实际尺寸
                //.setTargetResolution(Size(metrics.widthPixels ,metrics.heightPixels))
                //旋转角度
                .setTargetRotation(rotation)
                .build()
            //图像分析接口
            imageAnalyzer = ImageAnalysis.Builder()
                //宽高比
                .setTargetAspectRatio(screenAspectRatio)
                //设置实际尺寸
                //.setTargetResolution(Size(metrics.widthPixels ,metrics.heightPixels))
                //旋转角度
                .setTargetRotation(rotation)
                .build()
                .also { it ->
                    it.setAnalyzer(cameraExecutor, analysisdetector)
                }

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//使用后置摄像头
            videoCapture = VideoCapture.Builder()//录像用例配置
//                //宽高比
//                .setTargetAspectRatio(screenAspectRatio)
//                //旋转角度
//                .setTargetRotation(rotation)
//                .setAudioRecordSource(AudioSource.MIC)//设置音频源麦克风
                .build()

            try {
                cameraProvider?.unbindAll()//先解绑所有用例
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,
                    imageAnalyzer
                )//绑定用例
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
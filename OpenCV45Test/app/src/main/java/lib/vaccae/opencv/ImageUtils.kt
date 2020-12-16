package lib.vaccae.opencv

import androidx.camera.core.ImageProxy


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-12-02 14:38
 * 功能模块说明：
 */
class ImageUtils {
    companion object StaticFun {
        //将ImageProxy图片YUV_420_888转换为位图的byte数组
        fun imageProxyToByteArray(image: ImageProxy): ByteArray {
            val yuvBytes = ByteArray(image.width * (image.height + image.height / 2))
            val yPlane = image.planes[0].buffer
            val uPlane = image.planes[1].buffer
            val vPlane = image.planes[2].buffer

            yPlane.get(yuvBytes, 0, image.width * image.height)

            val chromaRowStride = image.planes[1].rowStride
            val chromaRowPadding = chromaRowStride - image.width / 2

            var offset = image.width * image.height
            if (chromaRowPadding == 0) {

                uPlane.get(yuvBytes, offset, image.width * image.height / 4)
                offset += image.width * image.height / 4
                vPlane.get(yuvBytes, offset, image.width * image.height / 4)
            } else {
                for (i in 0 until image.height / 2) {
                    uPlane.get(yuvBytes, offset, image.width / 2)
                    offset += image.width / 2
                    if (i < image.height / 2 - 2) {
                        uPlane.position(uPlane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until image.height / 2) {
                    vPlane.get(yuvBytes, offset, image.width / 2)
                    offset += image.width / 2
                    if (i < image.height / 2 - 1) {
                        vPlane.position(vPlane.position() + chromaRowPadding)
                    }
                }
            }

            return yuvBytes
        }


        //后置摄像头旋转90度
        fun rotateYUVDegree90(
            data: ByteArray,
            imageWidth: Int,
            imageHeight: Int
        ): ByteArray? {
            val yuv = ByteArray(imageWidth * imageHeight * 3 / 2)
            // Rotate the Y luma
            var i = 0
            for (x in 0 until imageWidth) {
                for (y in imageHeight - 1 downTo 0) {
                    yuv[i] = data[y * imageWidth + x]
                    i++
                }
            }
            // Rotate the U and V color components
            i = imageWidth * imageHeight * 3 / 2 - 1
            var x = imageWidth - 1
            while (x > 0) {
                for (y in 0 until imageHeight / 2) {
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + x]
                    i--
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + (x - 1)]
                    i--
                }
                x -= 2
            }
            return yuv
        }

        fun rotateYUVDegree180(
            data: ByteArray,
            imageWidth: Int,
            imageHeight: Int
        ): ByteArray? {
            val yuv = ByteArray(imageWidth * imageHeight * 3 / 2)
            var i = 0
            var count = 0
            i = imageWidth * imageHeight - 1
            while (i >= 0) {
                yuv[count] = data[i]
                count++
                i--
            }

            i = imageWidth * imageHeight * 3 / 2 - 1
            while (i >= imageWidth
                * imageHeight
            ) {
                yuv[count++] = data[i - 1]
                yuv[count++] = data[i]
                i -= 2
            }
            return yuv
        }

        //旋转270度
        fun rotateYUVDegree270(
            data: ByteArray,
            imageWidth: Int,
            imageHeight: Int
        ): ByteArray? {
            val yuv = ByteArray(imageWidth * imageHeight * 3 / 2)
            // Rotate the Y luma
            var i = 0
            for (x in imageWidth - 1 downTo 0) {
                for (y in 0 until imageHeight) {
                    yuv[i] = data[y * imageWidth + x]
                    i++
                }
            } // Rotate the U and V color components
            i = imageWidth * imageHeight
            var x = imageWidth - 1
            while (x > 0) {
                for (y in 0 until imageHeight / 2) {
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + (x - 1)]
                    i++
                    yuv[i] = data[imageWidth * imageHeight + y * imageWidth + x]
                    i++
                }
                x -= 2
            }
            return yuv
        }


        //旋转270度加翻转
        fun rotateYUVDegree270AndMirror(
            data: ByteArray,
            imageWidth: Int,
            imageHeight: Int
        ): ByteArray? {
            val yuv = ByteArray(imageWidth * imageHeight * 3 / 2)
            // Rotate and mirror the Y luma
            var i = 0
            var maxY = 0
            for (x in imageWidth - 1 downTo 0) {
                maxY = imageWidth * (imageHeight - 1) + x * 2
                for (y in 0 until imageHeight) {
                    yuv[i] = data[maxY - (y * imageWidth + x)]
                    i++
                }
            }
            // Rotate and mirror the U and V color components
            val uvSize = imageWidth * imageHeight
            i = uvSize
            var maxUV = 0
            var x = imageWidth - 1
            while (x > 0) {
                maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize
                for (y in 0 until imageHeight / 2) {
                    yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)]
                    i++
                    yuv[i] = data[maxUV - (y * imageWidth + x)]
                    i++
                }
                x -= 2
            }
            return yuv
        }
    }
}
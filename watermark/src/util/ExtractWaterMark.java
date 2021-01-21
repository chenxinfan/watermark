package util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 提取数字水印
 */
public class ExtractWaterMark {
    static{
        //加载opencv动态库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 提取水印
     * 一般情况提取的水印只有一张图片，
     * 可能会出现多张水印的提取结果，所以需要变量waterMarkOtherPath(水印文件夹)
     * @param srcPath：图片路径
     * @param waterPath：提取出来的水印路径
     * @param waterMarkOtherPath：水印文件夹
     * @throws IOException
     */
    public static void getImageWatermark(String srcPath, String waterPath, String waterMarkOtherPath) throws IOException {
        Mat originalGraph = Imgcodecs.imread(srcPath);
        //无论什么大小的图片，我们都直接将其变成960 * 1280的大小
        String tempSrcPath = "temporary_document\\temporary.bmp";
        ImageUtil.reset(srcPath,tempSrcPath);
        originalGraph = Imgcodecs.imread(tempSrcPath);

        int mark[][] = extractMethod(originalGraph);

        //是否旋转
        boolean isRotate = true;
        int angle = 90;

        while(isRotate) {
            int count_0 = 0, count_1 = 0;
            /*
            注意这里是28，因为计算前28行就可以大致得到图片的旋转方式
             */
            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < mark[0].length; j++) {
                    if (mark[i][j] == 1) {
                        count_1++;
                    } else {
                        count_0++;
                    }
                }
            }
            int val = Math.abs(count_1 - count_0);
            if (val >= 750 && val <= 1000) {
                //图片位置正确，生成水印
                isRotate = false;
                int waterMark[][] = new int[64][64];
                for (int i = 0; i < waterMark.length; i++) {
                    for (int j = 0; j < waterMark[0].length; j++) {
                        waterMark[i][j] = mark[28 + i][48 + j];
                    }
                }
                matrixToBinaryPhoto(waterMark, waterPath);
                return;
            } else {
                if (angle == 360) {
                    break;
                }
                BufferedImage dst = ImageUtil.Rotate(ImageIO.read(new FileInputStream(srcPath)), angle);
                tempSrcPath = "temporary_document\\temporary.bmp";
                ImageIO.write(dst, "bmp", new File(tempSrcPath));
                originalGraph = Imgcodecs.imread(tempSrcPath);
                tempSrcPath = "temporary_document\\temporary.bmp";
                ImageUtil.reset(tempSrcPath, tempSrcPath);
                originalGraph = Imgcodecs.imread(tempSrcPath);
                mark = extractMethod(originalGraph);
                angle += 90;
            }
        }

        if(isRotate) {
            //如果任然为true
            originalGraph = Imgcodecs.imread(srcPath);
            int rows = originalGraph.rows();
            int cols = originalGraph.cols();
            File dir = new File(waterMarkOtherPath);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    mark = extractMethod(originalGraph.submat(i, rows,j , cols));
                    matrixToBinaryPhoto(mark,dir.getAbsolutePath()+"\\waterMark_out"+i+j+".jpg");
                }
            }
        }

    }

    /*
     * 提取分块
     */
    public static int[][] extractMethod(Mat originalGraph) {
        List<Mat> allPlanes = new ArrayList<Mat>();

        //得到原图的Ycbcr
        Mat Ycbcr= new Mat();
        Imgproc.cvtColor(originalGraph, Ycbcr,Imgproc.COLOR_RGB2YCrCb);

        Core.split(originalGraph, allPlanes);
        Mat YMat = allPlanes.get(0);
        int rows = originalGraph.rows();
        int cols = originalGraph.cols();
        int mark[][] = new int[rows / 8][cols / 8];
        int markX = 0;
        int markY = 0;
        for(int i = 0; i < rows / 8; i++) {
            for(int j = 0; j < cols / 8; j++) {
                Mat block = getImageValue(YMat,i,j);
                //对分块进行DCT变换
                Core.dct(block, block);

                int x1 = 1, y1 = 2;
                int x2 = 2, y2 = 1;

                double a[] = block.get(x1, y1);
                double b[] = block.get(x2, y2);

                if(a[0] > b[0]){
                    mark[markX][markY] =1;
                }else {
                    mark[markX][markY] = 0;
                }

                markY++;
                if(markY == mark[0].length) {
                    markX++;
                    markY = 0;
                }
            }
        }
        return mark;
    }

    /**
     *将水印信息的二维数组转换为一张图片
     */
    public static void matrixToBinaryPhoto(int[][] watermark,String dstPath) {
        Mat binaryPhoto  = new Mat(watermark.length,watermark[0].length,Imgproc.THRESH_BINARY);
        double a[] = new double[] {255,255,255};
        double b[] = new double[] {0,0,0};
        for(int i = 0; i < watermark.length; i++) {
            for(int j = 0; j < watermark[0].length; j++) {
                if(watermark[i][j] == 1)
                    binaryPhoto.put(i, j, a);
                else
                    binaryPhoto.put(i, j, b);
            }
        }
        Imgcodecs.imwrite(dstPath, binaryPhoto);
    }

    /**
     * 提取每个分块
     * @return Mat
     */
    public static Mat getImageValue(Mat YMat,int x,int y) {
        Mat mat = new Mat(8,8, CvType.CV_32F);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                double[] temp = YMat.get(x * 8 + i, y * 8 + j);
                mat.put(i, j, temp);
            }
        }
        return mat;
    }
}

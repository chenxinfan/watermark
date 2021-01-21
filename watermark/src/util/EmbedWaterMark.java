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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入数字水印
 * 注意：我们嵌入的图片大小：960(行) * 1280(列)
 */
public class EmbedWaterMark {
    static{
        //加载opencv动态库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {
    }

    /**
     * 将最终的二维数组嵌入到图片当中
     * @param srcPath：图片源路径
     * @param dstPath：图片目标位置
     * @param waterMark：最终的二维数组
     * @param p：嵌入强度
     * @throws IOException
     */
    public static void addWaterMarkToImage(String srcPath, String dstPath, int[][] waterMark, double p) throws IOException {

        //在temporary_document文件中创建原图的临时文件
        String tempScrPath = "temporary_document\\temporary.bmp";
        FileInputStream in = new FileInputStream(new File(srcPath));
        FileOutputStream out = new FileOutputStream(new File(tempScrPath));
        byte[] burf = new byte[1024];
        int len = 0;
        while((len = in.read(burf)) != - 1){
            out.write(burf, 0 , len);
        }
        in.close();
        out.close();


        Mat originalMat = Imgcodecs.imread(tempScrPath);
        int rows = originalMat.rows();
        int cols = originalMat.cols();


        if(rows > cols){
            //表名图片是立起来的，然后我们将图片任意方向旋转90度
            BufferedImage dst = ImageUtil.Rotate(ImageIO.read(new FileInputStream(tempScrPath)), 90);
            String tempPath = "temporary_document\\temporary.bmp";
            ImageIO.write(dst, "bmp", new File(tempScrPath));
        }
        ImageUtil.reset(tempScrPath, tempScrPath);


        //图片大小已经处理完成变为 960(行) * 1280(列)

        originalMat = Imgcodecs.imread(tempScrPath);
        rows = originalMat.rows();
        cols = originalMat.cols();

        //得到原图的Ycbcr
        Mat Ycbcr= new Mat();
        Imgproc.cvtColor(originalMat, Ycbcr,Imgproc.COLOR_RGB2YCrCb);

        //得到Y通道
        List<Mat> allPlanes = new ArrayList<Mat>();
        Core.split(originalMat, allPlanes);
        Mat YMat = allPlanes.get(0);

        int numberJ = cols / 8;   //160
        int numberI = rows / 8;  //120

        int waterMarkX = 0;
        int waterMarkY = 0;

        for(int i=0 ; i<numberI ; i++) {
            for (int j = 0; j < numberJ; j++) {

                //获取8*8的像素块
                Mat block = getImageValue(YMat,i,j);
                //对分块进行DCT变换
                Core.dct(block, block);

                int x1 = 1, y1 = 2;
                int x2 = 2, y2 = 1;

                double[] a = block.get(x1, y1);
                double[] b = block.get(x2, y2);

                if(waterMark[waterMarkX][waterMarkY] == 1){
                    block.put(x1,y1, p);
                    block.put(x2,y2, -p);
                }else if(waterMark[waterMarkX][waterMarkY] == 0){
                    block.put(x1,y1, -p);
                    block.put(x2,y2, p);
                }

                waterMarkY++;
                if(waterMarkY == waterMark[0].length){
                    waterMarkX++;
                    waterMarkY = 0;
                }

                //对上面分块进行IDCT变换
                Core.idct(block, block);
                for(int m = 0; m < 8; m++) {
                    for(int t = 0; t < 8; t++) {
                        double[] e = block.get(m, t);
                        YMat.put(i * 8 + m,j * 8 + t, e);
                    }
                }
            }
        }

        Mat imageOut = new Mat();
        Core.merge(allPlanes, imageOut);

        Imgcodecs.imwrite(dstPath, imageOut);
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

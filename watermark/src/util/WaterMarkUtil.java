package util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 作用：
 * 1，生成，读取标志文件。
 *  标志文件：行：120，列：160
 *  对于标志文件的左上和右上角的28(行) * 48(列)为特殊值
 *  最终的标记图大概显示如下
 *  |---------------------------|
 *  |    1    |  0   |    1     |
 *  |    0       0        0     |
 *  |    0       0        0     |
 *  |---------------------------|
 *  标志文件的作用：
 *  用来解决图片任意旋转90度或者90的整数倍的情况。
 *
 * 2，文本内容生成二值图。行：64，列：64
 *
 * 3，获取二值图对应的二维矩阵。
 *
 * 4，将二维矩阵(二值图)和标志文件进行整合得到最终的嵌入图片的二维数组。
 */
public class WaterMarkUtil {
    static{
        //引入动态连接库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException {

        //生成标记文件
        //generateTagFile();

        //生成二值图
        //contextToBinaryGraphs("大碗稀饭Go", "C:\\Users\\Fenix\\Desktop\\water\\watermark.bmp");

        //得到最终的嵌入二维数组
        int[][] waterMark = getWaterMark(getArrayByBinaryGraphs("C:\\Users\\Fenix\\Desktop\\water\\watermark.bmp"), readTagFile());
        for(int i = 0; i < waterMark.length; i++){
            for(int j = 0; j < waterMark[0].length; j++){
                System.out.print(waterMark[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * 将二维矩阵(二值图)和标志文件进行整合得到最终的嵌入图片的二维数组。
     */
    public static int[][] getWaterMark(int[][] binaryArray, int[][] tagArray){
        int[][] waterMark = tagArray;
        for(int row = 0; row < binaryArray.length; row++){
            for(int col = 0; col < binaryArray[0].length; col++){
                waterMark[row + 28][col + 48] = binaryArray[row][col];
            }
        }
        return waterMark;
    }


    /**
     * 获取二值图对应的二维数组
     */
    public static int[][] getArrayByBinaryGraphs(String srcPath){
        int[][] binaryArray = new int[64][64];
        Mat mat = Imgcodecs.imread(srcPath);
        double temp[] = new double[3];
        for(int row = 0; row < 64; row++){
            for(int col = 0; col < 64; col++){
                temp = mat.get(row, col);
                if((int)temp[0] == 255){
                    binaryArray[row][col] = 1;
                }else {
                    binaryArray[row][col] = 0;
                }
            }
        }
        return binaryArray;
    }

    /**
     * 将文本内容生成二值图
     */
    public static void contextToBinaryGraphs(String context, String dstPath) throws IOException {
        File destFile = new File(dstPath);
        //创建64*64的二值图
        BufferedImage img = new BufferedImage(64,64, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = img.createGraphics();
        // 填充白色背景
        g.setColor(Color.white);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        //字体
        g.setFont(new Font("微软黑体", Font.PLAIN, 12));
        g.setPaint(new Color(103, 101, 120));
        g.drawString(context, 1, 12);
        g.drawString(context, 1, 24);
        g.drawString(context, 1, 36);
        g.drawString(context, 1, 48);
        g.drawString(context, 1, 60);
        g.dispose();
        // 输出
        ImageIO.write(img, "bmp", destFile);
    }

    /**
     * 读取标记文件
     */
    public static int[][] readTagFile() throws IOException {
        int[][] tagArray = new int[120][160];
        File tagFile = new File("waterMark.array");
        if(!tagFile.exists()){
            generateTagFile();
        }
        BufferedReader bufr = new BufferedReader(new FileReader(tagFile));
        String line = null;
        int row = 0;
        while((line = bufr.readLine()) != null) {
            for(int col = 0; col < 160; col++){
                tagArray[row][col] = line.charAt(col) - '0';
            }
            row++;
        }
        bufr.close();
        return tagArray;
    }

    /**
     * 生成标记文件
     */
    public static void generateTagFile() throws FileNotFoundException {
        //得到换行符：win和linux的换行符是不相同的，所以通过如下方式获取换行符
        String NEW_LINE = System.getProperty("line.separator");
        PrintStream out = new PrintStream(new File("waterMark.array"));

        for(int row = 1; row <= 120; row++){
            for(int col = 1; col <= 160; col++){
                if(row <= 28 && (col <= 48 || col >= 113)){
                    //左上角和右上角的特殊点
                    out.print(1);
                }else {
                    //其他区域
                    out.print(0);
                }
            }
            out.print(NEW_LINE);
        }
        out.close();
    }

}

import org.opencv.core.Core;
import util.EmbedWaterMark;
import util.ExtractWaterMark;
import util.WaterMarkUtil;

import java.io.IOException;

public class Main {
    static{
        //加载opencv动态库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws IOException {
//        embed("C:\\Users\\Fenix\\Desktop\\water\\stzz.jpg",
//                "C:\\Users\\Fenix\\Desktop\\water\\stzz-out.jpg",
//                "C:\\Users\\Fenix\\Desktop\\water\\watermark.bmp",100);

        extract("C:\\Users\\Fenix\\Desktop\\water\\stzz-out-4.jpg",
                "C:\\Users\\Fenix\\Desktop\\water\\watermark-out-4.bmp",
                "C:\\Users\\Fenix\\Desktop\\water\\watermar-4");
    }

    /**
     * 嵌入数字水印
     */
    public static void embed(String srcPath, String dstPath, String waterPath, double p) throws IOException {
        int[][] waterMark = WaterMarkUtil.getWaterMark(WaterMarkUtil.getArrayByBinaryGraphs(waterPath),
                WaterMarkUtil.readTagFile());
        EmbedWaterMark.addWaterMarkToImage(srcPath,dstPath, waterMark, p);
    }

    /**
     * 提取数字水印
     */
    public static void extract(String srcPath, String waterPath, String waterOtherPath) throws IOException {
        ExtractWaterMark.getImageWatermark(srcPath, waterPath, waterOtherPath);
    }


}

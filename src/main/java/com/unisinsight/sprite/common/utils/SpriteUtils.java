package com.unisinsight.sprite.common.utils;

import com.google.common.collect.Lists;
import com.unisinsight.framework.common.utils.CheckUtil;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小图转雪碧图
 */
@Slf4j
public class SpriteUtils {

    private static final String IMG_URL = "E:\\img\\";
    private static final String SPRITE_URL = "E:\\img\\data\\";
    private static final String SPRITE_NAME = "sprite.png";
    private static final String JSON_URL = "E:\\img\\data\\";
    private static final String JSON_NAME = "sprite.json";
    private static final int INIT_WIDTH = 255;

    public static void getImage() throws IOException {
        long startTime = System.currentTimeMillis();
        // 拿到全部图片
        File file = new File(IMG_URL);
        File[] files = file.listFiles();
        // 获得图片的参数
        List<ImageParams> imageParamsList = Lists.newArrayList();
        for (File img : files) {
            String name = img.getName();
            if (!name.endsWith(".png")) {
                continue;
            }
            BufferedImage read = ImageIO.read(new FileInputStream(img));
            int height = read.getHeight();
            int width = read.getWidth();
            ImageParams imageParams = new ImageParams();
            imageParams.setHeight(height);
            imageParams.setWidth(width);
            imageParams.setName(name.replace(".png", ""));
            log.info("【读取图片属性】，{}", imageParams);
            imageParamsList.add(imageParams);
        }
        if (CheckUtil.isNullOrEmpty(imageParamsList)) {
            return;
        }
        imageParamsList = imageParamsList.stream().sorted(
                Comparator.comparing(ImageParams::getName).thenComparing(ImageParams::getHeight).reversed())
                .collect(Collectors.toList());
        // 将图片分成行和列
        List<List<ImageParams>> row = Lists.newArrayList();
        List<ImageParams> col = Lists.newArrayList();
        int totalWidth = 0;
        for (int i = 0, j = imageParamsList.size(); i < j; i++) {
            // 是否超了宽度，超了就移动到下一行
            totalWidth += imageParamsList.get(i).getWidth();
            if (totalWidth > INIT_WIDTH) {
                i = i - 1;
                row.add(col);
                totalWidth = 0;
                col = Lists.newArrayList();
            } else {
                col.add(imageParamsList.get(i));
            }
            // 最后一张图片
            if (i == imageParamsList.size() - 1) {
                row.add(col);
            }
        }
        // 计算高度
        int totalHeight = row.stream().mapToInt(rows -> {
                    int asInt = rows.stream().mapToInt(ImageParams::getHeight).max().getAsInt();
                    return asInt;
                }
        ).sum();
        // 进行画图
        BufferedImage bufferedImage = new BufferedImage(INIT_WIDTH, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        // 背景透明
        bufferedImage = graphics.getDeviceConfiguration().createCompatibleImage(INIT_WIDTH, totalHeight, Transparency.TRANSLUCENT);
        // JSON字符串拼接
        String spritejson = "{";
        int startHeight = 0;
        for (int i = 0, j = row.size(); i < j; i++) {
            int tempWidth = 0;
            List<ImageParams> imageParams = row.get(i);
            for (int k = 0, m = imageParams.size(); k < m; k++) {
                BufferedImage read = ImageIO.read(new FileInputStream(IMG_URL + imageParams.get(k).getName() + ".png"));
                Integer height = read.getHeight();
                Integer width = read.getWidth();
                for (int n = 0; n < width; n++) {
                    for (int p = 0; p < height; p++) {
                        int rgb = read.getRGB(n, p);
                        bufferedImage.setRGB(tempWidth + n, startHeight + p, rgb);
                    }
                }
                spritejson += "\"" + row.get(i).get(k).getName() + "\":{\"x\":";
                spritejson += tempWidth + ",\"y\":" + startHeight + ",\"width\":" + row.get(i).get(k).getWidth();
                spritejson += ",\"height\":" + row.get(i).get(k).getHeight() + ",\"pixelRatio\":1,\"visible\":true},";
                tempWidth += width;
            }
            startHeight += imageParams.stream().mapToInt(ImageParams::getHeight).max().getAsInt();
        }
        spritejson += "}";
        graphics.dispose();
        // 保存图片
        ImageIO.write(bufferedImage, "PNG", new File(SPRITE_URL + SPRITE_NAME));
        // 保存JSON文件
        FileWriter fw = new FileWriter(new File(JSON_URL + JSON_NAME));
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(spritejson);
        bw.flush();
        bw.close();
        long endTime = System.currentTimeMillis();
        log.info("【sprite大图】，耗时：{}毫秒", (endTime - startTime));
    }


    public static void main(String[] args) throws IOException {
        getImage();
    }
}

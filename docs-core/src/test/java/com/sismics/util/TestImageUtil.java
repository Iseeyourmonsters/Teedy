package com.sismics.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test of the image utilities.
 *
 * @author bgamard
 */
public class TestImageUtil {

    @Test
    public void computeGravatarTest() {
        Assert.assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", ImageUtil.computeGravatar("MyEmailAddress@example.com "));
    }

    @Test
    public void computeGravatarNullTest() {
        Assert.assertNull(ImageUtil.computeGravatar(null));
    }

    @Test
    public void testIsBlackForBinaryImage() {
        // 创建一个 1x1 的黑白图像（TYPE_BYTE_BINARY）
        BufferedImage binaryImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = binaryImage.createGraphics();
        g2d.setColor(Color.BLACK);  // 设置为黑色
        g2d.fillRect(0, 0, 1, 1);
        g2d.dispose();

        Assert.assertTrue(ImageUtil.isBlack(binaryImage, 0, 0));

        // 再设置成白色像素
        g2d = binaryImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 1, 1);
        g2d.dispose();

        Assert.assertFalse(ImageUtil.isBlack(binaryImage, 0, 0));
    }

    @Test
    public void testIsBlackForRGBImage() {
        // 创建一个 1x1 的 RGB 图像
        BufferedImage rgbImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        rgbImage.setRGB(0, 0, new Color(0, 0, 0).getRGB());  // 设置为黑色
        Assert.assertTrue(ImageUtil.isBlack(rgbImage, 0, 0));

        rgbImage.setRGB(0, 0, new Color(255, 255, 255).getRGB());  // 设置为白色
        Assert.assertFalse(ImageUtil.isBlack(rgbImage, 0, 0));

        rgbImage.setRGB(0, 0, new Color(100, 100, 100).getRGB());  // 设置为灰色（luminance≈100）
        Assert.assertTrue(ImageUtil.isBlack(rgbImage, 0, 0));

        rgbImage.setRGB(0, 0, new Color(200, 200, 200).getRGB());  // 设置为亮灰色（luminance≈200）
        Assert.assertFalse(ImageUtil.isBlack(rgbImage, 0, 0));
    }
}

package com.etl.api.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public final class CaptchaUtil {

    // 验证码字符集（可按需加 A-Z）
    private static final String CHAR_SET = "0123456789";
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;
    private static final int CODE_LEN = 4;
    private static final int LINE_COUNT = 80;
    private CaptchaUtil() {
    }

    public static CaptchaResult generateCaptcha() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 字体
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) (HEIGHT * 0.7)));

        // 字体y坐标
        FontMetrics fontMetrics = g.getFontMetrics();
        int midY = (HEIGHT - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();

        // 画字符
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LEN; i++) {
            char c = CHAR_SET.charAt(random.nextInt(CHAR_SET.length()));
            code.append(c);

            g.setColor(new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
            ));

            g.drawString(String.valueOf(c), WIDTH / CODE_LEN * i, midY);
        }

        // 干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = x1 + random.nextInt(WIDTH / 8);
            int y2 = y1 + random.nextInt(HEIGHT / 8);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();

        // 转 Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            String dataUrl = "data:image/png;base64," + base64;
            return new CaptchaResult(code.toString(), dataUrl);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    public record CaptchaResult(String code, String base64Image) {
    }
}
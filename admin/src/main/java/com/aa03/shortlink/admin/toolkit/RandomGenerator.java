package com.aa03.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * 分组ID随机生成器
 */
public final class RandomGenerator {

    // 字符串包含小写字母、大写字母和数字
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBERS = "0123456789";
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBERS;
    private static SecureRandom random = new SecureRandom();


    /**
     * 生成长度为6未的随机分组ID
     *
     * @return 分组ID
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * 生成随机分组ID
     *
     * @param length 生成位数
     * @return 分组ID
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomCharIndex = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            sb.append(DATA_FOR_RANDOM_STRING.charAt(randomCharIndex));
        }
        return sb.toString();
    }
}

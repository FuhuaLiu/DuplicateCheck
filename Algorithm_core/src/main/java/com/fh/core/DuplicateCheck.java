package com.fh.core;

import com.fh.connection.EsClient;
import com.fh.entity.TextInformation;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

public abstract class DuplicateCheck {
    public static Properties props;

    static {
        props = new Properties();
        InputStream is = DuplicateCheck.class.getClassLoader().getResourceAsStream("duplicateCheck.properties");

        try {
            props.load(is);
        } catch (IOException e) {
            System.err.println("Unable to load properties file: " + e.getMessage());
        }
    }

    /**
     * 论文查重
     * @return
     */
    public abstract double duplicateCheck(String index, TextInformation tft);

    /**
     * 获取关键词命中文章
     * @param keywords
     * @return
     */
    public String[] getRelatedTextIds(String keywords) {
        return new String[]{};
    }

    /**
     *
     * @param sentence
     * @return
     */
    public List<String> getSplitWords(String sentence) {
        String index = props.getProperty("index");
        EsClient esClient = EsClient.getInstance();
        List<String> wordList = esClient.getSplitWords(sentence, index, "ik_smart");

        return wordList;
    }

    /**
     * 替换非逗号
     * @param text
     * @return
     */
    public String replaceSymbols(String text) {
        return text.replaceAll(" ", "").replaceAll("[？；！]", "。");
    }

    /**
     * 获取句子的UTF-8编码值
     * @param sentence
     * @return
     */
    public int getUTF8EncodingValue(String sentence) {
        int res = 0;
        int length = sentence.length();
        for (int i = 0; i < length; i ++) {
            String word = sentence.substring(i, i + 1);

            try {
                byte[] bytes = word.getBytes("UTF-8");
                for (int j = 0; j < bytes.length; j++) {
                    res += bytes[j] & 0xff;
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return res;
    }

    public String removeSpace(String text) {
        return text.replaceAll(" ", "");
    }
}

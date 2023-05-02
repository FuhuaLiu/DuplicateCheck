package com.fh.core.Impl;

import com.fh.connection.EsClient;
import com.fh.core.DuplicateCheck;
import com.fh.entity.TextInformation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeventhDuplicateCheck extends DuplicateCheck {
    private EsClient esClient = EsClient.getInstance();
    private String index = props.getProperty("index");

    private static final Logger LOGGER = LoggerFactory.getLogger(SeventhDuplicateCheck.class);

    @Override
    public double duplicateCheck(String index, TextInformation tft) {
        int analyzeWeight = Integer.parseInt(props.getProperty("analyzeWeight"));
        int conclusionWeight = Integer.parseInt(props.getProperty("conclusionWeight"));
        int methodWeight = Integer.parseInt(props.getProperty("methodWeight"));
        int prefaceWeight = Integer.parseInt(props.getProperty("prefaceWeight"));
        int resultWeight = Integer.parseInt(props.getProperty("resultWeight"));
        int summaryWeight = Integer.parseInt(props.getProperty("summaryWeight"));

        double analyzeCheckRate = checkPart("analyze", removeSpace(tft.getAnalyze()));
        double conclusionCheckRate = checkPart("conclusion", removeSpace(tft.getConclusion()));
        double methodCheckRate = checkPart("method", removeSpace(tft.getMethod()));
        double prefaceCheckRate = checkPart("preface", removeSpace(tft.getPreface()));
        double resultCheckRate = checkPart("result", removeSpace(tft.getResult()));
        double summaryCheckRate = checkPart("summary", removeSpace(tft.getResult()));

        if (analyzeCheckRate == -1.0) analyzeWeight = 0;
        if (conclusionCheckRate == -1.0) conclusionWeight = 0;
        if (methodCheckRate == -1.0) methodWeight = 0;
        if (prefaceCheckRate == -1.0) prefaceWeight = 0;
        if (resultCheckRate == -1.0) resultWeight = 0;
        if (summaryCheckRate == -1.0) summaryWeight = 0;

        int sumWeight = analyzeWeight + conclusionWeight + methodWeight + prefaceWeight + resultWeight + summaryWeight;

        double analyzeRate = analyzeWeight * 1.0 / sumWeight;
        double conclusionRate = conclusionWeight * 1.0 / sumWeight;
        double methodRate = methodWeight * 1.0 / sumWeight;
        double prefaceRate = prefaceWeight * 1.0 / sumWeight;
        double resultRate = resultWeight * 1.0 / sumWeight;
        double summaryRate = summaryWeight * 1.0 / sumWeight;

        System.out.println("摘要模块权重为：" + summaryWeight + " 查重率：" + summaryCheckRate);
        System.out.println("引言模块权重为：" + prefaceWeight + " 查重率：" + prefaceCheckRate);
        System.out.println("方法模块权重为：" + methodWeight + " 查重率：" + methodCheckRate);
        System.out.println("分析模块权重为：" + analyzeWeight + " 查重率：" + analyzeCheckRate);
        System.out.println("结果模块权重为：" + resultWeight + " 查重率：" + resultCheckRate);
        System.out.println("结论模块权重为：" + conclusionWeight + " 查重率：" + conclusionCheckRate);

        double finalCheckRate = (analyzeCheckRate * analyzeRate + conclusionCheckRate * conclusionRate + methodCheckRate * methodRate + prefaceCheckRate * prefaceRate + resultCheckRate * resultRate + summaryCheckRate * summaryRate) * 100;
        return finalCheckRate;
    }

    public double checkPart(String part, String text) {
        int singleRepetition = Integer.parseInt(props.getProperty("singleRepetition"));
        int samePartRecognition = 10 - Integer.parseInt(props.getProperty("samePartRecognition"));
        int differentPartRecognition = 10 - Integer.parseInt(props.getProperty("differentPartRecognition"));
        double acceptHitRate = Integer.parseInt(props.getProperty("acceptHitRate")) * 1.0 / 10;


        if (text == null || text.length() == 0 || text.equals("无")) {
            LOGGER.debug(part + "部分无内容");
            return -1.0;
        }

        text = replaceSymbols(text);

        String[] sourceSentences = Arrays.stream(text.split("。")).filter(s -> s.length() > 0).toArray(String[]::new);
        LOGGER.debug("正在进行"+ part + "部分的查重，共有" + sourceSentences.length + "句");
        System.out.println("正在进行"+ part + "部分的查重，共有" + sourceSentences.length + "句");
        getPartInfo(part, "正在进行"+ part + "部分的查重，共有" + sourceSentences.length + "句");
        getPartInfo(part, "");

        double[] finalData = new double[sourceSentences.length];
        getPartInfo(part, "");

        for (int i = 0; i < sourceSentences.length; i++) {
            Set<String> keepSingle = new HashSet<>();
            LOGGER.debug("正在进行第" + i + "句的分析");

            LOGGER.debug("第" + i + "句为：" + sourceSentences[i]);
            if (sourceSentences == null || sourceSentences.length == 0) {
                continue;
            }

            List<String> splitWords = getSplitWords(sourceSentences[i]);
            System.out.println("第" + i + "句："  + sourceSentences[i]);
            System.out.println("第" + i + "句分词情况如下：" + splitWords.toString());

            LOGGER.debug("第" + i + "句中的关键词如下");
            LOGGER.debug(splitWords.toString());

            int count = splitWords.size();
            LOGGER.debug("以上共有" + count + "个关键词");
            getPartInfo(part, i + ":  " + sourceSentences[i]);
            getPartInfo(part, "共有" + count + "个关键词");
            getPartInfo(part, "关键词：" + splitWords);

            int[][] wordRepetitionArr = new int[count][1];
            for (int j = 0; j < count; j++) {
                String word = splitWords.get(j);
                LOGGER.debug("第" + j + "个关键词为：" + word);

                String[] relatedSentences = esClient.searchHitSentenceInPart(index, part, word);
                String[] unrelatedSentences = esClient.searchHitSentenceBeyondPart(index, part, word);

                for (int k = 0; k < relatedSentences.length; k++) {
                    getSentencesSituation(relatedSentences[k]);
                }
                for (int k = 0; k < unrelatedSentences.length; k++) {
                    getSentencesSituation(unrelatedSentences[k]);
                }

                LOGGER.debug("同部分命中句子有" + relatedSentences.length + "句，如下：");
                keywordDuplicationCheck(part, singleRepetition, samePartRecognition, sourceSentences, i, splitWords, count, wordRepetitionArr, j, relatedSentences, acceptHitRate, keepSingle);

                LOGGER.debug("不同部分命中句子有" + unrelatedSentences.length + "句，如下：");
                keywordDuplicationCheck(part, singleRepetition, differentPartRecognition, sourceSentences, i, splitWords, count, wordRepetitionArr, j, unrelatedSentences, acceptHitRate, keepSingle);
            }

            int sourceSentenceCheckRate = 0;
            for (int j = 0; j < count; j++) {
                sourceSentenceCheckRate = Math.max(sourceSentenceCheckRate, wordRepetitionArr[j][0]);
            }

            finalData[i] = sourceSentenceCheckRate * 1.0 / 100;
//            LOGGER.debug("第" + i + "句的查重率：" + sourceSentenceCheckRate + "%");


            System.out.println("第" + i + "句查重率为：" + Math.round(finalData[i] * 100) + "%");

            getCheckRate("第" + i + "句：" + sourceSentences[i]);
            getCheckRate("第" + i + "句的查重率：" + sourceSentenceCheckRate + "%");
            getCheckRate("");
        }

        double thisPartCheckRate = 0.0;
        if (sourceSentences == null || sourceSentences.length == 0) {
            return -1.0;
        }

        for (int i = 0; i < sourceSentences.length; i++) {
            thisPartCheckRate += sourceSentences[i].length() * 1.0 * finalData[i]/ text.length();
        }

        return thisPartCheckRate;
    }

    /**
     * 单词查重
     * @param singleRepetition
     * @param PartRecognition
     * @param sourceSentences
     * @param i
     * @param splitWords
     * @param count
     * @param wordRepetitionArr
     * @param j
     * @param sentences
     */
    private void keywordDuplicationCheck(String part, int singleRepetition, int PartRecognition, String[] sourceSentences, int i, List<String> splitWords, int count, int[][] wordRepetitionArr, int j, String[] sentences, double acceptHitRate, Set<String> keepSingle) {
        for (int k = 0; k < sentences.length; k++) {
            if (keepSingle.contains(sentences[k])) {
                continue;
            } else {
                keepSingle.add(sentences[k]);
            }

            LOGGER.debug("-------------------------------------------------------------------------------");
            LOGGER.debug("第" + k + "句：" + sentences[k]);
            LOGGER.debug("所被查重句子：" + sourceSentences[i]);
            int hitCount = 0;
            for (int c = 0; c < count; c++) {
                if (sentences[k].contains(splitWords.get(c))) {
                    hitCount++;
                }
            }

            double hitRate = hitCount * 1.0 / count;
            if (hitRate < acceptHitRate) {
                LOGGER.debug("该句关键词命中率过低，可接受在查重范围外");
                return;
            }

            int sourceVal = getUTF8EncodingValue(sourceSentences[i]);
            int targetVal = getUTF8EncodingValue(sentences[k]);
            int sourceWordCount = sourceSentences[i].length();
            int targetWordCount = sentences[k].length();

            LOGGER.debug("第" + k + "句命中关键词个数：" + hitCount);

            double encodingRatio = sourceVal > targetVal ? targetVal * 1.0 / sourceVal : sourceVal * 1.0 / targetVal;
            double wordRatio = sourceWordCount > targetWordCount ? targetWordCount * 1.0 / sourceWordCount : sourceWordCount * 1.0 / targetWordCount;
            double rate = (hitRate * encodingRatio * wordRatio * 10 * PartRecognition);
            int duplicationRate = (int) Math.round(rate);
            LOGGER.debug("第" + k + "句命中率为：" + hitRate + "%");
            LOGGER.debug("第" + k + "句查重率为：" + duplicationRate + "%");

            getPartInfo(part, "原句：" + sourceSentences[i]);
            getPartInfo(part, "比对：" + sentences[k]);
            getPartInfo(part, "查重率：" + duplicationRate);
            getPartInfo(part, "命中率：" + hitRate);
            getPartInfo(part, "编码比：" + encodingRatio);
            getPartInfo(part, "字数比：" + wordRatio);
            getPartInfo(part, "");

            if (duplicationRate >= singleRepetition) {
                LOGGER.debug("第" + k + "句存在引用或抄袭嫌疑");
                if (wordRepetitionArr[j][0] < duplicationRate) {
                    wordRepetitionArr[j][0] = duplicationRate;
                }
            } else {
                LOGGER.debug("第" + k + "句不存在引用或抄袭嫌疑");
            }
            LOGGER.debug("-------------------------------------------------------------------------------");
        }
    }

    private void getCheckRate(String s) {
        FileWriter fw;
        try {
            fw = new FileWriter("D:\\data\\IDEA_Project\\Duplicate_check\\res\\rate.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSentencesSituation(String s) {
        FileWriter fw;
        try {
            fw = new FileWriter("D:\\data\\IDEA_Project\\Duplicate_check\\res\\sentencesSituation.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getPartInfo(String part, String s) {
        FileWriter fw;
        try {
            fw = new FileWriter("D:\\data\\IDEA_Project\\Duplicate_check\\res\\" + part + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

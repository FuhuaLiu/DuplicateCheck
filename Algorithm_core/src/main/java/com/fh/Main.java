package com.fh;

import com.fh.connection.EsClient;
import com.fh.core.DuplicateCheck;
import com.fh.core.Impl.SeventhDuplicateCheck;
import com.fh.entity.TextInformation;
import com.fh.util.split.DissertationSplit;
import com.fh.util.split.NoDissertationSplit;
import com.fh.util.split.Split;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Properties props = loadConfiguration();
        Scanner sc = new Scanner(System.in);
        TextInformation tft;
        EsClient esClient;

        System.out.println("-------------   欢迎使用本论文查重系统   -------------");

        esClient = EsClient.getInstance();

        boolean online = true;
        while (online) {
            System.out.println("本系统提供以下功能，请通过输入对应数字进行相关操作：");
            System.out.println("1 录入论文到ES集群");
            System.out.println("2 进行论文查重");
            System.out.println("3 退出本系统");
            System.out.print("请输入您的选择：");

            int firstChoose = sc.nextInt();
            while (firstChoose < 1 || firstChoose > 3) {
                System.out.println("您的输入有误，请重新输入：");
                System.out.println("1 录入论文到ES集群");
                System.out.println("2 进行论文查重");
                System.out.println("3 退出本系统");
                System.out.print("请输入您的选择：");
                firstChoose = sc.nextInt();
            }

            if (firstChoose == 3) {
                online = false;
            } else {
                System.out.println("接下来您需要选择手动或自动分割所需要加载的论文，请通过输入对应数字进行相关操作：");
                System.out.println("1 自动分割");
                System.out.println("2 手动分割");
                System.out.print("请输入您的选择：");

                int secondChoose = sc.nextInt();
                String checkStr = props.getProperty("check");

                boolean check = checkStr.equals("true") ? true : false;
                if (secondChoose == 1) {
                    tft = autoSplit(check);
                } else {
                    tft = noAutoSplit(check);
                }

                String index = props.getProperty("index");
                if (firstChoose == 1) {
                    int res = insertText(index, tft);
                    if (res == 1) {
                        System.out.println();
                        System.out.println("论文录入成功");
                        System.out.println();
                    }
                } else {
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\allResult.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\info.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\rate.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\sentencesSituation.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\analyze.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\conclusion.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\method.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\preface.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\result.txt");
                    clearFile("D:\\data\\IDEA_Project\\Duplicate_check\\res\\summary.txt");

                    long startTime = System.currentTimeMillis();
                    double result = duplicateCheck(index, tft);
                    long endTime = System.currentTimeMillis();
                    long seconds = (endTime - startTime) / 1000;

                    System.out.println("查重耗时：" + seconds + "秒");
                    System.out.println("查重率为：" + result + "%");
                }
            }
        }

        System.out.println("-------------  欢迎再次使用本论文查重系统  -------------");
        esClient.close();
        System.exit(0);
    }

    /**
     * 读取系统相关配置
     * @return
     */
    public static Properties loadConfiguration() {
        Properties props = new Properties();
        InputStream is = Main.class.getClassLoader().getResourceAsStream("mainPage.properties");

        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }

    /**
     * 自动分割
     * @param check
     * @return
     */
    public static TextInformation autoSplit(boolean check) {
        Split split = new DissertationSplit();
        String[] finalSplitText = split.getSplitText(check);
        TextInformation tft = new TextInformation().generateTft(finalSplitText);
        return tft;
    }

    /**
     * 手动分割
     * @param check
     * @return
     */
    public static TextInformation noAutoSplit(boolean check) {
        Split split = new NoDissertationSplit();
        String[] finalSplitText = split.getSplitText(check);
        TextInformation tft = new TextInformation().generateTft(finalSplitText);
        return tft;
    }

    /**
     * 论文录入集群
     * @param index
     * @param tft
     * @return
     */
    public static int insertText(String index, TextInformation tft) {
        EsClient esClient = EsClient.getInstance();
        int result = esClient.textInsert(index, tft);
        return result;
    }

    /**
     * 进行查重
     * @param index
     * @param tft
     * @return
     */
    public static double duplicateCheck(String index, TextInformation tft) {
        DuplicateCheck dc = new SeventhDuplicateCheck();
        double result = dc.duplicateCheck(index, tft);

        return result;
    }

    /**
     * 文件清除
     * @param path
     */
    public static void clearFile(String path) {
        File fileToClear = new File(path);

        try {
            // 创建 PrintWriter 对象，设置为追加模式
            PrintWriter pw = new PrintWriter(new FileWriter(fileToClear, false));
            // 关闭 PrintWriter 对象，即清空文件内容
            pw.close();
        } catch (IOException e) {
            System.out.println("清空文件内容失败：" + e.getMessage());
        }
    }
}
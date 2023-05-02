package com.fh.util.split;

import com.fh.util.FileChooser;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NoDissertationSplit implements Split{
    private String filePath;

    public NoDissertationSplit() {
        filePath = new FileChooser().getFilePath();
    }

    /**
     * 获取论文种类
     * @return
     */
    public String getTextKind() {
        Scanner sc = new Scanner(System.in);

        System.out.print("请输入论文种类：");
        Map<Integer, String> map = new HashMap<Integer, String>(){{
            put(1, "研究论文");
            put(2, "综述论文");
            put(3, "评论论文");
            put(4, "传记论文");
            put(5, "报告论文");
            put(6, "应用论文");
            put(7, "评论与回复");
        }};

        String kind = map.get(Integer.parseInt(sc.nextLine()));


        return kind;
    }

    public String[] getTextTitleAndName() {
        String[] fileName = filePath.split("\\\\");
        String title = fileName[fileName.length - 1].split("_")[0];
        String temp = fileName[fileName.length - 1].split("_")[1];
        String name = temp.split("\\.")[0];

        return new String[]{title, name};
    }

    public String readText() {
        StringBuilder sb = new StringBuilder();
        Scanner sc = new Scanner(System.in);

        String line;
        while (!(line = sc.nextLine()).equals("end")) {
            sb.append(line);
        }

        return sb.toString();
    }

    @Override
    public String[] getSplitText(boolean check) {
        String kind = getTextKind();

        String title = getTextTitleAndName()[0];

        String name = getTextTitleAndName()[1];

        System.out.print("请输入摘要：");
        String summary = readText();

        System.out.print("请输入关键字：");
        String keywords = readText();

        System.out.print("请输入全文：");
        String text = readText();

        System.out.print("请输入引言：");
        String preface = readText();

        System.out.print("请输入研究方法：");
        String method = readText();

        System.out.print("请输入研究结果：");
        String result = readText();

        System.out.print("请输入研究分析：");
        String analyze = readText();

        System.out.print("请输入结论：");
        String conclusion = readText();

        System.out.print("请输入参考文献：");
        String references = readText();

        String[] res = new String[]{kind, title, name, summary, keywords, text, preface, method, result, analyze, conclusion, references};
        if (check) {
            for (String s : res) {
                System.out.println(s);
            }
        }

        return res;
    }
}

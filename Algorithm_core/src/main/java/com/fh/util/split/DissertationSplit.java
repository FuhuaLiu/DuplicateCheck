package com.fh.util.split;

import com.fh.util.FileChooser;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DissertationSplit implements Split{
    //论文文本
    private String text;

    //文本路径
    private String filePath;

    //文本分类规则
    private Map<String, String> classifyRules;

    public DissertationSplit() {
        this.text = new String();
        this.filePath = new FileChooser().getFilePath();
        this.classifyRules = getClassifyRules();
    }

    /**
     * 读取配置文件中的规则
     * @return
     */
    public Map<String, String> getClassifyRules() {
        Properties props = new Properties();
        InputStream is = DissertationSplit.class.getClassLoader().getResourceAsStream("classification.properties");

        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashMap<String, String>();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            map.put(key, value);
        }

        return map;
    }

    /**
     * 读取论文
     * @return
     */
    public int readText() {
        if (filePath == "error") {
            return 0;
        }

        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + " ");
            }

            setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        return 1;
    }

    /**
     * 输出所读取文本到控制台
     */
    public void show() {
        System.out.println(text.length());
        System.out.println(text);
    }

    /**
     * 读取文本
     * @return
     */
    public String getText() {
        if (text == null || text.length() == 0) {
            boolean check = true;
            while (check) {
                int status = readText();

                if (status == 1) {
                    check = false;
                }
            }
        }

        return text;
    }

    /**
     * 设置文本
     */
    public void setText(String content) {
        this.text = content;
    }

    /**
     * 文本预处理
     */
    public String handleText(String content) {
        return replaceCatalogWords(replaceSeparatorWord(removeUselessWords(removeDuplicateWords(addSpaceAfterPreface(removeDuplicateSpace(removeUselessSingleNumber(replaceAllBracket(content)))))))).trim();
    }

    /**
     * 分割词预处理
     * @param content
     * @return
     */
    public String addSpaceAfterPreface(String content) {
        return content.replaceAll("引言", "引言 ").replaceAll("绪论", "绪论 ");
    }

    /**
     * 去除文本中重复相隔的空格
     * @return
     */
    public String removeDuplicateSpace(String content) {
        return content.replaceAll("\\s+", " ");
    }

    /**
     * 去除文本中重复相隔的词语
     * @return
     */
    public String removeDuplicateWords(String content) {
        String[] words = content.split(" ");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i > 0 && word.equals(words[i - 1])) {
                continue;
            }

            builder.append(word);

            if (i < words.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * 去除文本赘余词语
     * @param content
     * @return
     */
    public String removeUselessWords(String content) {
        String[] words = content.split(" ");

        StringBuilder builder = new StringBuilder();

        String regex = "\\d+[\u4e00-\u9fa5]+\\d+";

        Pattern pattern = Pattern.compile(regex);

        for (int i = 0; i < words.length - 2; i++) {
            String temp = words[i] + words[i + 1] + words[i + 2];
//            System.out.println(temp);
            Matcher matcher = pattern.matcher(temp);
            boolean check1 = words[i].matches("\\d+");
            boolean check2 = words[i + 2].matches("\\d+");
            boolean check3 = words[i + 1].matches("[\u4e00-\u9fa5]+");
            boolean check4 = matcher.matches();
//            System.out.println(check1 + " " + check2 + " " + check3 + " " + check4);
            if (check1 && check2 && check3 && check4) {
                words[i + 1] = " ";
            }

            builder.append(words[i]).append(" ");
        }

        builder.append(words[words.length - 2]).append(" ").append(words[words.length - 1]);

        return removeDuplicateSpace(builder.toString());
    }

    /**
     * 小标题预处理
     * @param content
     * @return
     */
    public String replaceCatalogWords(String content) {
        Map<String, Integer> map = new HashMap<String, Integer>(){{
            put("一", 1);
            put("二", 2);
            put("三", 3);
            put("四", 4);
            put("五", 5);
            put("六", 6);
            put("七", 7);
            put("八", 8);
            put("九", 9);
        }};

        String[] words = content.split(" ");

        StringBuilder builder = new StringBuilder();

        Pattern pattern1 = Pattern.compile("第\\d+章");
        Pattern pattern2 = Pattern.compile("第[\\u4e00-\\u4e8c\\u4e09-\\u56db\\u4e94-\\u9646]章");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            Matcher matcher1 = pattern1.matcher(word);
            Matcher matcher2 = pattern2.matcher(word);

            if (matcher1.find()) {
                String matchedStr = matcher1.group();
                Pattern numPattern = Pattern.compile("\\d+");
                Matcher numMatcher = numPattern.matcher(matchedStr);
                if (numMatcher.find()) {
                    builder.append(numMatcher.group());
                }
            } else if (matcher2.find()) {
                Integer num = map.get(matcher2.group().substring(1, 2));
                builder.append(num);
            } else {
                builder.append(word);
            }

            if (i < words.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * 去除目录前无用数字
     * @param content
     * @return
     */
    public String removeUselessSingleNumber(String content) {
        String[] words = content.split(" ");

        StringBuilder builder = new StringBuilder();

        Pattern pattern = Pattern.compile("^[0-9]$");
        boolean check = true;
        for (int i = 0; i < words.length; i++) {
            Matcher matcher = pattern.matcher(words[i]);
            if (words[i].contains("目")) {
                check = false;
            }

            if (matcher.find() && check) {
                builder.append("");
            } else {
                builder.append(words[i]);
            }

            if (i < content.length() - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    /**
     * 替换部分分隔词
     * @param content
     * @return
     */
    public String replaceSeparatorWord(String content) {
        return content.replaceAll("目 录", "目录").replaceAll(" 摘 要", "摘要").replaceAll("参 考 文 献", "参考文献").replaceAll("绪  论", "绪论");
    }

    /**
     * 获取论文分割关键词
     * @param content
     * @return
     */
    public String getSplitCatalogWord(String content) {
        String[] words = content.split(" ");

        String result = "";
        boolean judge1;
//        boolean judge2 = false;
//        boolean judge3 = false;
//        int index2 = 0;
//        int index3 = 0;
        for (int i = 0; i < words.length; i++) {
            judge1 = (words[i].length() == 1 && words[i].charAt(0) >= '1' && words[i].charAt(0) <= '9');

            if (words[i].contains("绪论") || words[i].contains("引言")) {
                result = words[i];
                break;
            }

            if (judge1) {
                result = words[i + 1];
                break;
            }

//            if (words[i].contains("引言")) {
//                judge2 = true;
//            }
//
//            if (words[i].contains("绪论")) {
//                judge3 = true;
//            }
        }

//        if (result.contains("引言") || result.contains("绪论")) {
//            return result;
//        } else if (judge2 && !judge3) {
//            return
//        }

        if (result == "") {
            return "无法找到";
        }

        return result;
    }

    /**
     * 论文粗分割
     * @param content
     * @param endWord
     * @return
     */
    public String[] getThreeParts(String content, String endWord, boolean check) {
        Pattern pattern1 = Pattern.compile(endWord);
        Matcher findMatcher1 = pattern1.matcher(content);

        int number = 0;
        while (findMatcher1.find()) {
            number++;
            if (number == 2) {
                break;
            }
        }

        int end = findMatcher1.start();
        int start1 = content.indexOf("目录");
        int start2 = content.indexOf("目 录");
        int start;
        if (start1 > 0 && start2 > 0) {
            start = start1 > start2 ? start2 : start1;
        } else {
            start = start1 > 0 ? start1 : start2;
        }

        if (check) {
            System.out.println(content);
            System.out.println("论文粗分割的坐标：");
            System.out.println("开始坐标：" + start + "  结束坐标：" + end);
            System.out.println("----------------------------------");
        }

        String firstPart = content.substring(0, start);
        String secondPart = content.substring(start, end);
        String thirdPart = " 1 " + content.substring(end, content.length());

        return new String[]{firstPart, secondPart, thirdPart};
    }

    /**
     * 提取第一部分信息
     * @param firstPart
     * @return 标题、作者、摘要、关键字
     */
    public String[] getFirstPartInfo(String firstPart, boolean check) {
        String[] fileName = filePath.split("\\\\");
        String title = fileName[fileName.length - 1].split("_")[0];
        String temp = fileName[fileName.length - 1].split("_")[1];
        String name = temp.split("\\.")[0];

        int summaryIndex;
        int start1 = firstPart.indexOf("摘要");
        int start2 = firstPart.indexOf("摘 要");
        if (start1 > 0 && start2 > 0) {
            summaryIndex = start1 > start2 ? start2 : start1;
        } else {
            summaryIndex = start1 > 0 ? start1 : start2;
        }



//        int keywordsIndex = firstPart.indexOf("关键词");
        int endIndex = firstPart.toUpperCase().indexOf("ABSTRACT");
        int keywordsIndex = firstPart.lastIndexOf("关键词", endIndex);

        if (check) {
            System.out.println("文章第一部分：");
            System.out.println("摘要索引：" + summaryIndex);
            System.out.println("关键词索引：" + keywordsIndex);
            System.out.println("结束索引：" + endIndex);
            System.out.println("------------------------------------------");
        }

        String summary = removeUselessStart(firstPart.substring(summaryIndex, keywordsIndex));
        String keywords = replaceAllStrangeSymbols(firstPart.substring(keywordsIndex + 3, endIndex));

        return new String[]{title, name, summary, keywords};
    }

    /**
     * 去除无用前文
     * @param s
     * @return
     */
    public String removeUselessStart(String s) {
        String[] sentences = s.split(" ");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences.length; i++) {
            if (sentences[i].equals("摘要") || sentences[i].length() <= 1) {
                sentences[i] = "";
            }

            sb.append(sentences[i]);
        }

        return sb.toString().trim();
    }

    /**
     * 替换奇怪的符号
     * @param s
     * @return
     */
    public String replaceAllStrangeSymbols(String s) {
//        return s.replaceAll("[^\u4E00-\u9FA5]", " ").trim();
        return s.replaceAll("[^a-zA-Z\\u4e00-\\u9fa5]+", " ").trim();
    }

    /**
     * 去除多余的括号
     * @param s
     * @return
     */
    public String replaceAllBracket(String s) {
        return s.replaceAll("\\(", " ").replaceAll("\\)", " ");
    }

    /**
     * 提取第二部分信息
     * @param secondPart
     * @return
     */
    public String[] getSecondPartInfo(String secondPart) {
        List<String> list = new LinkedList<String>();

        Pattern pattern = Pattern.compile(" [1-9]\\s?[\\u4e00-\\u9fa5]+");
        Matcher matcher = pattern.matcher(secondPart);
//        while (matcher.find()) {
//            list.add(matcher.group());
//        }

        while (matcher.find()) {
            int start = matcher.start();
            int end = secondPart.indexOf(" ", start + 3);
            String smallTitle = secondPart.substring(start, end);
            if (smallTitle.contains(".")) {
                int index = smallTitle.indexOf(".");
                smallTitle = smallTitle.substring(0, index);
            }

//            Pattern seqPattern = Pattern.compile("\\d+");
//            Matcher seqMatcher = seqPattern.matcher(smallTitle);
//
//            int num = Integer.parseInt(seqMatcher.group());

            list.add(smallTitle);
//            if (num >= sequence) {
//
//                sequence = num;
//            }
        }

        int sequence = 0;
        for (String s : list) {
            Pattern seqPattern = Pattern.compile("\\d+");
            Matcher seqMatcher = seqPattern.matcher(s);

            if (seqMatcher.find()) {
                int num = Integer.parseInt(seqMatcher.group());
                if (num >= sequence) {
                    sequence = num;
                } else {
                    list.remove(s);
                }
            }
        }

        boolean check = true;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains("参考文献")) {
                check = false;
            }
        }

        if (check) {
            list.add("参考文献");
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * 提取第三部分信息
     * @param thirdPart
     * @param secondParts
     * @return
     */
    public String[] getThirdPartInfo(String thirdPart, String[] secondParts, boolean check) {
        String toUpperText = thirdPart.toUpperCase();

        List<String> list = new LinkedList<String>();

        int length = secondParts.length;
        int begin = 0, end = 0;
        for (int i = 0; i < length; i++) {

            if (i == length - 1) {
                begin = toUpperText.lastIndexOf(secondParts[i].toUpperCase());
                end = toUpperText.length();
            } else {
                begin = toUpperText.indexOf(secondParts[i].toUpperCase());
                end = toUpperText.lastIndexOf(secondParts[i + 1].toUpperCase());
            }

            String data = thirdPart.substring(begin, end);
            if (check) {
                System.out.println("开始：" + begin + "  结束：" + end);
                System.out.println("开始词语：" + secondParts[i] + "  结束词语：" + ((i + 1) < length ? secondParts[i + 1] : "无"));
                System.out.println(data);
                System.out.println("~~~~~~~~~~~~~~~~~~~");
            }

            list.add(data);
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * 文章拆分
     * @param check
     * @return
     */
    public String[][] splitText(boolean check) {
        int status = readText();

        if (status == 0) {
            return new String[][]{{"你读取的文本有误！！！"}};
        }

        String handledText = handleText(getText());

        if (check) {
            System.out.println("以下为处理过的文本：");
            System.out.println(handledText);
            System.out.println("--------------------------------------------------");
        }

        String splitCatalogWord = getSplitCatalogWord(handledText);
        if (check) {
            System.out.println("<分割词>" + splitCatalogWord + "<分割词>");
            System.out.println("--------------------------------------------------");
        }

        String[] threeParts = getThreeParts(handledText, splitCatalogWord, check);
        if (check) {
            System.out.println("以下为粗分割三部分：");
            for (String s : threeParts) {
                System.out.println();
                System.out.println(s);
                System.out.println();
            }
            System.out.println("--------------------------------------------------");
        }

        String[] firstParts = getFirstPartInfo(threeParts[0], check);
        if (check) {
            System.out.println("第一部分信息：");
            for (String s : firstParts) {
                System.out.println();
                System.out.println(s);
                System.out.println();
            }
            System.out.println("--------------------------------------------------");

        }

        String[] secondParts = getSecondPartInfo(threeParts[1]);
        if (check) {
            System.out.println("第二部分信息：");
            for (String s : secondParts) {
                System.out.println();
                System.out.println(s);
                System.out.println();
            }
            System.out.println("--------------------------------------------------");
        }

        String[] thirdParts = getThirdPartInfo(threeParts[2], secondParts, check);
        if (check) {
            System.out.println("第三部分信息：");
            for (String s : thirdParts) {
                System.out.println();
                System.out.println(s);
                System.out.println();
            }
            System.out.println("--------------------------------------------------");
        }

        String[][] res = new String[3][20];
        res[0] = firstParts;

        res[1] = secondParts;

        res[2] = thirdParts;

        return res;
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

    /**
     * 获取论文所需模块
     * @param splitParts
     * @return
     */
    public String getMatchContent(String need, String[] splitParts, String[] splitContents, boolean[] chosen) {
        String[] rules = classifyRules.get(need).split("/");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splitParts.length; i++) {
            for (int j = 0; j < rules.length; j++) {
                if (splitParts[i].contains(rules[j]) && !chosen[i]) {
                    chosen[i] = true;
                    sb.append(splitContents[i]);
                    break;
                }
            }
        }

        if (sb.length() == 0) {
            sb.append("");
        }

        return sb.toString();
    }

    /**
     * 最后划分处理
     * @param splitText
     * @param check
     * @return
     */
    public String[] completeInformation(String[][] splitText, boolean check){
        String kind = getTextKind();

        String title = splitText[0][0];

        String name = splitText[0][1];

        String summary = splitText[0][2];

        String keywords = splitText[0][3];

        String text = getText();

        boolean[] chosen = new boolean[splitText[1].length];

        String preface = getMatchContent("preface", splitText[1], splitText[2], chosen);

        String method = getMatchContent("method", splitText[1], splitText[2], chosen);

        String result = getMatchContent("result", splitText[1], splitText[2], chosen);

        String analyze = getMatchContent("analyze", splitText[1], splitText[2], chosen);

        String conclusion = getMatchContent("conclusion", splitText[1], splitText[2], chosen);

        String references = getMatchContent("references", splitText[1], splitText[2], chosen);

        String[] res = new String[]{kind, title, name, summary, keywords, text, preface, method, result, analyze, conclusion, references};

        if (check) {
            System.out.println("最终信息：");
            for (String s : res) {
                System.out.println();
                System.out.println(s);
            }
        }

        return res;
    }

    @Override
    public String[] getSplitText(boolean check) {
        String[][] splitText = splitText(check);
        String[] finalSplitText = completeInformation(splitText, check);

        return finalSplitText;
    }
}

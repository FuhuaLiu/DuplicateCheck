package com.fh.entity;

public class TextInformation {
    //论文种类
    private String kind;

    //题目
    private String title;

    //作者
    private String author;

    //摘要
    private String summary;

    //关键字
    private String keywords;

    //全文
    private String text;

    //引言
    private String preface;

    //方法、设计
    private String method;

    //结果
    private String result;

    //分析、讨论
    private String analyze;

    //结论、总结
    private String conclusion;

    //参考文献
    private String references;

    public TextInformation generateTft(String[] textInformation) {
        TextInformation tft = new TextInformation();

        tft.setKind(textInformation[0]);
        tft.setTitle(textInformation[1]);
        tft.setAuthor(textInformation[2]);
        tft.setSummary(textInformation[3]);
        tft.setKeywords(textInformation[4]);
        tft.setText(textInformation[5]);
        tft.setPreface(textInformation[6]);
        tft.setMethod(textInformation[7]);
        tft.setResult(textInformation[8]);
        tft.setAnalyze(textInformation[9]);
        tft.setConclusion(textInformation[10]);
        tft.setReferences(textInformation[11]);

        return tft;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPreface() {
        return preface;
    }

    public void setPreface(String preface) {
        this.preface = preface;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAnalyze() {
        return analyze;
    }

    public void setAnalyze(String analyze) {
        this.analyze = analyze;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return "TextInformation{" +
                "kind='" + kind + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", summary='" + summary + '\'' +
                ", keywords='" + keywords + '\'' +
                ", text='" + text + '\'' +
                ", preface='" + preface + '\'' +
                ", method='" + method + '\'' +
                ", result='" + result + '\'' +
                ", analyze='" + analyze + '\'' +
                ", conclusion='" + conclusion + '\'' +
                ", references='" + references + '\'' +
                '}';
    }
}

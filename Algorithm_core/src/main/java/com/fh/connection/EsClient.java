package com.fh.connection;

import com.fh.entity.TextInformation;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EsClient {
    private static volatile EsClient esClient = null;

    private RestHighLevelClient restHighLevelClient;

    private static HttpHost[] connectHosts;

    private static String connectPort;

    static {
        readConfig();
    }

    public EsClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 获取客户端连接
     * @return
     */
    public static EsClient getInstance() {
        if (esClient == null) {
            synchronized (EsClient.class) {
                if (esClient == null) {
                    esClient = new EsClient(new RestHighLevelClient(RestClient.builder(connectHosts)));
                }
            }
        }

        return esClient;
    }

    /**
     * 断开客户端连接
     * @return
     */
    public int close() {
        try {
            restHighLevelClient.close();
        } catch (IOException e) {
            System.exit(0);
        }

        esClient = null;
        return 1;
    }

    /**
     * 读取配置文件
     */
    private static void readConfig() {
        Properties props = new Properties();
        InputStream is = EsClient.class.getClassLoader().getResourceAsStream("connection.properties");

        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String allHost = props.getProperty("ES.IpAddresses");
        String port = props.getProperty("ES.Port");

        connectPort = port;

        String[] hosts = allHost.split(";");
        HttpHost[] splitHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            splitHosts[i] = new HttpHost(hosts[i], Integer.parseInt(connectPort), "http");
        }

        connectHosts = splitHosts;
    }

    /**
     * 文章导入
     * @return
     */
    public int textInsert(String index, TextInformation tft) {
        JSONObject object = generateJSONObject(tft);

        IndexRequest request = new IndexRequest(index).source(object.toString(), XContentType.JSON);

        IndexResponse response;
        try {
            response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        System.out.println(response.getIndex());
        System.out.println(response.getId());
        System.out.println(response.getResult());

        if (response.getResult() == DocWriteResponse.Result.UPDATED) {
            return 2;
        } else {
            return 1;
        }
    }

    public void searchKeyWords(String index, String keywords) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("keywords", keywords));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("出现错误");
            throw new RuntimeException(e);
        }

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        System.out.println("搜索结果有" + searchHits.length + "个");

        for (SearchHit hit : searchHits) {
            System.out.println("---------------------------");

            Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 获取文档内容
            // 处理查询结果
            for (String s : sourceAsMap.keySet()) {
                System.out.println(s + " " + sourceAsMap.get(s));
            }


        }

    }

    /**
     * 获得关联部分文本
     * @param index
     * @param part
     * @param word
     * @return
     */
    public String[] searchHitSentenceInPart(String index, String part, String word){
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] excludeFields = new String[]{"text"};

        searchSourceBuilder.query(QueryBuilders.matchQuery(part, word)).fetchSource(null, excludeFields);
        searchRequest.source(searchSourceBuilder);

        String[] needParts = new String[]{part};
        return searchHitSentence(searchRequest, word, needParts);
    }

    /**
     * 获得非关联部分文本
     * @param index
     * @param part
     * @param word
     * @return
     */
    public String[] searchHitSentenceBeyondPart(String index, String part, String word){
        List<String> parts = new ArrayList<String>(Arrays.asList("analyze", "conclusion", "method", "preface", "result", "summary"));

        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] excludeFields = new String[]{"text"};

        parts.remove(part);
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(word, parts.get(0), parts.get(1),parts.get(2),parts.get(3),parts.get(4))).fetchSource(null, excludeFields);
        searchRequest.source(searchSourceBuilder);

        String[] needParts = new String[]{parts.get(0), parts.get(1), parts.get(2), parts.get(3), parts.get(4)};
        return searchHitSentence(searchRequest, word, needParts);
    }

    /**
     * 获得搜索结果
     * @param searchRequest
     * @return
     */
    public String[] searchHitSentence(SearchRequest searchRequest, String word, String[] needParts) {
        SearchResponse searchResponse;
        SearchHits hits = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            hits = searchResponse.getHits();
        } catch (IOException e) {
            System.out.println("查询失败: " + e.getMessage());
        }

        String text = "";

        if (hits == null) {
            return new String[]{};
        } else {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = new JSONObject(sourceAsString);

                for (String s : needParts) {
                    String content = jsonObject.getString(s);
//                    System.out.println(s + "     " + content);
                    text += content;
                }
            }

        }

        text = text.replaceAll(" ", "").replaceAll("[？；！]", "。");

        String[] res = Arrays.stream(text.split("。"))
                .filter(s -> s.contains(word) && s.length() > 0)
                .toArray(String[]::new);

        return res;
    }

    /**
     * 分词请求
     * @param sentence
     * @param index
     * @param method
     * @return
     */
    public List<String> getSplitWords(String sentence, String index, String method) {
        List<String> list = new ArrayList<>();

        AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(index, method, sentence);
        AnalyzeResponse response;
        try {
            response = restHighLevelClient.indices().analyze(request, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
            Set<String> set = new HashSet<>();
            for (AnalyzeResponse.AnalyzeToken token : tokens) {
                String word = token.getTerm();
                if (word.length() >= 2 && word.length() <= 4 && !set.contains(word)) {
                    set.add(word);
                    list.add(word);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public JSONObject generateJSONObject(TextInformation tft) {
        JSONObject object = new JSONObject();
        object.put("kind", tft.getKind());
        object.put("title", tft.getTitle());
        object.put("author", tft.getAuthor());
        object.put("summary", tft.getSummary());
        object.put("keywords", tft.getKeywords());
        object.put("text", tft.getText());
        object.put("preface", tft.getPreface());
        object.put("method", tft.getMethod());
        object.put("result", tft.getResult());
        object.put("analyze", tft.getAnalyze());
        object.put("conclusion", tft.getConclusion());
        object.put("references", tft.getReferences());

        return object;
    }

    public HttpHost[] getConnectHosts() {
        return connectHosts;
    }

    public void setConnectHosts(HttpHost[] connectHosts) {
        this.connectHosts = connectHosts;
    }

    public String getConnectPort() {
        return connectPort;
    }

    public void setConnectPort(String connectPort) {
        this.connectPort = connectPort;
    }
}

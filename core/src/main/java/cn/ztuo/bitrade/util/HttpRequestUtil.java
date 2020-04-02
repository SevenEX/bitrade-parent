package cn.ztuo.bitrade.util;


import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @ClassName: HttpRequestUtil
 * @Description: Http请求工具类
 */
public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static final String URL_PARAM_CONNECT_FLAG = "&";

    private static final String EMPTY = "";

    private static PoolingHttpClientConnectionManager connectionManager = null;

    private static int connectionTimeOut = 15000;

    private static int socketTimeOut = 15000;

    private static int maxConnectionPerHost = 500;

    private static int maxTotalConnections = 500;

    private static CloseableHttpClient client;

    static {
        connectionManager = new PoolingHttpClientConnectionManager();
//        connectionManager.getParams().setConnectionTimeout(connectionTimeOut);
//        connectionManager.getParams().setSoTimeout(socketTimeOut);
        connectionManager.setDefaultMaxPerRoute(maxConnectionPerHost);
        connectionManager.setMaxTotal(maxTotalConnections);
        client = HttpClients.createMinimal(connectionManager);
    }
    private static void config(HttpRequestBase httpRequestBase) {
        // 设置Header等
        // httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        // httpRequestBase
        // .setHeader("Accept",
        // "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpRequestBase.setHeader("Accept-Language",
        // "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        // httpRequestBase.setHeader("Accept-Charset",
        // "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");
        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionTimeOut)
                .setConnectTimeout(connectionTimeOut).setSocketTimeout(socketTimeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }
    /**
     * POST方式表单提交数据
     *
     * @param url
     * @param busiParams 业务参数
     * @param headParams 请求头参数
     */
    public static String URLPost(String url, Map<String, Object> busiParams, Map<String, Object> headParams) {
        String response = EMPTY;
        try {
            response = URLPostForException(url, busiParams, headParams);
        } catch (IOException e) {
            logger.error("发生网络异常", e);
        }
        return response;
    }

    private static void addFormParam(Map<String, Object> busiParams, HttpPost postMethod) throws UnsupportedEncodingException {
        // 将表单的值放入postMethod中
        Set<String> keySet = busiParams.keySet();
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String key : keySet) {
            Object value = busiParams.get(key);
            nvps.add(new BasicNameValuePair(key, String.valueOf(value)));
        }
        postMethod.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
    }

    public static String URLPostForException(String url, Map<String, Object> busiParams, Map<String, Object>
            headParams) throws IOException{
        String response = EMPTY;
        HttpPost postMethod = null;
        try {
            postMethod = new HttpPost(url);
            addHeader(headParams, postMethod);
            // 将表单的值放入postMethod中
            addFormParam(busiParams, postMethod);
            // 执行postMethod
            CloseableHttpResponse httpResponse = client.execute(postMethod);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                response = EntityUtils.toString(httpResponse.getEntity());
            } else {
                logger.error("响应状态码=" + statusCode);
            }
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return response;
    }
    /**
     * POST方式提交数据 json
     *
     * @param url
     * @param busiParams 业务参数
     * @param headParams 请求头参数
     */
    public static String URLPostJSONParams(String url, String busiParams, Map<String, Object> headParams) {
        logger.info("URLPostJSONParams>>>url>>>" + url + ">>>busiParams>>>" + busiParams + ">>>headParams>>>" +
				headParams);
        String response = EMPTY;
        try {
            response = URLPostJSONParamsForException(url, busiParams, headParams);
        } catch (IOException e) {
            logger.error("发生网络异常", e);
        }
        return response;
    }

    private static void addHeader(Map<String, Object> headParams, HttpPost postMethod) {
        // 将表单的head放入postMethod中
        if (headParams != null && !headParams.isEmpty()) {
            Set<String> headKeySet = headParams.keySet();
            for (String key : headKeySet) {
                Object value = headParams.get(key);
                postMethod.addHeader(key, String.valueOf(value));
            }
        }
    }


    public static String URLPostJSONParamsForException(String url, String busiParams, Map<String, Object> headParams)
            throws IOException {
        logger.info("URLPostJSONParams>>>url>>>" + url + ">>>busiParams>>>" + busiParams + ">>>headParams>>>" +
				headParams);
        String response = EMPTY;
        HttpPost postMethod = null;
        try {
            postMethod = new HttpPost(url);
            // 将表单的head放入postMethod中
            addHeader(headParams, postMethod);
            postMethod.setEntity(new StringEntity(busiParams, ContentType.APPLICATION_JSON));
            // 执行postMethod
            CloseableHttpResponse httpResponse = client.execute(postMethod);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                response = EntityUtils.toString(httpResponse.getEntity());
            } else {
                logger.error("响应状态码=" + statusCode);
            }
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return response;
    }

    /**
     * GET方式提交数据
     *
     * @param url
     * @param params
     * @param enc
     */
    public static String URLGet(String url, Map<String, String> params, String enc) {
        String response = EMPTY;
        try {
            response = URLGetForException(url, params, enc);
        } catch (IOException e) {
            logger.error("发生网络异常", e);
        }
        return response;
    }


    public static String URLGetForException(String url, Map<String, String> params, String enc) throws IOException {
        String response = EMPTY;
        HttpGet getMethod = null;
        StringBuffer strtTotalURL = new StringBuffer(EMPTY);
        if (strtTotalURL.indexOf("?") == -1) {
            strtTotalURL.append(url).append("?").append(getUrl(params, enc));
        } else {
            strtTotalURL.append(url).append("&").append(getUrl(params, enc));
        }
        logger.info("GET请求URL = \n" + strtTotalURL.toString());
        try {
            getMethod = new HttpGet(strtTotalURL.toString());
            getMethod.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + enc);
            // 执行getMethod
            CloseableHttpResponse httpResponse = client.execute(getMethod);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                response = EntityUtils.toString(httpResponse.getEntity());
            } else {
                logger.error("响应状态码=" + statusCode);
            }
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return response;
    }

    /**
     * 据Map生成URL字符串
     *
     * @param map
     * @param valueEnc
     */
    private static String getUrl(Map<String, String> map, String valueEnc) {
        if (null == map || map.keySet().size() == 0) {
            return (EMPTY);
        }
        StringBuffer url = new StringBuffer();
        Set<String> keys = map.keySet();
        for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
            String key = it.next();
            if (map.containsKey(key)) {
                String val = map.get(key);
                String str = val != null ? val : EMPTY;
                try {
                    str = URLEncoder.encode(str, valueEnc);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                url.append(key).append("=").append(str).append(URL_PARAM_CONNECT_FLAG);
            }
        }
        String strURL = EMPTY;
        strURL = url.toString();
        if (URL_PARAM_CONNECT_FLAG.equals(EMPTY + strURL.charAt(strURL.length() - 1))) {
            strURL = strURL.substring(0, strURL.length() - 1);
        }
        return (strURL);
    }
}

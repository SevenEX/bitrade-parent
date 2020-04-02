package cn.ztuo.bitrade.es;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.config.ESConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class ESClient {

    @Autowired
    private ESConfig esConfig;

    public JSONObject getClient(String method, String endPoint, JSONObject params){

        log.info("=====method:"+method+"<>====endPoint:"+endPoint+"<>===params+"+params);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(esConfig.getEsUsername(), esConfig.getEsPassword()));
            RestClient restClient = RestClient.builder(new HttpHost(esConfig.getPrivateNet(), esConfig.getEsPort()))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                }).build();
        try {
            HttpEntity httpEntity = new NStringEntity(params.toJSONString(), ContentType.APPLICATION_JSON);

            Request request = new Request(method, endPoint);
            request.setEntity(httpEntity);
            request.addParameter("pretty", "true");
            Response response = restClient.performRequest(request);
            log.info("======response:"+response);

            int statusCode = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            log.info("=====result:"+result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            restClient.close();
            if (200 == statusCode || 201 == statusCode){
                return jsonObject;
            }else {
                log.info("es client 调用失败"+response);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("es client 调用失败={}",e);
        }finally {
            try {
                restClient.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.info("es client 调用失败={}",e);
            }
        }
        return null;
    }
}

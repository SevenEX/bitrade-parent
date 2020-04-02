package cn.ztuo.bitrade.util;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author tansitao
 * @time 2018-04-05
 * http短信接口访问工具
 */
public class HttpSend {

	
	
	/**
     * 基于HttpClient 4.3的通用POST方法
     *
     * @param url       提交的URL
     * @param paramsMap 提交<参数，值>Map
     * @return 提交响应
     */

	public static String yunpianPost(String url, Map<String, String> paramsMap) {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost method = new HttpPost(url);
			if (paramsMap != null) {
				ArrayList<NameValuePair> namePairs = new ArrayList<>(paramsMap.size());
				int i = 0;
				for (Map.Entry<String, String> param : paramsMap.entrySet()) {
					BasicNameValuePair pair = new BasicNameValuePair(param.getKey(),
							param.getValue());
					namePairs.add(pair);
				}
				HttpEntity entity = new UrlEncodedFormEntity(namePairs, "UTF-8");
				method.setEntity(entity);
			}
			CloseableHttpResponse httpResponse = client.execute(method);
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}

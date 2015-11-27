package com.papple.framework.util;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {

	private static final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

	private static CloseableHttpClient httpClient;

	public static synchronized CloseableHttpClient getHttpClient() {
		if (httpClient == null) {
			ConnectionConfig connectionConfig = ConnectionConfig.custom()
					.setCharset(Consts.UTF_8).build();

			cm.setMaxTotal(200); // 连接池里的最大连接数
			cm.setDefaultMaxPerRoute(50); // 每个路由的默认最大连接数
			cm.setDefaultMaxPerRoute(50);
			cm.setDefaultConnectionConfig(connectionConfig);
			httpClient = HttpClients.custom().setConnectionManager(cm).build();
		}
		return httpClient;
	}

	public static String doPostJson(String url, String postData) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPost httpPost = new HttpPost(url);

		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(5000).setConnectTimeout(5000).build();
		httpPost.setConfig(requestConfig);

		httpPost.setEntity(new StringEntity(postData,
				ContentType.APPLICATION_JSON));
		CloseableHttpResponse response = null;
		String content = null;
		try {
			response = httpClient.execute(httpPost);
			content = EntityUtils.toString(response.getEntity(), "UTF-8");
		} finally {
			response.close();
		}
		return content;
	}

	
	public static String doRequest(String urlWithParams) throws IOException {
        HttpGet httpget = new HttpGet(urlWithParams);
        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000).setSocketTimeout(5000).build();
        httpget.setConfig(requestConfig);

        CloseableHttpClient httpclient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String jsonStr = EntityUtils.toString(entity);// , "utf-8");
            return jsonStr;
        } finally {
            response.close();
        }
    }
}

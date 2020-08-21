package com.inspur.apigateway.gateway;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.inspur.apigateway.data.ApiServiceMonitor;
import com.inspur.apigateway.data.ServiceInput;
import com.inspur.apigateway.utils.OpenServiceConstants;
import com.inspur.apigateway.utils.StringUtil;
import com.inspur.apigateway.utils.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.inspur.apigateway.utils.OpenServiceConstants.*;
import static com.inspur.apigateway.utils.OpenServiceConstants.ASM_ERROR_REQUEST_TEXT;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

/**
 * Http请求方法 get post put delete
 */
@Component
public class HttpRequestMethod {
    private static Log log = LogFactory.getLog(HttpRequestMethod.class);

    @Value("manage.open.connect.timeout")
    private static int  CONNECT_TIMEOUT;

    @Value("manage.open.socket.timeout")
    private static int  SOCKET_TIMEOUT;

    @Value("manage.open.connection.request.timeout")
    private static int CONNECTION_REQUEST_TIMEOUT;

    //http客户端
    private static AsyncHttpClient ahc = asyncHttpClient(config().setConnectTimeout(CONNECT_TIMEOUT).setRequestTimeout(SOCKET_TIMEOUT));

    /**
     * GET方法访问URL
     *
     * @param url
     * @param headersMap
     * @return
     */
    public static String execGet(ApiServiceMonitor apiServiceMonitor, String dataType, HttpServletResponse responseOut, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, List<ServiceInput> listServiceInput) {

        StringBuffer urlAppend = new StringBuffer();
        boolean firstQueryParam = true;
        for (Map.Entry<String,Object> temp : paramsMap.entrySet()){
            String key = temp.getKey();
            Object value = temp.getValue();
            if (firstQueryParam) {
                urlAppend.append("?").append(key).append("=").append(URLEncoder.encode((String)value));
                firstQueryParam = false;
            } else {
                urlAppend.append("&").append(key).append("=").append(URLEncoder.encode((String)value));
            }
        }
        HttpGet httpGet = new HttpGet(url + urlAppend.toString());

        // 设置请求头
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();//设置请求和传输超时时间
        httpGet.setConfig(requestConfig);
        if (StringUtil.isNotEmpty(headersMap)) {
            for (String key : headersMap.keySet()) {
                httpGet.addHeader(key, headersMap.get(key));
            }
        }

        // 执行请求
        String result = "";
        try {
            CloseableHttpClient httpclient = createCloseableHttpClient(url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            log.info("GET接口返回状态码:["+code+"]");
            if (code == 200){
                apiServiceMonitor.setResult(ASM_SUCCESS);
            }else {
                apiServiceMonitor.setResult(ASM_ERROR_STATUS_CODE);
                apiServiceMonitor.setNotes("GET接口返回状态码异常:["+code+"]");
            }
            HttpEntity entity = response.getEntity();

            if (StringUtils.equals(dataType,"binary")){
                createResponseFile(headersMap,responseOut, response, entity,listServiceInput);
            }else{
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);

        }catch (HttpException he){
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关GET method failed to access URL，URL：" + url, he);
        }catch (FileNotFoundException fnfe){
            result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+fnfe.getMessage(), apiServiceMonitor);
            log.error("内部错误,FileNotFoundException,URL=[" + url+"] 错误信息"+ fnfe.getMessage());
        }catch (IOException ioe){
            if( ioe instanceof ConnectException || ioe instanceof SocketTimeoutException || ioe instanceof UnknownHostException){
                result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
                log.error("API网关GET method failed to access URL，URL：" + url, ioe);
            }else {
                result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+ioe.getMessage(), apiServiceMonitor);
                log.error("内部错误,IOException,URL=[" + url+"] 错误信息"+ ioe.getMessage());
            }
        } catch (Exception e) {
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关GET method failed to access URL，URL：" + url, e);
        }
        return result;
    }

    public static String execGetJson(ApiServiceMonitor apiServiceMonitor, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, String instream) {
        BoundRequestBuilder brb = ahc.prepareGet(url);
        if (headersMap.size() > 0) {
            for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                brb.setHeader(entry.getKey(), entry.getValue());
            }
        }
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (paramsMap.size() > 0) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } else if (StringUtils.isNotEmpty(instream)) {
            json = JSON.parseObject(instream);
        }
        String result = "";
        Response response = null;
        try {
            Future<Response> future = brb
                    .setHeader("Content-Type", OpenServiceConstants.content_type_json)
                    .setBody(json.toJSONString())
                    .execute(new AsyncCompletionHandler<Response>() {
                        @Override
                        public Response onCompleted(Response response) {
                            return response;
                        }

                        @Override
                        public void onThrowable(Throwable t) {
                            log.error("GET调用JSON出错:" + t.getMessage());
                        }
                    });

            response = future.get();
            result = response.getResponseBody();
        } catch (Exception e) {
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关GET method failed to access URL，URL：" + url, e);
            if (response != null) {
                log.error("code=[" + response.getStatusCode() + "] body=[" + response.getResponseBody() + "]");
            }
        }
        return result;
    }
    
    /**
     * POST方法访问URL
     *
     * @param url
     * @return
     */

    public static String execPost(ApiServiceMonitor apiServiceMonitor, String dataType, HttpServletResponse responseOut, String instream, String scType, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, Map<String, Object> paramsType, List<ServiceInput> serviceInputList, String formType) throws IOException {
        // 创建请求对象，进行初始化设置
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();//设置请求和传输超时时间
        httpPost.setConfig(requestConfig);

        // 赋值请求头
        if (StringUtil.isNotEmpty(headersMap)) {
            for (String key : headersMap.keySet()) {
                httpPost.addHeader(key, headersMap.get(key));
            }
        }

        // 如果参数不为空
        File file = null;
        if (paramsMap.size()>0) {

            if(StringUtils.equals(formType,"application/x-www-form-urlencoded")){
                List<NameValuePair> pairList = new ArrayList<>(paramsMap.size());
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                            .getValue().toString());
                    pairList.add(pair);
                }
                httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            }else if(StringUtils.equals(formType,"multipart/form-data")){
                // 复杂Entity构造器
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

                // 遍历Query参数，赋值到 entityBuilder
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    //添加普通参数
                    if(paramsType.get(entry.getKey()).equals(SC_TYPE_FILE)){
                        //添加上传的文件
                        MultipartFile mulFile = (MultipartFile) entry.getValue();
                        String fileName = mulFile.getOriginalFilename();
                        file = asFile(mulFile.getInputStream(),fileName);
                        entityBuilder.addPart(entry.getKey(),new FileBody(file));
                    }else{
                        ContentType strContent = ContentType.create("text/plain", Charset.forName("UTF-8"));
                        entityBuilder.addTextBody(entry.getKey(), (String)entry.getValue(),strContent);
                    }
                }
                HttpEntity httpEntity = entityBuilder.build();
                httpPost.setEntity(httpEntity);
            }
//        } else if (StringUtils.isNotEmpty(instream) && (scType.equals(OpenServiceConstants.SC_TYPE_APPLICATION_JSON) || scType.equals(OpenServiceConstants.SC_TYPE_TEXT_XML))) {
        } else if (StringUtils.isNotEmpty(instream)) {
            switch (scType) {
                case OpenServiceConstants.SC_TYPE_APPLICATION_JSON:
                    httpPost.addHeader("Content-Type", OpenServiceConstants.content_type_json);
                    break;
                case OpenServiceConstants.SC_TYPE_TEXT_XML:
                    httpPost.addHeader("Content-Type", OpenServiceConstants.content_type_text_xml);
                    break;
                default:
                    httpPost.addHeader("Content-Type", "text/plain");
                    break;
            }
            try {
                httpPost.setEntity(new StringEntity(instream, "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 执行请求
        String result = "";
        try{
            final CloseableHttpClient httpclient = createCloseableHttpClient(url);
            final CloseableHttpResponse response = httpclient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            log.info("POST接口返回状态码:["+code+"]");
            if (code == 200){
                apiServiceMonitor.setResult(ASM_SUCCESS);
            }else {
                apiServiceMonitor.setResult(ASM_ERROR_STATUS_CODE);
                apiServiceMonitor.setNotes("POST接口返回状态码异常:["+code+"]");
            }
            // 响应Entity
            HttpEntity entity = response.getEntity();
            if (StringUtils.equals(dataType,"binary")){
                createResponseFile(headersMap, responseOut, response, entity, serviceInputList);
            }else{
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
        }catch (HttpException he){
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关POST method failed to access URL，URL：" + url, he);
        }catch (FileNotFoundException fnfe){
            result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+fnfe.getMessage(), apiServiceMonitor);
            log.error("内部错误,FileNotFoundException,URL=[" + url+"] 错误信息"+ fnfe.getMessage());
        }catch (IOException ioe){
            if( ioe instanceof ConnectException || ioe instanceof SocketTimeoutException || ioe instanceof UnknownHostException){
                result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
                log.error("API网关POST method failed to access URL，URL：" + url, ioe);
            }else {
                result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+ioe.getMessage(), apiServiceMonitor);
                log.error("内部错误,IOException,URL=[" + url+"] 错误信息"+ ioe.getMessage());
            }
        } catch (Exception e) {
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关POST method failed to access URL，URL：" + url, e);
        }finally {
            if (file != null){
                file.delete();
            }
        }

        return result;
    }


    /**
     * Delete方法访问URL
     *
     * @param url
     * @return
     */

    public static String execDelete(ApiServiceMonitor apiServiceMonitor, String dataType, HttpServletResponse responseOut, String instream, String scType, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, Map<String, Object> paramsType, List<ServiceInput> serviceInputList) throws IOException {
        // 创建请求对象，进行初始化设置
        HttpDelete httpDelete = new HttpDelete(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();//设置请求和传输超时时间
        httpDelete.setConfig(requestConfig);

        // 赋值请求头
        if (StringUtil.isNotEmpty(headersMap)) {
            for (String key : headersMap.keySet()) {
                httpDelete.addHeader(key, headersMap.get(key));
            }
        }

        // 如果参数不为空
        File file = null;
        if (paramsMap.size() > 0) {
            // 复杂Entity构造器
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

            // 遍历Query参数，赋值到 entityBuilder
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                //添加普通参数
                if (paramsType.get(entry.getKey()).equals(SC_TYPE_FILE)) {
                    //添加上传的文件
                    MultipartFile mulFile = (MultipartFile) entry.getValue();
                    String fileName = mulFile.getOriginalFilename();
                    file = asFile(mulFile.getInputStream(), fileName);
                    entityBuilder.addPart(entry.getKey(), new FileBody(file));
                } else {
                    entityBuilder.addTextBody(entry.getKey(), (String) entry.getValue());
                }
            }
            HttpEntity httpEntity = entityBuilder.build();
            httpDelete.setEntity(httpEntity);

        } else if (StringUtils.isNotEmpty(instream)) {
            switch (scType) {
                case OpenServiceConstants.SC_TYPE_APPLICATION_JSON:
                    httpDelete.addHeader("Content-Type", OpenServiceConstants.content_type_json);
                    break;
                case OpenServiceConstants.SC_TYPE_TEXT_XML:
                    httpDelete.addHeader("Content-Type", OpenServiceConstants.content_type_text_xml);
                    break;
                default:
                    httpDelete.addHeader("Content-Type", "text/plain");
                    break;
            }
            try {
                httpDelete.setEntity(new StringEntity(instream, "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 执行请求
        String result = "";
        try {
            final CloseableHttpClient httpclient = createCloseableHttpClient(url);
            final CloseableHttpResponse response = httpclient.execute(httpDelete);
            int code = response.getStatusLine().getStatusCode();
            log.info("DELETE接口返回状态码:["+code+"]");
            if (code == 200) {
                apiServiceMonitor.setResult(ASM_SUCCESS);
            }else {
                apiServiceMonitor.setResult(ASM_ERROR_STATUS_CODE);
                apiServiceMonitor.setNotes("DELETE接口返回状态码异常:["+code+"]");
            }
            // 响应Entity
            HttpEntity entity = response.getEntity();
            if (StringUtils.equals(dataType, "binary")) {
                createResponseFile(headersMap, responseOut, response, entity, serviceInputList);
            } else {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
        }catch (HttpException he){
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关DELETE method failed to access URL，URL：" + url, he);
        }catch (FileNotFoundException fnfe){
            result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+fnfe.getMessage(), apiServiceMonitor);
            log.error("内部错误,FileNotFoundException,URL=[" + url+"] 错误信息"+ fnfe.getMessage());
        }catch (IOException ioe){
            if( ioe instanceof ConnectException || ioe instanceof SocketTimeoutException || ioe instanceof UnknownHostException){
                result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
                log.error("API网关DELETE method failed to access URL，URL：" + url, ioe);
            }else {
                result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误","内部错误:"+ioe.getMessage(), apiServiceMonitor);
                log.error("内部错误,IOException,URL=[" + url+"] 错误信息"+ ioe.getMessage());
            }
        } catch (Exception e) {
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关DELETE method failed to access URL，URL：" + url, e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }

        return result;
    }

    /**
     * PUT方法访问URL
     *
     * @param url
     * @return
     */

    public static String execPut(ApiServiceMonitor apiServiceMonitor, String dataType, HttpServletResponse responseOut, String instream, String scType, String url, Map<String, String> headersMap, Map<String, Object> paramsMap, Map<String, Object> paramsType, List<ServiceInput> serviceInputList) throws IOException {
        // 创建请求对象，进行初始化设置
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build();//设置请求和传输超时时间
        httpPut.setConfig(requestConfig);

        // 赋值请求头
        if (StringUtil.isNotEmpty(headersMap)) {
            for (String key : headersMap.keySet()) {
                httpPut.addHeader(key, headersMap.get(key));
            }
        }

        // 如果参数不为空
        File file = null;
        if (paramsMap.size() > 0) {
            // 复杂Entity构造器
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

            // 遍历Query参数，赋值到 entityBuilder
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                //添加普通参数
                if (paramsType.get(entry.getKey()).equals(SC_TYPE_FILE)) {
                    //添加上传的文件
                    MultipartFile mulFile = (MultipartFile) entry.getValue();
                    String fileName = mulFile.getOriginalFilename();
                    file = asFile(mulFile.getInputStream(), fileName);
                    entityBuilder.addPart(entry.getKey(), new FileBody(file));
                } else {
                    entityBuilder.addTextBody(entry.getKey(), (String) entry.getValue());
                }
            }
            HttpEntity httpEntity = entityBuilder.build();
            httpPut.setEntity(httpEntity);

        } else if (StringUtils.isNotEmpty(instream)) {
            switch (scType) {
                case OpenServiceConstants.SC_TYPE_APPLICATION_JSON:
                    httpPut.addHeader("Content-Type", OpenServiceConstants.content_type_json);
                    break;
                case OpenServiceConstants.SC_TYPE_TEXT_XML:
                    httpPut.addHeader("Content-Type", OpenServiceConstants.content_type_text_xml);
                    break;
                default:
                    httpPut.addHeader("Content-Type", "text/plain");
                    break;
            }
            try {
                httpPut.setEntity(new StringEntity(instream, "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 执行请求
        String result = "";
        try {
            final CloseableHttpClient httpclient = createCloseableHttpClient(url);
            final CloseableHttpResponse response = httpclient.execute(httpPut);
            int code = response.getStatusLine().getStatusCode();
            log.info("PUT接口返回状态码:["+code+"]");
            if (code == 200) {
                apiServiceMonitor.setResult(ASM_SUCCESS);
            }else {
                apiServiceMonitor.setResult(ASM_ERROR_STATUS_CODE);
                apiServiceMonitor.setNotes("PUT接口返回状态码异常:["+code+"]");
            }
            // 响应Entity
            HttpEntity entity = response.getEntity();
            if (StringUtils.equals(dataType, "binary")) {
                createResponseFile(headersMap, responseOut, response, entity, serviceInputList);
            } else {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
        }catch (HttpException he){
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关PUT method failed to access URL，URL：" + url, he);
        }catch (FileNotFoundException fnfe){
            result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误", "内部错误:"+fnfe.getMessage(), apiServiceMonitor);
            log.error("内部错误,FileNotFoundException,URL=[" + url+"] 错误信息"+ fnfe.getMessage());
        }catch (IOException ioe){
            if( ioe instanceof ConnectException || ioe instanceof SocketTimeoutException || ioe instanceof UnknownHostException){
                result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
                log.error("API网关PUT method failed to access URL，URL：" + url, ioe);
            }else {
                result = Result.constructErrorResult(ASM_ERROR_UNKNOWN,"内部错误", "内部错误:"+ioe.getMessage(), apiServiceMonitor);
                log.error("内部错误,IOException,URL=[" + url+"] 错误信息"+ ioe.getMessage());
            }
        } catch (Exception e) {
            result = Result.constructErrorResult(ASM_ERROR_REQUEST, ASM_ERROR_REQUEST_TEXT, apiServiceMonitor);
            log.error("API网关PUT method failed to access URL，URL：" + url, e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
        return result;
    }



    private static void createResponseFile(Map<String,String> headersMap, HttpServletResponse responseOut, CloseableHttpResponse response, HttpEntity entity, List<ServiceInput> listServiceInput) throws Exception {
        Header contentHead = response.getLastHeader("Content-Disposition");
        String filename = null;
        if (contentHead != null){
            HeaderElement[] elements = contentHead.getElements();
            for (HeaderElement el : elements) {
                //遍历，获取filename
                NameValuePair pair = el.getParameterByName("filename");
                filename = pair.getValue();

                if (null != filename) {
                    break;
                }
            }
        }else{
            filename = UUID.randomUUID().toString();
            for (ServiceInput serviceInput : listServiceInput) {
                String paramPositionType = serviceInput.getScParamType();
                if (paramPositionType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_PATH)) {
                    filename = (String)serviceInput.getValue();
                    break;
                }
            }
        }

        InputStream inputStream = entity.getContent();

        // 生成本地文件
//        File tmp = File.createTempFile("tmp", filename, new File("C:\\"));
//        OutputStream os1 = new FileOutputStream(tmp);
//        int bytesRead = 0;
//        byte[] buffer1 = new byte[8192];
//        while ((bytesRead = inputStream.read(buffer1, 0, 8192)) != -1) {
//            os1.write(buffer1, 0, bytesRead);
//        }
//        inputStream.close();

//        if(tmp.exists()) {
        responseOut.setContentType("application/octet-stream");
        responseOut.addHeader("Content-Disposition","attachment;filename="+filename);
        byte[] buffer = new byte[1024];
        InputStream fis =null;
        BufferedInputStream bis =null;
        try {
            fis = inputStream;
            bis = new BufferedInputStream(fis);
            OutputStream os = responseOut.getOutputStream();
            int i = bis.read(buffer);
            while ( i!=-1 ) {
                os.write(buffer, 0, i);
                i=bis.read(buffer);
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        }
    }



    public static CloseableHttpClient createCloseableHttpClient(String url) throws Exception {
        if (!url.toLowerCase().startsWith("https")) {
            return HttpClients.createDefault();
        }
        SSLContext sslcontext = createIgnoreVerifySSL();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(100);
        return HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(connManager)
                .build();
    }

    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }


    private static File asFile(InputStream inputStream, String fileName) throws IOException{
        // 创建相对路径（tomcat的bin目录或jdk的bin目录）的临时目录（避免非root用户绝对路径无权限的问题）
        File dir = new File("./tempFile/");
        File tmp = new File("./tempFile/"+fileName);
        try{
            if(!dir.exists()){
                dir.mkdirs();
            }
            if (!tmp.exists()){
                tmp.createNewFile();
            }
        }catch (Exception e){
            // 创建不成功时，读取配置文件
            log.info("创建文件夹出错："+e.toString());
            tmp = new File(PropertiesUtil.getValue(OpenServiceConstants.CONF_PROPERTIES, "server.tempFilePath")+fileName);
        }

        OutputStream os = new FileOutputStream(tmp);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return tmp;
    }

    static class HttpDelete extends HttpEntityEnclosingRequestBase {

        public static final String METHOD_NAME = "DELETE";

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDelete(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDelete(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDelete() {
            super();
        }
    }

    public static String getRequestIn(HttpServletRequest request) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        return responseStrBuilder.toString();
    }

}

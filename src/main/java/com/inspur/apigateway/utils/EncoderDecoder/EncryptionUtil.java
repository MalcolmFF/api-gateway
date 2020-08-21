package com.inspur.apigateway.utils.EncoderDecoder;

import com.inspur.apigateway.utils.HttpUtils;
import com.inspur.apigateway.utils.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具类
 */
public class EncryptionUtil {
    private static final Log log = LogFactory.getLog(EncryptionUtil.class);
    public static final String KEY_SHA = "SHA";
    public static final String KEY_MD5 = "MD5";

    /**
     * BASE64解密,入参出参为String
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptBASE64String(String key) throws Exception {
        BASE64Decoder decoder = new BASE64Decoder();
        return new String(decoder.decodeBuffer(key));
    }

    /**
     * 通过REST接口进行解密
     *
     * @return
     * @throws Exception
     */
    public static String decryptRESTString(String url,String name,String value) throws Exception {
        return crypt(url, name, value);
    }
    /**
     * 通过REST接口进行加密
     *
     * @return
     * @throws Exception
     */
    public static String encryptRESTString(String url,String name,String value) throws Exception {
        return crypt(url, name, value);
    }

    private static String crypt(String url, String name, String value) {
        String result = null;
        try {
            Map<String, Object> params = new HashMap<>();
//            params.put(name, value);
            params.put("content", URLEncoder.encode(value));
            result = HttpUtils.httpsGet(url, params);
            if (StringUtil.isEmpty(result)) {
                log.error("参数Rest解密异常.");
            }
        } catch (Exception e) {
            log.error("参数Rest解密异常：" + e.getMessage());
        }
        return result;
    }


    /**
     * BASE64加密,入参出参为String
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptBASE64String(String data) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data.getBytes());
    }

    /**
     * MD5加密,入参出参为String
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptMD5String(String data) {
        return DigestUtils.md5Hex(data);
    }

    /**
     * SHA-1加密,入参出参为String
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encryptSHA1(String data) {
        return DigestUtils.sha1Hex(data);
    }

}

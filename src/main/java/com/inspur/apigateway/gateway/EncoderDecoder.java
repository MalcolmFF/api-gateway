package com.inspur.apigateway.gateway;

import com.inspur.apigateway.data.ServiceInput;
import com.inspur.apigateway.utils.EncoderDecoder.SM3;
import com.inspur.apigateway.utils.StringUtil;
import org.apache.commons.lang.StringUtils;

import static com.inspur.apigateway.utils.OpenServiceConstants.*;
import static com.inspur.apigateway.utils.OpenServiceConstants.ENCRYPT_MODE_KEY_SHA_1;
import static com.inspur.apigateway.utils.EncoderDecoder.EncryptionUtil.*;

/**
 * 接口参数，响应的加解密
 */
public class EncoderDecoder {
    /**
     * 解密参数 接口级
     *
     * @param encryptionType
     * @return
     * @throws Exception
     */
    public static Object decryptedParam(ServiceInput serviceInput, String encryptionType) throws Exception {
        if (serviceInput.getType().equals(SC_TYPE_FILE)){
            return serviceInput.getValue();
        }
        String decryptedParamStr = "";
        String param = (String)serviceInput.getValue();
        String url = serviceInput.getDecryptUrl();
        String name = serviceInput.getName();
        if (StringUtil.isNotEmpty(encryptionType) && StringUtil.isNotEmpty(ENCRYPTION_MAP.get(encryptionType))) {
            switch (encryptionType) {
                case ENCRYPT_MODE_NO:
                    decryptedParamStr = param;
                    break;
                case ENCRYPT_MODE_KEY_BASE64:
                    decryptedParamStr = decryptBASE64String(param);
                    break;
                case ENCRYPT_MODE_KEY_REST:
                    decryptedParamStr = decryptRESTString(url,name,param);
                    break;
                case ENCRYPT_MODE_KEY_SM3:
                    throw new Exception("暂不支持国密SM3解密，param = [" + param + "]");
                case ENCRYPT_MODE_KEY_MD5:
                    throw new Exception("暂不支持MD5解密，param = [" + param + "]");
                case ENCRYPT_MODE_KEY_SHA_1:
                    throw new Exception("暂不支持SHA-1解密，param = [" + param + "]");
                default:
                    throw new Exception("暂不支持对所选加密方式解密，param = [" + param + "],encryptionType = [" + encryptionType + "]");
            }
        } else {
            throw new Exception("传入加密方式不正确，请检查后重试，param = [" + param + "],encryptionType = [" + encryptionType + "]");
        }
        // 赋值后进行参数级加解密
        serviceInput.setValue(decryptedParamStr);

        return decryptedParam_paramLevel(serviceInput);
    }

    /**
     * 解密参数 参数级
     *
     * @return
     * @throws Exception
     */
    public static String decryptedParam_paramLevel(ServiceInput serviceInput) throws Exception {
        String after_value = "";
        String param = (String)serviceInput.getValue();
        String name = serviceInput.getName();

        String decryptType = serviceInput.getDecryptType();
        String encryptType = serviceInput.getEncryptType();
        String decryptUrl = serviceInput.getDecryptUrl();
        String encryptUrl = serviceInput.getEncryptUrl();

        // 执行解密
        if ( StringUtil.isNotEmpty(decryptType) && !StringUtils.equals(decryptType,"")) {
            switch (decryptType) {
                case ENCRYPT_MODE_NO:
                    after_value = param;
                    break;
                case ENCRYPT_MODE_KEY_BASE64:
                    after_value = decryptBASE64String(param);
                    break;
                case ENCRYPT_MODE_KEY_REST:
                    after_value = decryptRESTString(decryptUrl,name,param);
                    break;
                case ENCRYPT_MODE_KEY_SM3:
                    throw new Exception("暂不支持国密SM3解密，param = [" + param + "]");
                case ENCRYPT_MODE_KEY_MD5:
                    throw new Exception("暂不支持MD5解密，param = [" + param + "]");
                case ENCRYPT_MODE_KEY_SHA_1:
                    throw new Exception("暂不支持SHA-1解密，param = [" + param + "]");
                default:
                    throw new Exception("暂不支持对所选解密方式，param = [" + param + "],encryptionType = [" + decryptType + "]");
            }
        }
        // 执行加密
        if ( StringUtil.isNotEmpty(encryptType) && !StringUtils.equals(encryptType,"") ) {
            switch (encryptType) {
                case ENCRYPT_MODE_NO:
                    after_value = after_value;
                    break;
                case ENCRYPT_MODE_KEY_BASE64:
                    after_value = encryptBASE64String(after_value);
                    break;
                case ENCRYPT_MODE_KEY_REST:
                    after_value = encryptRESTString(encryptUrl,name,after_value);
                    break;
                case ENCRYPT_MODE_KEY_SM3:
                    throw new Exception("暂不支持国密SM3加密，param = [" + after_value + "]");
                case ENCRYPT_MODE_KEY_MD5:
                    throw new Exception("暂不支持MD5加密，param = [" + after_value + "]");
                case ENCRYPT_MODE_KEY_SHA_1:
                    throw new Exception("暂不支持SHA-1加密，param = [" + after_value + "]");
                default:
                    throw new Exception("暂不支持对所选加密方式，param = [" + after_value + "],encryptionType = [" + encryptType + "]");
            }
        }
        return after_value;
    }

    /**
     * 加密返回值
     *
     * @param result
     * @param encryptionType
     * @return
     * @throws Exception
     */
    public static String encryptionResult(String result, String encryptionType) throws Exception {
        String encryptionResultStr = "";
        if (StringUtil.isNotEmpty(encryptionType) && StringUtil.isNotEmpty(ENCRYPTION_MAP.get(encryptionType))) {
            switch (encryptionType) {
                case ENCRYPT_MODE_NO:
                    encryptionResultStr = result;
                    break;
                case ENCRYPT_MODE_KEY_BASE64:
                    encryptionResultStr = encryptBASE64String(result);
                    break;
                case ENCRYPT_MODE_KEY_SM3:
                    encryptionResultStr = SM3.byteArrayToHexString(SM3.hash(result.getBytes()));
                    break;
                case ENCRYPT_MODE_KEY_MD5:
                    encryptionResultStr = encryptMD5String(result);
                    break;
                case ENCRYPT_MODE_KEY_SHA_1:
                    encryptionResultStr = encryptSHA1(result);
                    break;
                default:
                    throw new Exception("暂不支持所选加密方式，param = [" + result + "],encryptionType = [" + encryptionType + "]");
            }
        } else {
            throw new Exception("传入加密方式不正确，请检查后重试，param = [" + result + "],encryptionType = [" + encryptionType + "]");
        }
        return encryptionResultStr;
    }
}

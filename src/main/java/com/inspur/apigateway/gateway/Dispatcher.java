package com.inspur.apigateway.gateway;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.inspur.apigateway.data.ApiServiceMonitor;
import com.inspur.apigateway.data.ServiceDef;
import com.inspur.apigateway.data.ServiceInput;
import com.inspur.apigateway.utils.OpenServiceConstants;
import net.sf.json.JSONObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.inspur.apigateway.utils.OpenServiceConstants.ASM_ERROR_UNKNOWN;

public class Dispatcher {

    private static Log log = LogFactory.getLog(Dispatcher.class);

    public static String execHttpRequest(HttpServletResponse response, String instream, ServiceDef serviceDef, List<ServiceInput> listServiceInput, ApiServiceMonitor apiServiceMonitor) throws Exception {
        String scType = null;
        String dataType = serviceDef.getContentType();

        // 地址拼接
        String addr = serviceDef.getScAddr();
        addr = addr.endsWith("/") ? addr.substring(0, addr.length() - 1) : addr;
        StringBuilder httpurl = new StringBuilder();
        httpurl.append(addr);

        // monitor记录所有入参
        JSONObject serviceInputParam = new JSONObject();

        Collections.sort(listServiceInput);//根据scSeq排序

        //组装后端参数
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "*/*");

        Map<String,Object> paramsMap = new HashMap<>();
        Map<String,Object> paramsTypeMap = new HashMap<>();

        // 核保定制化（将日志id传递给核保获取模型接口）
        if (httpurl.indexOf("getHealthModel") != -1){
            String key = "logId";
            String value = apiServiceMonitor.getId();
            paramsMap.put(key,value);
            paramsTypeMap.put(key,"string");
            serviceInputParam.put(key, value);
        }

        for (ServiceInput serviceInput : listServiceInput) {
            String paramsType = serviceInput.getType();
            String paramsName = serviceInput.getName();
            String paramPositionType = serviceInput.getScParamType();

            // 入参解密
            Object decryptedParam = EncoderDecoder.decryptedParam(serviceInput, serviceDef.getEncryptionType());
            if (decryptedParam!=null){
                serviceInputParam.put(paramsName, decryptedParam.toString());
            }

            // 根据参数位置 来进行处理
            if (paramPositionType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_PATH)) {
                httpurl.append("/").append(decryptedParam);
            } else if (paramPositionType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_QUERY)) {
                if (decryptedParam != null) {
                    paramsMap.put(paramsName, decryptedParam);
                }
            } else if (paramPositionType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_HEAD)) {
                headerMap.put(paramsName, (String)decryptedParam);
            } else if (paramPositionType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_BODY)) {
                // 如果选择了Body类型，但不是raw类型，跟query一样，也放到 K-V Map
                if (!serviceInput.getScType().equals(OpenServiceConstants.SC_TYPE_APPLICATION_JSON) && !serviceInput.getScType().equals(OpenServiceConstants.SC_TYPE_TEXT_XML)) {
                    if (decryptedParam != null ) {
                        paramsMap.put(paramsName, decryptedParam);
                    }
                } else {
                    if (StringUtils.isEmpty(instream) && decryptedParam != null) {
                        instream = (String) decryptedParam;
                    }
                    scType = serviceInput.getScType();
                }
            }
            paramsTypeMap.put(paramsName,paramsType);
        }
        String method = serviceDef.getScHttpMethod().toUpperCase();
        String result;
        apiServiceMonitor.setServiceInput(serviceInputParam.toString());
        apiServiceMonitor.setServiceInputHeader(JSONObject.fromObject(headerMap).toString());
        apiServiceMonitor.setServiceMethod(method);
        switch (method) {
            case "GET":
                if (OpenServiceConstants.SC_TYPE_APPLICATION_JSON.equals(scType)) {
                    result = HttpRequestMethod.execGetJson(apiServiceMonitor,httpurl.toString(), headerMap, paramsMap, instream);
                } else {
                    result = HttpRequestMethod.execGet(apiServiceMonitor, dataType, response, httpurl.toString(), headerMap, paramsMap, listServiceInput);
                }
                break;
            case "POST":
                result = HttpRequestMethod.execPost(apiServiceMonitor, dataType, response, instream, scType, httpurl.toString(), headerMap, paramsMap, paramsTypeMap, listServiceInput, serviceDef.getFormType());
                break;
            case "DELETE":
                result = HttpRequestMethod.execDelete(apiServiceMonitor, dataType, response, instream, scType, httpurl.toString(), headerMap, paramsMap, paramsTypeMap, listServiceInput);
                break;
            case "PUT":
                result = HttpRequestMethod.execPut(apiServiceMonitor, dataType, response, instream, scType, httpurl.toString(), headerMap, paramsMap, paramsTypeMap, listServiceInput);
                break;
            default:
                result = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "请求错误:不支持的请求方式", apiServiceMonitor);
        }
        apiServiceMonitor.setServiceOutput(result);

        result = EncoderDecoder.encryptionResult(result, serviceDef.getEncryptionType());
        return result;
    }

    public static String executeRPCAxis(ServiceDef ws, List<ServiceInput> serviceInputList, ApiServiceMonitor apiServiceMonitor) throws Exception {
        // monitor记录所有入参
        JSONObject serviceInputParam = new JSONObject();
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress(ws.getScAddr());
        call.setOperationName(new QName(ws.getNameSpace(), ws.getSc_ws_function()));
        Object[] parameters;
        if (serviceInputList != null) {
            parameters = new Object[serviceInputList.size()];
            int j = 0;
            for (ServiceInput item : serviceInputList) {
                parameters[j++] = item.getValue();
                call.addParameter(item.getName(), org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
                serviceInputParam.put(item.getName(), item.getValue());
            }
            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
        } else {
            parameters = new Object[]{null};
        }
        apiServiceMonitor.setServiceInput(serviceInputParam.toString());
        return (String) call.invoke(parameters);
    }


    public static String executeRPCAxisClient(ServiceDef ws, List<ServiceInput> serviceInputList, ApiServiceMonitor apiServiceMonitor) throws Exception {
        JSONObject serviceInputParam = new JSONObject();
        Object[] parameters = new Object[serviceInputList.size()];
        /*
        命名空间
         NameSpace为axis中的SOAPActionURI
        方法名
         Sc_ws_function为axis中的OperationNameQName，
         其中localPart对应OperationDesc中的name，
         字符串格式是{namespaceURI}localPart或者localPart，是QName格式
        WS_RETURN_TYPE
            ws.getWsReturnType()对应的是returnTypeQName，字符串格式是{namespaceURI}localPart或者localPart，是QName格式
        WS_RETURN_QNAME
            ws.getWsReturnQname()对应的是returnQName，字符串格式是{namespaceURI}localPart或者localPart，是QName格式
         */
        Call call = AxisClientUtil.initAxisClientCall(ws.getScAddr(), ws.getNameSpace(), ws.getSc_ws_function(), ws.getWsReturnType(), ws.getWsReturnQname(), serviceInputList, parameters, serviceInputParam);
        apiServiceMonitor.setServiceInput(serviceInputParam.toString());
        return (String) call.invoke(parameters);
    }

    public static String executeAxis2(ServiceDef ws, List<ServiceInput> listServiceInput, ApiServiceMonitor apiServiceMonitor) throws AxisFault {
        try {
            // monitor记录所有入参
            JSONObject serviceInputParam = new JSONObject();
            String[] params = new String[listServiceInput.size()];
            int i = 0;
            for (ServiceInput serviceInput : listServiceInput) {
                String paramType = serviceInput.getScParamType();
                if (paramType.equalsIgnoreCase("body")) {
                    params[i++] = serviceInput.getName();
                }
            }

            String[] paramValues = new String[listServiceInput.size()];
            int j = 0;
            for (ServiceInput item : listServiceInput) {
                paramValues[j++] = (String)item.getValue();
                serviceInputParam.put(item.getName(), item.getValue());
            }
            apiServiceMonitor.setServiceInput(serviceInputParam.toString());

            OMElement getPricePayload = buildParam(ws.getNameSpace(), params, paramValues, "tn", ws.getSc_ws_function(), "tn");

            Options options = new Options();
            options.setTo(new EndpointReference(ws.getScAddr()));
            options.setTransportInProtocol("http");


            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);


            OMElement result = sender.sendReceive(getPricePayload);
            String response = result.getFirstElement().getText();
            apiServiceMonitor.setResult("200");
            return response;
        } catch (AxisFault e) {
            log.error("axis2执行异常", e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static OMElement buildParam(String nameSpace, String[] params, String[] paramValues, String paramPrefix, String method, String wsMethodPrefix) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(nameSpace, wsMethodPrefix);
        OMNamespace omNsParam = (paramPrefix == null) ? null : fac.createOMNamespace(nameSpace, paramPrefix);
        OMElement data = fac.createOMElement(method, omNs);
        for (int i = 0; i < params.length; i++) {
            OMElement inner = fac.createOMElement(params[i], omNsParam);
            inner.setText(paramValues[i]);
            data.addChild(inner);
        }
        return data;
    }


}

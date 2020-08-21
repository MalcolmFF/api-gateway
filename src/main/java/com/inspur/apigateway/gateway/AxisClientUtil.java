package com.inspur.apigateway.gateway;

import com.inspur.apigateway.data.ServiceInput;
import net.sf.json.JSONObject;
import org.apache.axis.AxisEngine;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.soap.SOAPConstants;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class AxisClientUtil {

    /**
     * 初始化Call
     */
    public static Call initAxisClientCall(String address, String SOAPActionURI, String operationName, String returnTypeStr,
                                          String returnQNameStr, List<ServiceInput> serviceInputList, Object[] parameters,
                                          JSONObject serviceInputParam) throws ServiceException, MalformedURLException {
        Service service = new Service();
        QName opName = QName.valueOf(operationName);
        Call call = null;
        call = (Call) service.createCall();
        call.setTargetEndpointAddress(new URL(address));
        call.setOperation(initAxisClientOperation(opName.getLocalPart(), serviceInputList, returnTypeStr, returnQNameStr, parameters, serviceInputParam));
        call.setUseSOAPAction(true);
        call.setSOAPActionURI(SOAPActionURI == null ? "" : SOAPActionURI);
        call.setEncodingStyle(null);
        call.setProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
        call.setProperty(AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
        call.setOperationName(opName);
        return call;
    }

    /**
     * 初始化服务描述
     */
    private static OperationDesc initAxisClientOperation(String name, List<ServiceInput> serviceInputList, String returnTypeStr,
                                                         String returnQNameStr, Object[] parameters, JSONObject serviceInputParam) {
        OperationDesc oper;
        ParameterDesc param;
        oper = new OperationDesc();
        oper.setName(name);
        int j = 0;

        /*
        后端参数名称
            ScName对应的是ParameterDesc中的QName，字符串格式是{namespaceURI}localPart或者localPart，是QName格式
         */
        //取出所有的入参并赋值
        for (ServiceInput in : serviceInputList) {
            QName pd = QName.valueOf(in.getScName());
            param = new ParameterDesc(pd,
                    ParameterDesc.IN, new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
            param.setOmittable(true);
            oper.addParameter(param);
            parameters[j++] = in.getValue();
            serviceInputParam.put(pd.getLocalPart(), in.getValue());
        }
        //暂时只支持string格式的返回值
        oper.setReturnType(QName.valueOf(returnTypeStr));
        oper.setReturnClass(String.class);
        oper.setReturnQName(QName.valueOf(returnQNameStr));
        oper.setStyle(Style.WRAPPED);
        oper.setUse(Use.LITERAL);
        return oper;
    }
}

package com.dgut.clouddisk.handler;

import com.dgut.clouddisk.exception.CloudException;
import com.dgut.clouddisk.util.JsonData;
import com.dgut.clouddisk.util.StatusCode;
import com.obs.services.exception.ObsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


/**
 * @ClassName GlobalExceptionHandler
 * @Description 异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     *
     * @Title handlerCloudException
     * @Description 处理自定义异常
     * @param ex 异常
     * @return 返回JsonData封装的错误信息
     */
    @ExceptionHandler(value = CloudException.class)
    public JsonData handlerPanException(CloudException ex) {
        return JsonData.buildError(ex.getCode(), ex.getMessage());
    }

    /**
     *
     * @Title handlerObsException
     * @Description 处理obs异常
     * @param ex 异常
     * @return 返回JsonData封装的错误信息
     */
    @ExceptionHandler(ObsException.class)
    public JsonData handlerObsException(ObsException ex) {
        return JsonData.buildError(StatusCode.OBS_ERROR.code(), ex.getErrorMessage());
    }

//    /**
//     *
//     * @Title defaultHandler
//     * @Description 默认错误处理（用来处理http异常以及其他异常）
//     * @param ex 异常
//     * @return 返回JsonData封装的错误信息
//     */
//    @ExceptionHandler(Exception.class)
//    public JsonData defaultHandler(Exception ex) {
//        Integer code = null;
//        String message = null;
//
//        if(ex instanceof NoHandlerFoundException
//                || ex instanceof HttpRequestMethodNotSupportedException
//                || ex instanceof IllegalStateException
//                || ex instanceof MethodArgumentTypeMismatchException
//                || ex instanceof HttpMessageNotReadableException) {
//            code = StatusCode.CLIENT_ERROR.code();
//            message = StatusCode.CLIENT_ERROR.message();
//        }else {
//            code = StatusCode.SERVER_ERROR.code();
//            message = StatusCode.SERVER_ERROR.message();
//            //message = ex.getMessage();
//        }
//        return JsonData.buildError(code, message);
//    }
}

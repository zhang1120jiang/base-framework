/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.exception;

import com.unisinsight.framework.common.exception.BaseErrorCode;
import com.unisinsight.framework.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * description
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/12/04 15:03
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 记录系统运行日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        StringBuilder messageBuilder = new StringBuilder();
        String message;
        for (int i = 0; i < errors.size(); i++) {
            FieldError error = errors.get(i);
            message = Strings.isNotBlank(error.getDefaultMessage()) ? error.getDefaultMessage() : "参数异常";
            messageBuilder.append(error.getField()).append(message);
            if (i < errors.size() - 1) {
                messageBuilder.append(";");
            }
        }
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.INVALID_PARAM_ERROR.getErrorCode());
        result.setMessage(messageBuilder.toString());
        LOGGER.error("Params valid exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }


    /**
     * SQL 异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> sqlexception(SQLException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.SQL_EXCEPTION.getErrorCode());
        result.setMessage(BaseErrorCode.SQL_EXCEPTION.getMessage());
        LOGGER.error("sql  exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * 业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> baseException(BaseException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(e.getErrorCode());
        result.setMessage(e.getMessage());
        if (StringUtils.isBlank(result.getErrorCode())) {
            result.setErrorCode("500");
        }
        LOGGER.error("internal server exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 请求方法不支持
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> methodNotSupportHandle(HttpRequestMethodNotSupportedException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR.getErrorCode());
        result.setMessage(e.getMessage());
        LOGGER.error("HttpRequestMethodNotSupportedException exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 参数类型不匹配
     *
     * @param e
     * @return
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Object> typeMismatchHandle(TypeMismatchException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.PARAM_SWITCH_ERROR.getErrorCode());
        result.setMessage(BaseErrorCode.PARAM_SWITCH_ERROR.getMessage() + e.getPropertyName() + "类型应该为" + e.getRequiredType());
        LOGGER.error("param type mismatch exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 参数类型不匹配
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> httpMessageNotReadableHandle(HttpMessageNotReadableException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.PARAM_SWITCH_ERROR.getErrorCode());
        result.setMessage(BaseErrorCode.PARAM_SWITCH_ERROR.getMessage() + e.getMessage());
        LOGGER.error("param type mismatch exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    /**
     * 请求地址不存在
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> noHandlerFoundException(NoHandlerFoundException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.API_NOT_EXIST_ERROR.getErrorCode());
        result.setMessage(BaseErrorCode.API_NOT_EXIST_ERROR.getMessage() + " 请求地址：" + e.getRequestURL());
        LOGGER.error("api not exist exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<Object> noHandlerFoundException(BadSqlGrammarException e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.SQL_EXCEPTION.getErrorCode());
        result.setMessage(BaseErrorCode.SQL_EXCEPTION.getMessage());
        LOGGER.error("unknown exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * 全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> globalHandle(Exception e) {
        ErrorResult result = new ErrorResult();
        result.setErrorCode(BaseErrorCode.SYS_INTERNAL_ERROR.getErrorCode());
        result.setMessage(BaseErrorCode.SYS_INTERNAL_ERROR.getMessage());
        LOGGER.error("unknown exception={}", e.getMessage(), e);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * 异常返回体
     */
    class ErrorResult {
        /**
         * 异常code
         */
        private String errorCode;
        /**
         * 异常信息
         */
        private String message;

        public String getErrorCode() {
            return errorCode;
        }

        /**
         * 设置异常code
         *
         * @param errorCode 异常code
         */
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

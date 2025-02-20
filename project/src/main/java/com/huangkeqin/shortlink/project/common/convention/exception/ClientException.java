package com.huangkeqin.shortlink.project.common.convention.exception;


import com.huangkeqin.shortlink.project.common.convention.errorcode.BaseErrorCode;
import com.huangkeqin.shortlink.project.common.convention.errorcode.IErrorCode;

/**
 * 客户端异常
 */
public class ClientException extends AbstractException {
    /*
    构造函数 ClientException 接收一个 IErrorCode 类型的参数 errorCode，
    并调用另一个重载的构造函数，传递 null、null 和 errorCode 作为参数。
    * */
    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}

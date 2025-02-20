package com.huangkeqin.shortlink.project.common.convention.exception;


import com.huangkeqin.shortlink.project.common.convention.errorcode.IErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 * @see ClientException
 * @see ServiceException
 * @see RemoteException
 */
@Getter
public class AbstractException extends RuntimeException{
    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        //用于存储异常的错误码。通过 getErrorCode() 方法直接获取错误码，进行后续的错误处理。
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}

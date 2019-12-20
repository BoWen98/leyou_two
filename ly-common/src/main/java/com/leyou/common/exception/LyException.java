package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LyException extends RuntimeException {
    private int status;

    public LyException(ExceptionEnum em) {
        super(em.getMsg());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em, Throwable cause) {
        super(em.getMsg(), cause);
        this.status = em.getStatus();
    }

    public LyException(HttpStatus httpStatus, String msg) {
        super(msg);
        this.status=httpStatus.value();

    }
}

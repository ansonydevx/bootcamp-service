package com.onclass.bootcamp.domain.exceptions;

import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final TechnicalMessage technicalMessage;

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage.toString());
        this.technicalMessage = technicalMessage;
    }
}

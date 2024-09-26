package io.hhplus.tdd.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PointException extends RuntimeException {
    private final PointErrorResult errorResult;

    public PointException(PointErrorResult errorResult, String customMessage) {
        super(customMessage != null ? customMessage : errorResult.getMessage());
        this.errorResult = errorResult;
    }

    @Override
    public String getMessage() {
        return errorResult.getMessage() + (super.getMessage() != null ? ": " + super.getMessage() : "");
    }
}
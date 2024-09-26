package io.hhplus.tdd.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorResult {

    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "Point Not found"),
    POINT_CHARGE_LIMIT(HttpStatus.BAD_REQUEST, "Point Charge Limit"),
    POINT_USE_LIMIT(HttpStatus.BAD_REQUEST, "Point Use Limit"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
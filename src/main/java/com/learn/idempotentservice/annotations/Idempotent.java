package com.learn.idempotentservice.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Time to live for the idempotency record.
     */
    long ttl() default 60;

    /**
     * Time unit for the TTL.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * HTTP Header used to extract the key.
     */
    String headerName() default "Idempotency-key";

}

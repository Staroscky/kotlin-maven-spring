package com.curso.kotlinspringmaven.application.exception.error

import org.springframework.lang.NonNull

class RequestLimitException(val status: Int, @NonNull reason:String) : RuntimeException(reason)
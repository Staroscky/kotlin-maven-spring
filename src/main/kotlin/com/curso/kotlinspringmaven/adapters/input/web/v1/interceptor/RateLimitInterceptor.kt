package com.curso.kotlinspringmaven.adapters.input.web.v1.interceptor

import com.curso.kotlinspringmaven.application.domain.constants.KeyConstants
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication(scanBasePackages = [KeyConstants.PACKAGE_SCAN_BUCKET_RATE_LIMIT])
class RateLimitInterceptor(
    @Lazy private val interceptor: RateLimitHandlerInterceptor,
    private val environment: Environment
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        if(interceptor != null){
            registry
                .addInterceptor(interceptor)
                .addPathPatterns(KeyConstants.RATE_LIMIT_CONTROLLER_INTERCEPTOR)
        }

    }
}
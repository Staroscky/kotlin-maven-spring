package com.curso.kotlinspringmaven.adapters.input.web.v1.interceptor

import com.curso.kotlinspringmaven.application.exception.error.RequestLimitException
import com.curso.kotlinspringmaven.helper.BUCKET_LIMIT_REQUEST_USER
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.Refill
import io.github.bucket4j.grid.GridBucketState
import io.github.bucket4j.grid.ProxyManager
import io.github.bucket4j.grid.jcache.JCache
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Supplier
import javax.cache.Cache
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RateLimitHandlerInterceptor (
    private val environment: Environment,
    private val cache: Cache<String, GridBucketState>
) : HandlerInterceptor {
    private val AMOUNT_CONSUMER = 1L


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val toogleIsEnabledRateLimit = environment.getProperty("toggle.interceptor.rate-limit.enabled", "false").toBoolean()
        val bucketCapacity = environment.getProperty("rate-limit.retry.get.key.max", "70").toLong()
        val amountMinutesRateLimit = environment.getProperty("rate-limit.remaining.after.minute", "1").toLong()

        if(!toogleIsEnabledRateLimit){
            println("Rate-Limit disabled")
            return true
        }
        val requestURL = request.requestURL
        val headerLimitUser = requestURL.substring(requestURL.lastIndexOf("/")+1)

        val proxyManagerForCache = Bucket4j.extension(JCache::class.java).proxyManagerForCache<String>(cache)

        if(
            isNotHaveBandwidths(proxyManagerForCache, headerLimitUser) &&
            (compareConfigBandwidthWithToggle(proxyManagerForCache,headerLimitUser,bucketCapacity,amountMinutesRateLimit))
        ){
            cache.remove(headerLimitUser)
        }

        val configurationLazySupplier = getConfigurationSupplier(bucketCapacity, amountMinutesRateLimit)

        val tokenBucket = proxyManagerForCache.getProxy(headerLimitUser, configurationLazySupplier)

        val probe = tokenBucket.tryConsumeAndReturnRemaining(AMOUNT_CONSUMER)

        return checkIsConsumer(probe)
    }

    private fun compareConfigBandwidthWithToggle(proxyManagerForCahe: ProxyManager<String>, headerLimitUser: String, bucketCapacity: Long, amountMinutesRateLimit: Long) : Boolean {
        if(
            proxyManagerForCahe.getProxyConfiguration(headerLimitUser).get().bandwidths[0].capacity != bucketCapacity ||
            Duration.ofNanos(proxyManagerForCahe.getProxyConfiguration(headerLimitUser).get().bandwidths[0].refillPeriodNanos).toMinutes()!= amountMinutesRateLimit
        ){
            return true
        }
        return false
    }

    private fun isNotHaveBandwidths(proxyManagerForCahe: ProxyManager<String>, headerLimitUser: String) : Boolean {
        if (
            !proxyManagerForCahe.getProxyConfiguration(headerLimitUser).isEmpty &&
            proxyManagerForCahe.getProxyConfiguration(headerLimitUser).get().bandwidths != null &&
            proxyManagerForCahe.getProxyConfiguration(headerLimitUser).get().bandwidths.isNotEmpty()
        ){
            return true
        }
        return false
    }

    private fun getConfigurationSupplier(bucketCapacity: Long, amountMinutesRateLimit: Long) = Supplier {
        Bucket4j.configurationBuilder().addLimit(returnConfigurationBandwith(bucketCapacity, amountMinutesRateLimit)).build()
    }

    private fun returnConfigurationBandwith(bucketCapacity: Long, amountMinutesRateLimit: Long): Bandwidth {
        val toInstant = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(amountMinutesRateLimit).toInstant()
        return Bandwidth.classic(bucketCapacity, Refill.intervallyAligned(bucketCapacity, Duration.ofMinutes(amountMinutesRateLimit), toInstant, false))
    }

    private fun checkIsConsumer(probe: ConsumptionProbe) =
        if (probe.isConsumed){
            true
        }else{
            throw RequestLimitException(HttpStatus.TOO_MANY_REQUESTS.value(), BUCKET_LIMIT_REQUEST_USER)
        }
}

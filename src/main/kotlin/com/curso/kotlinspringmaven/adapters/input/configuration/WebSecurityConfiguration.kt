package com.curso.kotlinspringmaven.adapters.input.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@EnableWebSecurity
@Configuration
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.httpBasic()
    }

    @Bean
    override fun userDetailsService(): UserDetailsService {
        val details = User.withDefaultPasswordEncoder()
            .username("Ederson")
            .password("12345")
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(details)
    }
}
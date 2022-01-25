package com.curso.kotlinspringmaven

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@ConfigurationPropertiesScan
class KotlinSpringMavenApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringMavenApplication>(*args)
}

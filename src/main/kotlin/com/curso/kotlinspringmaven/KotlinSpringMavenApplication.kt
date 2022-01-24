package com.curso.kotlinspringmaven

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class KotlinSpringMavenApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringMavenApplication>(*args)
}

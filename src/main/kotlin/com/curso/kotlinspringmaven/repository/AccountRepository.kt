package com.curso.kotlinspringmaven.repository

import com.curso.kotlinspringmaven.model.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long> {
}
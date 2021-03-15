package br.com.zup.core.models

import io.micronaut.context.annotation.Executable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface EmailPixRepository:CrudRepository<EmailPix,Long> {

    @Executable
    fun find(email:String?): EmailPix

    @Executable
    fun existsByEmail(email: String): Boolean

}
package br.com.zup.core.models

import io.micronaut.context.annotation.Executable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
@Repository
interface PhonePixRepository :CrudRepository<PhonePix,Long> {

    @Executable
    fun find(phone: String?): PhonePix

    @Executable
    fun existsByPhone(phone: String?):Boolean

    @Executable
    fun existsByClientId(clientId: String?):Boolean

}
package br.com.zup.core.models

import io.micronaut.context.annotation.Executable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
@Repository
interface CpfPixRepository :CrudRepository<CpfPix, Long> {

    @Executable
    fun find(cpf:String):CpfPix

    @Executable
    fun existsByCpf(cpf:String):Boolean

    @Executable
    fun existsByClientId(clientId:String):Boolean
}
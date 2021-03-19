package br.com.zup.core.models

import io.micronaut.context.annotation.Executable
import io.micronaut.context.annotation.Parameter
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface KeyWordRamdomPixRepository :CrudRepository<KeyWordRamdomPix,Long>{

    @Executable
    fun find(keyword:String):KeyWordRamdomPix

    @Executable
    fun existsByKeyword(keyword: String?):Boolean

    @Executable
    fun existsByClientId(clientId: String):Boolean

}
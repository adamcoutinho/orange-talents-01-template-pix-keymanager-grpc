package br.com.zup.core.models

import io.micronaut.context.annotation.Executable
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface KeyWordRamdomPixRepository :CrudRepository<KeyWordRamdomPix,Long>{

    @Executable
    fun find(keyword:String):KeyWordRamdomPix

}
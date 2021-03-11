package br.com.zup.core.models

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
@Repository
interface EmailPixRepository:JpaRepository<EmailPix,Long> {

}
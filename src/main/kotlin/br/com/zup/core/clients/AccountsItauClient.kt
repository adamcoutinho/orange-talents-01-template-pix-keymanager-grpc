package br.com.zup.core.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


    @Client("http://localhost:9091/api/v1")
    interface AccountsItauClient {

        @Get("/clientes/{clientId}/contas")
        fun findAccountByIdClient(@PathVariable("clientId") id:String ,@QueryValue tipo:String):HttpResponse<DataAccountResponse>

        @Get("/clientes/{clientId}")
        fun findClientByIdClient(@PathVariable("clientId") id:String): HttpResponse<DataClientResponse>
    }

data class DataClientResponse(val id:String, val nome:String, val cpf:String, val instituicao: InstituteResponse) {}
data class DataAccountResponse (val tipo:String, val instituicao:InstituteResponse, val numero:String, val agencia:String, val titular:TitularResponse){}
data class InstituteResponse (val nome:String,val ispb:String){}
data class TitularResponse(val id:String,val nome:String, val cpf:String) {}
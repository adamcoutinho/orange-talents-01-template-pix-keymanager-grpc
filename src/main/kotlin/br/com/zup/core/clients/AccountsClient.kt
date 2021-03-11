package br.com.zup.core.clients

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1")
interface AccountsClient {
    @Get("/private/contas/todas")
    fun findAllAccounts():MutableList<DataAccountResponse>
    @Get("/clientes/{clientId}/contas")
    fun findAccountByIdClient(@PathVariable("clientId") id:String):MutableList<DataAccountResponse>
    @Get("/clientes/{clientId}")
    fun findClientByIdClient(@PathVariable("clientId") id:String):MutableList<DataClientResponse>
}

class DataClientResponse(val id:String, val nome:String, val cpf:String, val instituicao: InstituteResponse) {}
class DataAccountResponse (val tipo:String,instituicao:InstituteResponse,numero:String,agencia:String,titular:titularResponse){}
class InstituteResponse (val nome:String,val ispb:String){}
class titularResponse(val id:String,val nome:String, val cpf:String) {}

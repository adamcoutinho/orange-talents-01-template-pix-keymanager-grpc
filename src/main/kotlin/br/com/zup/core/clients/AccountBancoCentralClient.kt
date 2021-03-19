package br.com.zup.core.clients
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/api/v1")
interface AccountBancoCentralClient {

    @Get("/pix/keys")
    fun findAllPixKeys():HttpResponse<MutableList<CreatePixKeyResponse>>

    @Post("/pix/keys" ) @Produces(value = [MediaType.APPLICATION_XML])
    fun registerKeyWordPix(@Body pixKeyRequest: CreatePixKeyRequest):HttpResponse<CreatePixKeyResponse>

    @Delete("/pix/keys/{key}") @Produces(value = [MediaType.APPLICATION_XML])  @Consumes(value = [MediaType.APPLICATION_XML])
    fun deleteKeyWordPix(@PathVariable key:String, @QueryValue @Body deletePixKeyRequest:  DeletePixKeyRequest):HttpResponse<DeletePixKeyResponse>
}

enum class KeyType {
    CPF,CNPJ,PHONE,EMAIL,RAMDOM
}

enum class AccountType{
    CACC,SVGS
}

data class BankAccount(val participant:String,val branch:String,val accountNumber:String, val accountType:AccountType) {}

enum class Type {
    NATURAL_PERSON ,LEGAL_PERSON
}
data class Owner(val type:Type,val name:String,val taxIdNumber:String)
data class CreatePixKeyRequest (val keyType:KeyType, val key:String,val bankAccount:BankAccount,val owner: Owner)
data class CreatePixKeyResponse (val keyType:KeyType, val key:String,val bankAccount:BankAccount?,val createdAt:String)
data class DeletePixKeyRequest(val key:String , val participant:String)
data class DeletePixKeyResponse(val key:String , val participant:String, val deletedAt:String)

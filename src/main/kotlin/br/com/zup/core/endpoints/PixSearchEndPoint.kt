package br.com.zup.core.endpoints

import br.com.zup.PixKeyWordSarchServiceGrpc
import br.com.zup.PixSearchRequest
import br.com.zup.PixSearchResponse
import br.com.zup.core.clients.AccountBancoCentralClient
import br.com.zup.core.clients.AccountsItauClient
import br.com.zup.core.services.PixSearchUniqueKeyService
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import java.time.ZoneOffset

import java.time.Instant

import java.time.LocalDateTime




@Singleton
class PixSearchEndPoint :PixKeyWordSarchServiceGrpc.PixKeyWordSarchServiceImplBase(){

    @Inject
    lateinit var itauClient:AccountsItauClient

    @Inject
    lateinit var bcbClient:AccountBancoCentralClient

    @Inject
    lateinit var service: PixSearchUniqueKeyService

    override fun findByCpf(request: PixSearchRequest, responseObserver: StreamObserver<PixSearchResponse>) {

        val cpfObject = service.findByCpf(request)

        val itauAccount  = itauClient.findAccountByIdClient(id= cpfObject.clientId ,  tipo = cpfObject.type);

        val bcbAccount =  bcbClient.findAccountByKeyWord(key = cpfObject.cpf)

     val detailResponse=   PixDetailResponse(
            key = PixKeyWordDetailResponse(
                pixId = cpfObject.internal,
                key = cpfObject.cpf,
                type = "CPF"
            ),
            client = PixClientDetailResponse(
                clientId = cpfObject.clientId,
                name = itauAccount.body()!!.titular.nome,
                cpf = itauAccount.body()!!.titular.cpf
            ) ,
            account = PixAccountDetailResponse(
                numberAccount = itauAccount.body()!!.numero,
                branch = itauAccount.body()!!. agencia,
                institute = itauAccount.body()!!.instituicao.nome,
                accountType = itauAccount.body()!!.tipo
            ),
            registered = bcbAccount.body()!!.createdAt
        )

        responseObserver.onNext(request.converter(detail = detailResponse))
        responseObserver.onCompleted()

    }

    override fun findByEmail(request: PixSearchRequest, responseObserver: StreamObserver<PixSearchResponse>) {

        val emailObject = service.findByEmail(request)

        val itauAccount  = itauClient.findAccountByIdClient(id= emailObject.clientId ,  tipo = emailObject.type);

        val bcbAccount =  bcbClient.findAccountByKeyWord(key = emailObject.email)

     val detailResponse =   PixDetailResponse(
            key = PixKeyWordDetailResponse(
                pixId = emailObject.internal,
                key = emailObject.email,
                type = "EMAIL"
            ),
            client = PixClientDetailResponse(
                clientId = emailObject.clientId,
                name = itauAccount.body()!!.titular.nome,
                cpf = itauAccount.body()!!.titular.cpf
            ) ,
            account = PixAccountDetailResponse(
                numberAccount = itauAccount.body()!!.numero,
                branch = itauAccount.body()!!. agencia,
                institute = itauAccount.body()!!.instituicao.nome,
                accountType = itauAccount.body()!!.tipo
            ),
            registered = bcbAccount.body()!!.createdAt
        )

        responseObserver.onNext(request.converter(detail = detailResponse))
        responseObserver.onCompleted()

    }

    override fun findByPhone(request: PixSearchRequest, responseObserver: StreamObserver<PixSearchResponse>) {

        val phoneObject = service.findByPhone(request)

        val itauAccount  = itauClient.findAccountByIdClient(id= phoneObject.clientId ,  tipo = phoneObject.type);

        val bcbAccount =  bcbClient.findAccountByKeyWord(key = phoneObject.phone)

      val detailResponse =  PixDetailResponse(
            key = PixKeyWordDetailResponse(
                pixId = phoneObject.internal,
                key = phoneObject.phone,
                type = "CELULAR"
            ),
            client = PixClientDetailResponse(
                clientId = phoneObject.clientId,
                name = itauAccount.body()!!.titular.nome,
                cpf = itauAccount.body()!!.titular.cpf
            ) ,
            account = PixAccountDetailResponse(
                numberAccount = itauAccount.body()!!.numero,
                branch = itauAccount.body()!!. agencia,
                institute = itauAccount.body()!!.instituicao.nome,
                accountType = itauAccount.body()!!.tipo
            ),
            registered = bcbAccount.body()!!.createdAt
        )

        responseObserver.onNext(request.converter(detail = detailResponse))
        responseObserver.onCompleted()
    }

    override fun findByRamdom(request: PixSearchRequest, responseObserver: StreamObserver<PixSearchResponse>) {
        val ramdomObject = service.findByRamdom(request)

        val itauAccount  = itauClient.findAccountByIdClient(id= ramdomObject.clientId ,  tipo = ramdomObject.type);

        val bcbAccount =  bcbClient.findAccountByKeyWord(key = ramdomObject.keyword)

        val detailResponse = PixDetailResponse(
            key = PixKeyWordDetailResponse(
                pixId = ramdomObject.internal,
                key = ramdomObject.keyword,
                type = "CHAVE_ALEATORIA"
            ),
            client = PixClientDetailResponse(
                clientId = ramdomObject.clientId,
                name = itauAccount.body()!!.titular.nome,
                cpf = itauAccount.body()!!.titular.cpf
            ) ,
            account = PixAccountDetailResponse(
                numberAccount = itauAccount.body()!!.numero,
                branch = itauAccount.body()!!. agencia,
                institute = itauAccount.body()!!.instituicao.nome,
                accountType = itauAccount.body()!!.tipo
            ),
            registered = bcbAccount.body()!!.createdAt
        )

        responseObserver.onNext(request.converter(detail = detailResponse))
        responseObserver.onCompleted()
    }

}

fun PixSearchRequest.converter(detail:PixDetailResponse): PixSearchResponse? {
    val numberTypeAccount = when {
        detail.account.accountType == "CONTA_CORRENTE" -> 0
        detail.account.accountType == "CONTA_POUPANCA" -> 1
        else -> throw RuntimeException("tipo de conta nao identificada.")
    }

    val keyType = when {
        detail.key.type == "CPF" -> 0
        detail.key.type == "CHAVE_ALEATORIA" -> 1
        detail.key.type == "CELULAR" -> 2
        detail.key.type == "EMAIL" -> 3

        else -> throw RuntimeException("nenhuma chave encontrada.")
    }
    val localDateTime = LocalDateTime.parse(detail.registered)


    val instant = localDateTime.toInstant(ZoneOffset.UTC)

    val createAt = Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()



    return PixSearchResponse
        .newBuilder()
        .setAccount(PixSearchResponse
            .newBuilder()
            .accountBuilder
            .setName(detail.client.name)
            .setCpf(detail.client.cpf)
            .setAccountType(PixSearchResponse.AccountType.forNumber(numberTypeAccount))
            .setIntitute(detail.account.institute)
            .setAgency(detail.account.branch)
            .setKeyType(PixSearchResponse.KeyType.forNumber(keyType))
            .setNumberAccount(detail.account.numberAccount)
            .setCreateAt(createAt)
            .build())
        .build()
}


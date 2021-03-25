package br.com.zup.core.services

import br.com.zup.SearchRequest
import br.com.zup.SearchResponse
import br.com.zup.core.clients.KeyType
import br.com.zup.core.models.CpfPixRepository
import br.com.zup.core.models.EmailPixRepository
import br.com.zup.core.models.KeyWordRamdomPixRepository
import br.com.zup.core.models.PhonePixRepository
import com.google.protobuf.Timestamp
import io.micronaut.data.exceptions.EmptyResultException
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientPixService {

    @Inject
    private lateinit var emailPixRepository: EmailPixRepository

    @Inject
    private lateinit var cpfPixRepository: CpfPixRepository

    @Inject
    private lateinit var phonePixRepository: PhonePixRepository

    @Inject
    private lateinit var ramdomPixRepository: KeyWordRamdomPixRepository

    fun find(request: SearchRequest): MutableList<SearchResponse.ClientPix> {
        val keys = mutableListOf<SearchResponse.ClientPix>()


        keys.let{
            try {
                it.add(this.emailPixRepository.findByClientId(clientId = request.clientId)
                    .let {
                        toClientPixReponse(timeCreateAt = it.createAt,
                            clientId = it.clientId,
                            internal = it.internal,
                            keyTypeValue = 3,
                            keyValue = it.email,
                            type = it.type)
                    })
            }catch (e:EmptyResultException){}

            try {
                it.add(this.phonePixRepository.findByClientId(clientid = request.clientId).let {
                    toClientPixReponse(timeCreateAt = it.createAt,
                        clientId = it.clientId,
                        internal = it.internal,
                        keyTypeValue = 2,
                        keyValue = it.phone,
                        type = it.type)
                })
            }catch (e:EmptyResultException){}
            try {
            it.add(this.cpfPixRepository.findByClientId(clientid = request.clientId).let {
                toClientPixReponse(timeCreateAt = it.createAt,
                    clientId = it.clientId,
                    internal = it.internal,
                    keyTypeValue = 0,
                    keyValue = it.cpf,
                    type = it.type)
            })
            }catch (e:EmptyResultException){}
            try {
            it.add(this.ramdomPixRepository.findByClientId(clientid = request.clientId).let {
                toClientPixReponse(timeCreateAt = it.createAt,
                    clientId = it.clientId,
                    internal = it.internal,
                    keyTypeValue = 1,
                    keyValue = it.keyword,
                    type = it.type)
            })
            }catch (e:EmptyResultException){}
        }
        return keys
    }
}

fun ClientPixService.toClientPixReponse(
    timeCreateAt: LocalDateTime,
    clientId: String,
    type: String,
    internal: String,
    keyTypeValue: Int,
    keyValue: String,
): SearchResponse.ClientPix {
    val instant = timeCreateAt.toInstant(ZoneOffset.UTC)
    val createAt = Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
    return SearchResponse.ClientPix.newBuilder()
        .setClientId(clientId)
        .setAccountType(type)
        .setPixId(internal)
        .setKeyType(SearchResponse.ClientPix.KeyType.forNumber(keyTypeValue))
        .setKey(keyValue)
        .setCreateAt(createAt)
        .build()
}
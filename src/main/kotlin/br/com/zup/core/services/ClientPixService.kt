package br.com.zup.core.services

import br.com.zup.SearchRequest
import br.com.zup.SearchResponse
import br.com.zup.core.models.CpfPixRepository
import br.com.zup.core.models.EmailPixRepository
import br.com.zup.core.models.KeyWordRamdomPixRepository
import br.com.zup.core.models.PhonePixRepository
import com.google.protobuf.Timestamp
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

    fun find(request: SearchRequest):MutableList<SearchResponse.ClientPix>{
         val keys   = mutableListOf<SearchResponse.ClientPix>()

      keys.add(this.emailPixRepository.findByClientId(clientId = request.clientId).let {
                 val instant = it.createAt.toInstant(ZoneOffset.UTC)
                 val createAt = Timestamp.newBuilder()
                     .setSeconds(instant.epochSecond)
                     .setNanos(instant.nano)
                     .build()
                 SearchResponse.ClientPix.newBuilder()
                     .setClientId(it.clientId)
                     .setAccountType(it.type)
                     .setPixId(it.internal)
                     .setKeyType(SearchResponse.ClientPix.KeyType.forNumber(0))
                     .setKey(it.email)
                     .setCreateAt(createAt)
                     .build()
             })


            this.phonePixRepository.findByClientId(clientid = request.clientId)

            this.cpfPixRepository.findByClientId(clientid = request.clientId)

            this.ramdomPixRepository.findByClientId(clientid = request.clientId)



        return keys

    }

}
package br.com.zup.core.services

import br.com.zup.PixSearchRequest
import br.com.zup.core.models.CpfPix
import br.com.zup.core.models.CpfPixRepository
import br.com.zup.core.models.EmailPix
import br.com.zup.core.models.EmailPixRepository
import br.com.zup.core.models.KeyWordRamdomPix
import br.com.zup.core.models.KeyWordRamdomPixRepository
import br.com.zup.core.models.PhonePix
import br.com.zup.core.models.PhonePixRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixSearchUniqueKeyService {

    @Inject
    private lateinit var emailPixRepository: EmailPixRepository

    @Inject
    private lateinit var cpfPixRepository: CpfPixRepository

    @Inject
    private lateinit var phonePixRepository: PhonePixRepository

    @Inject
    private lateinit var ramdomPixRepository: KeyWordRamdomPixRepository


    fun findByCpf(request: PixSearchRequest): CpfPix {

        when (request.filterCase) {
            PixSearchRequest.FilterCase.PIXID -> {
                return cpfPixRepository.find(clientId = request.pixId.clientId, internal = request.pixId.pixId)
            }
            PixSearchRequest.FilterCase.KEYWORD -> {
                return cpfPixRepository.find(cpf = request.keyword)
            }

            PixSearchRequest.FilterCase.FILTER_NOT_SET -> {
                throw RuntimeException("Filtro não encontrado.")
            }
        }
    }

    fun findByPhone(request: PixSearchRequest): PhonePix {
        when (request.filterCase) {

            PixSearchRequest.FilterCase.PIXID -> {
                return phonePixRepository.find(clientId = request.pixId.clientId, internal = request.pixId.pixId)
            }
            PixSearchRequest.FilterCase.KEYWORD -> {
                return phonePixRepository.find(phone = request.keyword)
            }

            PixSearchRequest.FilterCase.FILTER_NOT_SET -> {
                throw java.lang.RuntimeException("Filtro não encontrado.")
            }
        }
    }

    fun findByEmail(request: PixSearchRequest): EmailPix {
        when (request.filterCase) {
            PixSearchRequest.FilterCase.PIXID -> {
                return emailPixRepository.find(clientId = request.pixId.clientId, internal = request.pixId.pixId)
            }
            PixSearchRequest.FilterCase.KEYWORD -> {
                return emailPixRepository.find(email = request.keyword)
            }

            PixSearchRequest.FilterCase.FILTER_NOT_SET -> {
                throw java.lang.RuntimeException("Filtro não encontrado.")
            }
        }
    }

    fun findByRamdom(request: PixSearchRequest): KeyWordRamdomPix {
        when (request.filterCase) {

            PixSearchRequest.FilterCase.PIXID -> {
                return ramdomPixRepository.find(clientId = request.pixId.clientId, internal = request.pixId.pixId)
            }

            PixSearchRequest.FilterCase.KEYWORD -> {
                return ramdomPixRepository.find(keyword = request.keyword)
            }

            PixSearchRequest.FilterCase.FILTER_NOT_SET -> {
                throw java.lang.RuntimeException("Filtro não encontrado.")
            }
        }
    }

}


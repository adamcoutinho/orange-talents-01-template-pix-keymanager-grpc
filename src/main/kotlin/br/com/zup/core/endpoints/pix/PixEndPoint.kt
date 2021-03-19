package br.com.zup.core.endpoints.pix

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.builder.konstraint
import br.com.zup.PixCpfWordRequest
import br.com.zup.PixCpfWordResponse
import br.com.zup.PixEmailKeyWordRequest
import br.com.zup.PixEmailKeyWordResponse
import br.com.zup.PixKeyWordServiceGrpc
import br.com.zup.PixPhoneKeyWordRequest
import br.com.zup.PixPhoneKeyWordResponse
import br.com.zup.PixRamdomKeyWordDeleteRequest
import br.com.zup.PixRamdomKeyWordDeleteResponse
import br.com.zup.PixRamdomKeyWordRequest
import br.com.zup.PixRamdomKeyWordResponse
import br.com.zup.core.clients.AccountBancoCentralClient
import br.com.zup.core.clients.AccountType
import br.com.zup.core.clients.AccountsItauClient
import br.com.zup.core.clients.BankAccount
import br.com.zup.core.clients.CreatePixKeyRequest
import br.com.zup.core.clients.CreatePixKeyResponse
import br.com.zup.core.clients.DataAccountResponse
import br.com.zup.core.clients.DeletePixKeyRequest
import br.com.zup.core.clients.DeletePixKeyResponse
import br.com.zup.core.clients.KeyType
import br.com.zup.core.clients.Owner
import br.com.zup.core.clients.Type
import br.com.zup.core.models.CpfPix
import br.com.zup.core.models.CpfPixRepository
import br.com.zup.core.models.EmailPix
import br.com.zup.core.models.EmailPixRepository
import br.com.zup.core.models.KeyWordRamdomPix
import br.com.zup.core.models.KeyWordRamdomPixRepository
import br.com.zup.core.models.PhonePix
import br.com.zup.core.models.PhonePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixEndPoint : PixKeyWordServiceGrpc.PixKeyWordServiceImplBase() {

    private val logger = LoggerFactory.getLogger(PixEndPoint::class.java)

    @Inject
    private lateinit var phoneRepository: PhonePixRepository

    @Inject
    private lateinit var keyWordRamdomPixRepository: KeyWordRamdomPixRepository

    @Inject
    private lateinit var emailPixRepository: EmailPixRepository

    @Inject
    private lateinit var cpfPixRepository: CpfPixRepository

    @Inject
    private lateinit var accountsItauClient: AccountsItauClient

    @Inject
    private lateinit var accountsBancoCentralClient: AccountBancoCentralClient

    private val LIMIT_SIZE_KEY_WORD_RANDOM = 77

    private fun getTypeAccount(type: Number?) = when (type) {
        0 -> "CONTA_CORRENTE"
        1 -> "CONTA_POUPANCA"
        else -> throw Exception("Type Account Unknown")
    }

    private fun newRegisterKeyWordBancoCentral(
        keyPix: String,
        dataClientResponse: DataAccountResponse,
    ): HttpResponse<CreatePixKeyResponse> {
        return this.accountsBancoCentralClient.registerKeyWordPix(
            CreatePixKeyRequest(
                keyType = KeyType.CPF,
                key = keyPix,
                bankAccount = BankAccount(
                    participant = dataClientResponse.instituicao.ispb,
                    branch = dataClientResponse.agencia,
                    accountNumber = dataClientResponse.numero,
                    AccountType.CACC
                ),
                owner = Owner(
                    type = Type.NATURAL_PERSON,
                    name = dataClientResponse.titular.nome,
                    taxIdNumber = dataClientResponse.titular.cpf
                )
            )
        )
    }

    private fun removeKeyWordBancoCentral(keyPix: String, ispb: String): HttpResponse<DeletePixKeyResponse> {
        return this.accountsBancoCentralClient.deleteKeyWordPix(
            key = keyPix,
            DeletePixKeyRequest(
                key = keyPix,
                participant = ispb
            ))
    }

    override fun ramdomKeyWordRegister(request: PixRamdomKeyWordRequest?,responseObserver: StreamObserver<PixRamdomKeyWordResponse>?) {
        try {

            val keyWordRamdom = UUID.randomUUID().toString()

            if (keyWordRamdom.length <= LIMIT_SIZE_KEY_WORD_RANDOM) {

                val accountCLient = this.accountsItauClient.findAccountByIdClient(request!!.idInternal,
                    getTypeAccount(request.type.number))

                val keyWordRamdomObject = KeyWordRamdomPix(
                    ispb = accountCLient.body()!!.instituicao.ispb,
                    keyword = keyWordRamdom,
                    clientId = accountCLient.body()!!.titular.id,
                    type = accountCLient.body()!!.tipo
                )

                val either = ValidatorBuilder.of<KeyWordRamdomPix>()
                    .konstraint(KeyWordRamdomPix::keyword)
                    {
                        notBlank().message("informe a chave.")
                    }
                    .konstraint(KeyWordRamdomPix::clientId)
                    {
                        notBlank().message("informe código do titular da conta.")
                    }
                    .konstraint(KeyWordRamdomPix::type)
                    {
                        notBlank().message("inform  tipo da conta.")
                    }
                    .build()
                    .validateToEither(keyWordRamdomObject)

                val existIdInternal = this.keyWordRamdomPixRepository.existsByClientId(clientId = request.idInternal)

                when {
                    existIdInternal -> {
                        val e = Status
                            .INVALID_ARGUMENT
                            .withDescription("Já existe uma chave aleatórica cadastrada.")
                            .asRuntimeException()
                        responseObserver!!.onError(e)
                    }
                    either.isLeft -> {
                        val e = Status
                            .INVALID_ARGUMENT
                            .withDescription(either.left().get()[0].message())
                            .asRuntimeException()
                        responseObserver!!.onError(e)
                    }

                    either.isRight -> {

                        newRegisterKeyWordBancoCentral(keyWordRamdomObject.keyword, accountCLient.body()!!)

                        this.keyWordRamdomPixRepository.save(keyWordRamdomObject)

                        val keyWordRamdomResponse = PixRamdomKeyWordResponse
                            .newBuilder()
                            .setMessage(keyWordRamdomObject.keyword)
                            .build()

                        responseObserver!!.onNext(keyWordRamdomResponse)

                        responseObserver.onCompleted()

                    }
                }

                logger.info("{KEY_WORD_RAMDOM} -> GENERATED")

            } else {
                val e = Status
                    .INVALID_ARGUMENT
                    .withDescription("TAMANHO DA CHAVE É MENOR QUE 77 CARACTERES.")
                    .asRuntimeException()
                responseObserver!!.onError(e)

            }
        } catch (e: HttpClientException) {
            val errorClientHttp = Status
                .INTERNAL
                .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
                .asRuntimeException()
            responseObserver!!.onError(errorClientHttp)
            logger.error("${e.message}")
        } catch (e: HttpClientResponseException) {
            val errorGrpc = Status
                .INTERNAL
                .withDescription(e.localizedMessage)
                .asRuntimeException()
            responseObserver!!.onError(errorGrpc)
        } catch (e: Exception) {
            val errorGrpc = Status
                .NOT_FOUND
                .withDescription("Erro Inesperado.")
                .asRuntimeException()
            println(e.localizedMessage)
            responseObserver!!.onError(errorGrpc)
        }

    }


    override fun ramdomKeyWordRemove(request: PixRamdomKeyWordDeleteRequest?,responseObserver: StreamObserver<PixRamdomKeyWordDeleteResponse>? ) {

        var existError = false

        if (request!!.keyWord.length > LIMIT_SIZE_KEY_WORD_RANDOM) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("TAMANHO DA CHAVE É MENOR QUE 77 CARACTERES.")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }

        val existKeyWordRamdomPix = this.keyWordRamdomPixRepository.existsByKeyword(keyword = request.keyWord)

        if (existKeyWordRamdomPix.not()) {
            val e = Status
                .NOT_FOUND
                .withDescription("CHAVE ALEATÓRIA JÁ FOI REMOVIDO")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }

        if (existError.not()) {
            val keyWordRamdomPix = this.keyWordRamdomPixRepository.find(keyword = request.keyWord)

            removeKeyWordBancoCentral(keyPix = keyWordRamdomPix.keyword, ispb = keyWordRamdomPix.ispb)

            keyWordRamdomPixRepository.delete(keyWordRamdomPix)

            val keyWordDeleteResponse = PixRamdomKeyWordDeleteResponse
                .newBuilder()
                .setMessage("KEY_WORD_RAMDOM_DELETED")
                .build()
            responseObserver!!.onNext(keyWordDeleteResponse)
            responseObserver.onCompleted()
        }

    }

    override fun cpfKeyWordRegister(request: PixCpfWordRequest?, responseObserver: StreamObserver<PixCpfWordResponse>? ) {
        try{
        var existError = validationCpfKeyWord(request, responseObserver)

        val existCpf = cpfPixRepository.existsByCpf(cpf = request!!.cpfKeyWord)

        if (existCpf) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("cpf já existe.")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }

        if (existError.not()) {

            val accountClient =
                this.accountsItauClient.findAccountByIdClient(request.idInternal, getTypeAccount(request.type.number))

            val cpfPixObject = CpfPix(cpf = request.cpfKeyWord,
                clientId = accountClient.body()!!.titular.id,
                type = getTypeAccount(request.type.number),
                ispb = accountClient.body()!!.instituicao.ispb)

            val either = ValidatorBuilder.of<CpfPix>()
                .konstraint(CpfPix::clientId)
                {
                    notBlank().message("inform id titular para o registro da chave.")
                }
                .konstraint(CpfPix::cpf)
                {
                    notBlank().message("informe o cpf para registro da chave.")
                }
                .konstraint(CpfPix::type)
                {
                    notBlank().message("informe o tipo de conta.")
                }
                .konstraint(CpfPix::ispb)
                {
                    notBlank().message("informe o ispb")
                }
                .build().validateToEither(cpfPixObject)

            val existCpfPix = this.cpfPixRepository.existsByClientId(accountClient.body()!!.titular.id)

                when {

                    existCpfPix -> {
                        val e = Status
                            .INVALID_ARGUMENT
                            .withDescription("Já existe uma chave cadastrada.")
                            .asRuntimeException()
                        responseObserver!!.onError(e)
                    }

                    either.isLeft -> {
                        val e = Status
                            .INVALID_ARGUMENT
                            .withDescription(either.left().get()[0].message())
                            .asRuntimeException()
                        responseObserver!!.onError(e)
                    }
                    either.isRight -> {

                            newRegisterKeyWordBancoCentral(keyPix = cpfPixObject.cpf,
                                dataClientResponse = accountClient.body()!!)

                            cpfPixRepository.save(cpfPixObject)

                            val cpfWordResponse = PixCpfWordResponse.newBuilder()
                                .setMessage(cpfPixObject.cpf)
                                .build()

                            responseObserver!!.onNext(cpfWordResponse)

                            responseObserver.onCompleted()

                            logger.info("{KEY_WORD_CPF} -> GENERATED")

                    }
                }
            }
        } catch (e: HttpClientException) {
            val errorClientHttp = Status
                .INTERNAL
                .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
                .asRuntimeException()
            responseObserver!!.onError(errorClientHttp)
            logger.error("${e.message}")
        } catch (e: HttpClientResponseException) {
            val errorGrpc = Status
                .INTERNAL
                .withDescription(e.localizedMessage)
                .asRuntimeException()
            responseObserver!!.onError(errorGrpc)
        } catch (e: Exception) {
            val errorGrpc = Status
                .NOT_FOUND
                .withDescription("Erro Inesperado.")
                .asRuntimeException()
            println(e.localizedMessage)
            responseObserver!!.onError(errorGrpc)
        }
    }

    override fun cpfKeyWordRemove(request: PixCpfWordRequest?, responseObserver: StreamObserver<PixCpfWordResponse>?) {
        var existError = validationCpfKeyWord(request, responseObserver)

        val existCpf = cpfPixRepository.existsByCpf(cpf = request!!.cpfKeyWord)

        if (existCpf.not()) {
            val e = Status
                .NOT_FOUND
                .withDescription("CPF JÁ FOI REMOVIDO.")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }

        if (existError.not()) {
            try {
                val cpfObject = cpfPixRepository.find(cpf = request.cpfKeyWord)

                removeKeyWordBancoCentral(cpfObject.cpf, cpfObject.ispb)

                cpfPixRepository.delete(cpfObject)

                val cpfResponse = PixCpfWordResponse
                    .newBuilder()
                    .setMessage("CPF_KEY_WORD_DELETED")
                    .build()

                responseObserver!!.onNext(cpfResponse)
                responseObserver.onCompleted()
            } catch (e: HttpClientResponseException) {
                val errorClientHttp = Status
                    .NOT_FOUND
                    .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
                    .asRuntimeException()
                responseObserver!!.onError(errorClientHttp)
                logger.error("${e.message}")
            }

        }
    }

    private fun validationCpfKeyWord(
        request: PixCpfWordRequest?,
        responseObserver: StreamObserver<PixCpfWordResponse>?,
    ): Boolean {
        var existError = false

        if (request!!.cpfKeyWord!!.isBlank()) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um cpf.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        if (request.cpfKeyWord.matches("^[0-9]{11}\$".toRegex()).not()) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um cpf válido.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        return existError
    }

    override fun phoneKeyWordRegister(request: PixPhoneKeyWordRequest?,responseObserver: StreamObserver<PixPhoneKeyWordResponse>?) {
    try {
        var existError = validationPhoneKeyWord(request, responseObserver)

        val existPhone = phoneRepository.existsByPhone(phone = request!!.phoneKeyWord)

        if (existPhone) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Número de contato já cadastrado.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        if (existError.not()) {

            val accountClient =
                this.accountsItauClient.findAccountByIdClient(request.idInternal, getTypeAccount(request.type.number))

            val phoneObject = PhonePix(phone = request.phoneKeyWord,
                clientId = accountClient.body()!!.titular.id,
                type = getTypeAccount(request.type.number),
                ispb = accountClient.body()!!.instituicao.ispb
            )


            val either = ValidatorBuilder.of<PhonePix>()
                .konstraint(PhonePix::clientId) {
                    notBlank().message("informe id do titular")
                }
                .konstraint(PhonePix::phone) {
                    notBlank().message("informe o número de contato para gerar a chave")
                }
                .konstraint(PhonePix::type) {
                    notBlank().message("informe o tipo de conta.")
                }
                .build()
                .validateToEither(phoneObject)

            val existNumberContactPix =
                this.phoneRepository.existsByClientId(clientId = accountClient.body()!!.titular.id)

            when {
                existNumberContactPix -> {
                    val e = Status
                        .INVALID_ARGUMENT
                        .withDescription("já existe uma chave pix cadastrada!")
                        .asRuntimeException()
                    responseObserver!!.onError(e)
                }
                either.isLeft -> {
                    val e = Status
                        .INVALID_ARGUMENT
                        .withDescription(either.left().get()[0].message())
                        .asRuntimeException()
                    responseObserver!!.onError(e)
                }
                either.isRight -> {

                        newRegisterKeyWordBancoCentral(keyPix = phoneObject.phone,
                            dataClientResponse = accountClient.body()!!)

                        this.phoneRepository.save(phoneObject)

                        val phoneKeyResponse = PixPhoneKeyWordResponse
                            .newBuilder()
                            .setMessage("${phoneObject.id}")
                            .build()

                        responseObserver!!.onNext(phoneKeyResponse)
                        responseObserver.onCompleted()
                        logger.info("{KEY_WORD_PHONE} -> GENERATED")
                }
            }
        }
    } catch (e: HttpClientException) {
        val errorClientHttp = Status
            .INTERNAL
            .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
            .asRuntimeException()
        responseObserver!!.onError(errorClientHttp)
        logger.error("${e.message}")
    } catch (e: HttpClientResponseException) {
        val errorGrpc = Status
            .INTERNAL
            .withDescription(e.localizedMessage)
            .asRuntimeException()
        responseObserver!!.onError(errorGrpc)
    } catch (e: Exception) {
        val errorGrpc = Status
            .NOT_FOUND
            .withDescription("Erro Inesperado.")
            .asRuntimeException()
        println(e.localizedMessage)
        responseObserver!!.onError(errorGrpc)
    }
    }

    override fun phoneKeyWordRemove(request: PixPhoneKeyWordRequest?, responseObserver: StreamObserver<PixPhoneKeyWordResponse>? ) {

        var existError = validationPhoneKeyWord(request, responseObserver)

        val existPhone = phoneRepository.existsByPhone(phone = request!!.phoneKeyWord)
        if (existPhone.not()) {
            val e = Status
                .NOT_FOUND
                .withDescription("NÚMERO DE CONTATO JÁ FOI REMOVIDO.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        if (existError.not()) {
            try {
                val phoneObject = phoneRepository.find(phone = request.phoneKeyWord)

                removeKeyWordBancoCentral(keyPix = phoneObject.phone, ispb = phoneObject.ispb)

                phoneRepository.delete(phoneObject)

                val phoneResponse = PixPhoneKeyWordResponse.newBuilder().setMessage("PHONE_KEY_WORD_DELETED").build()

                responseObserver!!.onNext(phoneResponse)

                responseObserver.onCompleted()
            } catch (e: HttpClientResponseException) {
                val errorClientHttp = Status
                    .NOT_FOUND
                    .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
                    .asRuntimeException()
                responseObserver!!.onError(errorClientHttp)
                logger.error("${e.message}")
            }
        }
    }

    private fun validationPhoneKeyWord(request: PixPhoneKeyWordRequest?,responseObserver: StreamObserver<PixPhoneKeyWordResponse>?): Boolean {

        var existError = false

        if (request!!.phoneKeyWord.isBlank()) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe o número de contato.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        if (request.phoneKeyWord.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex()).not()) {

            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe o número de contato valido.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        return existError
    }

    override fun emailKeyWordRegister(request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>? ) {
        try {
            var existError = validationEmailKeyWord(request, responseObserver)

            val existEmail = emailPixRepository.existsByEmail(email = request!!.emailKeyWord)

            if (existEmail) {
                val e = Status
                    .INVALID_ARGUMENT
                    .withDescription("Email já foi cadastrado.")
                    .asRuntimeException()
                responseObserver!!.onError(e)

                existError = true
            }

        if (existError.not()) {
            val accountClient =
                accountsItauClient.findAccountByIdClient(request.idInternal, getTypeAccount(request.type.number))

            val emailObject = EmailPix(email = request.emailKeyWord.toString(),
                clientId = accountClient.body()!!.titular.id,
                getTypeAccount(request.type.number),
                ispb = accountClient.body()!!.instituicao.ispb
            )


            val either = ValidatorBuilder.of<EmailPix>()
                .konstraint(EmailPix::clientId) {
                    notBlank().message("informe id do titular.")
                }
                .konstraint(EmailPix::email)
                {
                    notBlank().message("informe um email valido para o registro da chave")
                }
                .konstraint(EmailPix::type)
                {
                    notBlank().message("informe o tipo de conta.")
                }
                .konstraint(EmailPix::ispb)
                {
                    notBlank().message("informe o ispb")
                }
                .build()
                .validateToEither(emailObject)

            val existEmailPix = this.emailPixRepository.existsByClientId(accountClient.body()!!.titular.id)

            when {
                existEmailPix -> {
                    val e = Status
                        .INVALID_ARGUMENT
                        .withDescription("já existe uma chave cadastrada.")
                        .asRuntimeException()
                    responseObserver!!.onError(e)
                }
                either.isLeft -> {
                    val e = Status
                        .INVALID_ARGUMENT
                        .withDescription("Email já foi cadastrado.")
                        .asRuntimeException()
                    responseObserver!!.onError(e)
                }
                either.isRight -> {

                        newRegisterKeyWordBancoCentral(keyPix = emailObject.email,
                            dataClientResponse = accountClient.body()!!)

                        this.emailPixRepository.save(emailObject)

                        val emailKeyWordResponse = PixEmailKeyWordResponse
                            .newBuilder()
                            .setMessage("${emailObject.id}")
                            .build()

                        responseObserver!!.onNext(emailKeyWordResponse)

                        responseObserver.onCompleted()

                        logger.info("{KEY_WORD_EMAIL} -> GENERATED")

                }
            }
        }
    } catch (e: HttpClientException) {
        val errorClientHttp = Status
            .INTERNAL
            .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
            .asRuntimeException()
        responseObserver!!.onError(errorClientHttp)
        logger.error("${e.message}")
    } catch (e: HttpClientResponseException) {
        val errorGrpc = Status
            .INTERNAL
            .withDescription(e.localizedMessage)
            .asRuntimeException()
        responseObserver!!.onError(errorGrpc)
    } catch (e: Exception) {
        val errorGrpc = Status
            .NOT_FOUND
            .withDescription("Erro Inesperado.")
            .asRuntimeException()
        println(e.localizedMessage)
        responseObserver!!.onError(errorGrpc)
    }
    }

    override fun emailKeyWordRemove(request: PixEmailKeyWordRequest?,responseObserver: StreamObserver<PixEmailKeyWordResponse>?) {

        var existError = validationEmailKeyWord(request, responseObserver)

        val existEmail = emailPixRepository.existsByEmail(email = request!!.emailKeyWord)

        if (existEmail.not()) {
            val e = Status
                .NOT_FOUND
                .withDescription("EMAIL JÁ FOI REMOVIDO.")
                .asRuntimeException()
            responseObserver!!.onError(e)

            existError = true
        }


        if (existError.not()) {

            try {
                val emailObject = this.emailPixRepository.find(email = request.emailKeyWord)

                removeKeyWordBancoCentral(keyPix = emailObject.email, ispb = emailObject.ispb)

                this.emailPixRepository.delete(emailObject)

                val emailKeyWordResponse = PixEmailKeyWordResponse
                    .newBuilder()
                    .setMessage("EMAIL_KEY_WORD_DELETED")
                    .build()

                responseObserver!!.onNext(emailKeyWordResponse)
                responseObserver.onCompleted()
            } catch (e: HttpClientResponseException) {
                val errorClientHttp = Status
                    .NOT_FOUND
                    .withDescription("Houve Um erro inesperado por favor aguarde uns minutos e tente novamente. ")
                    .asRuntimeException()
                responseObserver!!.onError(errorClientHttp)
                logger.error("${e.message}")
            }
        }

    }

    private fun validationEmailKeyWord(request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>?): Boolean {
        var existError = false
        if (request!!.emailKeyWord.isBlank()) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um email.")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }

        if (request.emailKeyWord.matches("^[a-zA-Z0-9_!#\$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$".toRegex()).not()) {

            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um email valido.")
                .asRuntimeException()
            responseObserver!!.onError(e)
            existError = true
        }
        return existError
    }


}
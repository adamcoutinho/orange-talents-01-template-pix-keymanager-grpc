package br.com.zup.core.endpoints.pix

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
    private lateinit var emailPixRepository: EmailPixRepository;

    @Inject
    private lateinit var cpfPixRepository: CpfPixRepository;

    private val LIMIT_SIZE_KEY_WORD_RAMDOM: Int = 77

    override fun ramdomKeyWordRegister(request: PixRamdomKeyWordRequest?, responseObserver: StreamObserver<PixRamdomKeyWordResponse>?, ) {
        var existError: Boolean = false

        val keyWordRamdom = KeyWordRamdomPix(keyword = UUID.randomUUID().toString())

        if (keyWordRamdom.keyword.length <= LIMIT_SIZE_KEY_WORD_RAMDOM) {
            this.keyWordRamdomPixRepository.save(keyWordRamdom)

            val keyWordRamdomResponse = PixRamdomKeyWordResponse
                .newBuilder()
                .setMessage(keyWordRamdom.keyword)
                .build()
            responseObserver!!.onNext(keyWordRamdomResponse)

            responseObserver.onCompleted()

            logger.info("{KEY_WORD_RAMDOM} -> GENERATED")

        } else {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("TAMANHO DA CHAVE É MENOR QUE 77 CARACTERES")
                .asRuntimeException()
            responseObserver!!.onError(e)
            responseObserver.onCompleted()
        }

    }

    override fun ramdomKeyWordRemove(request: PixRamdomKeyWordDeleteRequest?,responseObserver: StreamObserver<PixRamdomKeyWordDeleteResponse>?,  ) {

        var existError: Boolean = false

        if (request!!.keyWord.length == LIMIT_SIZE_KEY_WORD_RAMDOM) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("TAMANHO DA CHAVE É MENOR QUE 77 CARACTERES")
                .asRuntimeException()
            responseObserver!!.onError(e)
            responseObserver.onCompleted()
            existError = true
        }

        if (existError.not()) {
            val keyWordRamdomPix = this.keyWordRamdomPixRepository.find(keyword = request.keyWord)

            keyWordRamdomPixRepository.delete(keyWordRamdomPix)

            val keyWordDeleteResponse = PixRamdomKeyWordDeleteResponse
                .newBuilder()
                .setMessage("keyword-ramdom deleted response")
                .build()
            responseObserver!!.onNext(keyWordDeleteResponse)
            responseObserver.onCompleted()
        }

    }

    override fun cpfKeyWordRegister(request: PixCpfWordRequest?,responseObserver: StreamObserver<PixCpfWordResponse>?, ) {

        val existError = validationCpfKeyWord(request, responseObserver)

        if (existError.not()) {
            val cpfPix = CpfPix(cpf = request!!.cpfKeyWord)

            cpfPixRepository.save(cpfPix)

            val cpfWordResponse = PixCpfWordResponse.newBuilder()
                .setMessage(cpfPix.cpf)
                .build();

            responseObserver!!.onNext(cpfWordResponse)

            responseObserver.onCompleted();

            logger.info("{KEY_WORD_CPF} -> GENERATED")

        }
    }

    override fun cpfKeyWordRemove(request: PixCpfWordRequest?, responseObserver: StreamObserver<PixCpfWordResponse>?) {
        val existError = validationCpfKeyWord(request, responseObserver)
        if (existError.not()) {
            val cpfObject = cpfPixRepository.find(cpf = request!!.cpfKeyWord)

            cpfPixRepository.delete(cpfObject)

            val cpfResponse = PixCpfWordResponse
                .newBuilder()
                .setMessage("CPF_KEY_WORD_DELETED")
                .build()

            responseObserver!!.onNext(cpfResponse)
            responseObserver.onCompleted();
        }
    }

    private fun validationCpfKeyWord(request: PixCpfWordRequest?,responseObserver: StreamObserver<PixCpfWordResponse>?, ): Boolean {
        var existError: Boolean = false

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
                .withDescription("Informe um email valido.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }

        return existError
    }

    override fun phoneKeyWordRegister(request: PixPhoneKeyWordRequest?,responseObserver: StreamObserver<PixPhoneKeyWordResponse>?, ) {
        val existError = validationPhoneKeyWord(request, responseObserver)

        if (existError.not()) {
            val phoneObject = PhonePix(phone = request!!.phoneKeyWord)

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


    override fun phoneKeyWordRemove(request: PixPhoneKeyWordRequest?, responseObserver: StreamObserver<PixPhoneKeyWordResponse>?,  ) {

        val existError = validationPhoneKeyWord(request, responseObserver)

        if (existError.not()) {
            val phoneObject = phoneRepository.find(phone = request!!.phoneKeyWord)

            phoneRepository.delete(phoneObject)

            val phoneResponse = PixPhoneKeyWordResponse.newBuilder().setMessage("PHONE_KEY_WORD_DELETED").build()

            responseObserver!!.onNext(phoneResponse)

            responseObserver.onCompleted()
        }
    }

    private fun validationPhoneKeyWord(request: PixPhoneKeyWordRequest?, responseObserver: StreamObserver<PixPhoneKeyWordResponse>?,): Boolean {

        var existError: Boolean = false

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

    override fun emailKeyWordRegister(request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>?,  ) {

        var existError = validationEmailKeyWord(request, responseObserver)

        if (existError.not()) {
            val emailObject = EmailPix(email = request!!.emailKeyWord.toString())




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

    override fun emailKeyWordRemove(request: PixEmailKeyWordRequest?,responseObserver: StreamObserver<PixEmailKeyWordResponse>?,
    ) {

        val existError = validationEmailKeyWord(request, responseObserver)

        if (existError.not()) {

            val emailObject = this.emailPixRepository.find(email = request!!.emailKeyWord)

            this.emailPixRepository.delete(emailObject)

            val emailKeyWordResponse = PixEmailKeyWordResponse
                .newBuilder()
                .setMessage("EMAIL_KEY_WORD_DELETED")
                .build()

            responseObserver!!.onNext(emailKeyWordResponse)
            responseObserver.onCompleted()
        }


    }

    private fun validationEmailKeyWord( request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>?, ): Boolean {
        var existError: Boolean = false
        if (request!!.emailKeyWord.isBlank()) {
            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um email.")
                .asRuntimeException()
            responseObserver?.onError(e)

            existError = true
        }

        if (request.emailKeyWord.matches("^[a-zA-Z0-9_!#\$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$".toRegex()).not()) {

            val e = Status
                .INVALID_ARGUMENT
                .withDescription("Informe um email valido.")
                .asRuntimeException()
            responseObserver?.onError(e)
            existError = true
        }
        return existError
    }


}
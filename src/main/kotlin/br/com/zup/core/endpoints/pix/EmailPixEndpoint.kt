package br.com.zup.core.endpoints.pix

import br.com.zup.PixEmailKeyWordRequest
import br.com.zup.PixEmailKeyWordResponse
import br.com.zup.PixKeyWordServiceGrpc
import br.com.zup.core.models.EmailPix
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class EmailPixEndpoint() :PixKeyWordServiceGrpc.PixKeyWordServiceImplBase(){

    private val logger = LoggerFactory.getLogger(EmailPixEndpoint::class.java)

    override fun emailKeyWordRegister(request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>?,    ) :PixEmailKeyWordResponse{


//        val emailObject:EmailPix = EmailPix(email = request?.emailKeyWord.toString())

        println(request?.emailKeyWord)
            PixEmailKeyWordResponse
                .newBuilder()
                .setMessage("${request?.emailKeyWord}")
                .build()




//        this.emailRepository.save(emailObject)


        responseObserver?.onCompleted()

        return PixEmailKeyWordResponse.getDefaultInstance()
    }

    override fun emailKeyWordRemove(request: PixEmailKeyWordRequest?, responseObserver: StreamObserver<PixEmailKeyWordResponse>?, ) {
        val emailObject:EmailPix = EmailPix(email = request?.emailKeyWord.toString())

//        this.emailRepository.save(emailObject)
    }
}
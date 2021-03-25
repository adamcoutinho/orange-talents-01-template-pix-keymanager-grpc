package br.com.zup.core.endpoints

import br.com.zup.FindAllPixKeyServiceGrpc
import br.com.zup.SearchRequest
import br.com.zup.SearchResponse
import br.com.zup.core.services.ClientPixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PixAllKeysSearchEndPoint :FindAllPixKeyServiceGrpc.FindAllPixKeyServiceImplBase() {

    @Inject
    lateinit var service:ClientPixService

    override fun findAll(request: SearchRequest, responseObserver: StreamObserver<SearchResponse>) {

        val keys =  SearchResponse.newBuilder()
            .addAllClientPix(service.find(request))
            .build()
        responseObserver.onNext(keys)
        responseObserver.onCompleted()

    }
}


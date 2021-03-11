package br.com.zup.core.endpoints.pix

import br.com.zup.PixCpfWordRequest
import br.com.zup.PixCpfWordResponse
import br.com.zup.PixKeyWordServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class CpfPixEndPoint : PixKeyWordServiceGrpc.PixKeyWordServiceImplBase() {

    override fun cpfKeyWordRegister(request: PixCpfWordRequest?, responseObserver: StreamObserver<PixCpfWordResponse>?, ) {

    }

    override fun cpfKeyWordRemove(request: PixCpfWordRequest?, responseObserver: StreamObserver<PixCpfWordResponse>?) {

    }
}
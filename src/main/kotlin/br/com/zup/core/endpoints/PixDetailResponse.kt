package br.com.zup.core.endpoints

data class PixDetailResponse(val key: PixKeyWordDetailResponse,val client:PixClientDetailResponse,val account:PixAccountDetailResponse,  val registered:String) {}
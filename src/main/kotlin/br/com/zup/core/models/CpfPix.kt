package br.com.zup.core.models

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "cpf_pix")
class CpfPix (@field:NotBlank(message = "informe um cpf")  val cpf:String,  val clientId:String, val type:String,val ispb:String) :Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_cpf_pix")
    @SequenceGenerator(name = "sequence_cpf_pix", sequenceName = "sq_cpf_pix")
    var id: Long? = null

    var internal:String  = UUID.randomUUID().toString()

    var createAt:LocalDateTime  = LocalDateTime.now()
}

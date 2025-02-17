package br.com.zup.core.models

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
@Table(name = "phone_pix")
class PhonePix(
    @field:NotBlank(message = "informe o telefone de contato.") @field:Size(min = 14,
        max = 14,
        message = "informe o telefone.") val phone: String,
    val clientId: String,
    val type: String,
    val ispb: String
) : Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_phone_pix")
    @SequenceGenerator(name = "sequence_phone_pix", sequenceName = "sq_phone_pix")
    var id: Long? = null

    var internal:String  = UUID.randomUUID().toString()

    var createAt: LocalDateTime = LocalDateTime.now()

}

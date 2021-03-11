package br.com.zup.core.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "cpf_pix")
class CpfPix (@field:NotBlank(message = "informe um cpf")  val cpf:String) :Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_cpf_pix")
    @SequenceGenerator(name = "sequence_cpf_pix", sequenceName = "sq_cpf_pix")
    var id: Long? = null
}

package br.com.zup.core.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
@Table(name = "key_word_ramdom_pix")
class KeyWordRamdomPix(
    @field:NotBlank(message = "informe a chave ramdômica")
    @field:Size(max = 77, message = "tamanho máximo da chave é 77 caracateres.")
    val keyword: String,
    val type: String,
    val idInternal: String,
) : Pix {



    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_key_word_ramdom_pix")
    @SequenceGenerator(name = "sequence_key_word_ramdom_pix", sequenceName = "sq_key_word_ramdom_pix")
    var id: Long? = null
}
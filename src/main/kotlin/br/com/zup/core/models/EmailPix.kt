package br.com.zup.core.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.Size


@Entity
@Table(name = "email_pix")
class EmailPix (@field:Email(message = "informe um email valido.") @field:Size(max = 77,message = "número máximo de caracteres é 77." ) val email:String ):Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_email_pix")
    @SequenceGenerator(name = "sequence_email_pix", sequenceName = "sq_email_pix")
    var id: Long? = null

}

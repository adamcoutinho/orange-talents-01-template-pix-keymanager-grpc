package br.com.zup.core.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.Email


@Entity
@Table(name = "email_pix")
class EmailPix (@field:Email(message = "informe um email valido.") val email:String ):Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_email_pix")
    @SequenceGenerator(name = "sequence_email_pix", sequenceName = "sq_email_pix")
    var id: Long? = null

}

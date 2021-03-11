package br.com.zup.core.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "phone_pix")
class PhonePix (val phone:String):Pix {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_phone_pix")
    @SequenceGenerator(name = "sequence_phone_pix", sequenceName = "sq_phone_pix")
    var id: Long? = null

}

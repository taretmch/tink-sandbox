/* In Progress
import com.google.crypto.tink.KeyTypeManager
import com.google.crypto.tink.proto.HmacKey

case class CustomKeyManager extends KeyTypeManager[HmacKey] () {

}

object CustomKeyManager {
  def apply() = {
    super(
      classOf(HmacKey),
      new PrimitiveFactory[Mac, HmacKey](classOf(HmacKey)) {
        

      })
  }
}
*/

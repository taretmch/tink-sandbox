import org.keyczar._
import org.keyczar.enums._

import org.apache.commons.codec.binary.{Hex, StringUtils}
import org.apache.commons.codec.digest.DigestUtils

import ixias.security._

object SignerSample {
  def main(args: Array[String]): Unit = {
    // 秘密鍵の設定
    val secret = "secret-key"
    val secretDigest = DigestUtils.sha256(secret)
    val key = new HmacKey(secretDigest)
    val keyMetadata = new KeyMetadata(
      "HMAC test",
      KeyPurpose.SIGN_AND_VERIFY,
      DefaultKeyType.HMAC_SHA1
    )
    val keyVersion = new KeyVersion(0, KeyStatus.PRIMARY, false)
    keyMetadata.addVersion(keyVersion)
    val reader = new KeyReader(keyMetadata, IndexedSeq(key))
    // 秘密鍵の設定から Signer を生成する
    val signer = new Signer(reader)
    
    // 署名するメッセージ
    val message = "This is a pen."
    // 秘密鍵を用いて生成したメッセージの署名 (16進数)
    val signature = signer.sign(StringUtils.getBytesUsAscii(message))
    println("message: " + message)
    println("0xsignature: " + signature)
    println("signature: " + Hex.encodeHex(signature))
  }
}

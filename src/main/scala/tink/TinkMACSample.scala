import com.google.crypto.tink.Mac
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.mac.{ MacConfig, MacKeyTemplates, HmacKeyManager }

import com.google.protobuf.ByteString;

import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.OutputPrefixType;

import org.apache.commons.codec.binary.{Hex, StringUtils}
import org.apache.commons.codec.digest.DigestUtils

object TinkMACSample {
  def main(args: Array[String]): Unit = {
    // MAC プリミティブの初期化
    // 全ての MAC key type を登録する
    // MacConfig.register()
    // この中では
    //   HmacKeyManager.register(true)
    //   AesCmacKeyManager.register(true)
    //   MacWrapper.register()
    // が呼び出されている。
    MacConfig.register()
    // KeysetHandle の生成
    val handle: KeysetHandle = KeysetHandle.generateNew(MacKeyTemplates.HMAC_SHA256_128BITTAG)
    // MAC プリミティブの取得
    val mac: Mac = handle.getPrimitive(classOf[Mac])
    // メッセージの署名
    val message = "This is a pen."
    val signature = mac.computeMac(StringUtils.getBytesUsAscii(message))
    println("message: " + message)
    println("signature: " + signature)
  }
}

package tink.sandbox.stdlib

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import java.security.SecureRandom
import scala.util.Try

object TokenAuthenticator {
  val HMAC_ALGO = "HmacSHA256"
  val CHARSET = "UTF-8"

  // 署名関数
  def sign(message: String, secret: String): String = {
    val salt = generateSalt()
    val signature = hmacSha256(message + salt, secret)
    val combinedSigSalt = signature ++ salt.getBytes(CHARSET)
    Base64.getEncoder.encodeToString(combinedSigSalt) + "$" + message
  }

  // 検証関数
  def verify(signedToken: String, secret: String): Boolean = {
    val parts = signedToken.split("\\$", 2)
    if (parts.length < 2) return false

    val combinedSigSalt = Base64.getDecoder.decode(parts(0))
    val signatureLength = 32 // SHA-256 outputs 32 bytes
    if (combinedSigSalt.length <= signatureLength) return false

    val signature = combinedSigSalt.slice(0, signatureLength)
    val salt = new String(combinedSigSalt.slice(signatureLength, combinedSigSalt.length), CHARSET)

    val correctSig = hmacSha256(parts(1) + salt, secret)

    // 署名のタイムリーな比較
    java.security.MessageDigest.isEqual(signature, correctSig)
  }

  // HMAC-SHA256 関数
  private def hmacSha256(message: String, secret: String): Array[Byte] = {
    val secretKey = new SecretKeySpec(secret.getBytes(CHARSET), HMAC_ALGO)
    val mac = Mac.getInstance(HMAC_ALGO)
    mac.init(secretKey)
    mac.doFinal(message.getBytes(CHARSET))
  }

  // ソルト生成関数
  private def generateSalt(): String = {
    val random = new SecureRandom()
    val bytes = new Array[Byte](16) // 128 bits
    random.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes)
  }
}

@main def run: Unit =
  val message = "Hello, World!"
  val secret = System.getenv("TINK_SECRET_KEY")
  val signed = TokenAuthenticator.sign(message, secret)
  println(s"Signed: $signed")

  val isValid = TokenAuthenticator.verify(signed, secret)
  println(s"Is Valid: $isValid")

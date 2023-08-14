package tink.sandbox.stdlib

import java.security.SecureRandom
import java.util.Base64

object GenerateSecretKey:

  def generateSecretKey(): String =
    val random = SecureRandom()
    val bytes = new Array[Byte](32) // 256 bits, which is suitable for HMAC-SHA256
    random.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes)

@main def generateAndPrintSecretKey: Unit =
  val secretKey = GenerateSecretKey.generateSecretKey()
  println(s"Generated Secret Key: $secretKey")

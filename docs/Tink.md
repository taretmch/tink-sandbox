
<!-- vim-markdown-toc GFM -->

- [Tink とは](#tink-)
  - [Tink の設計目標](#tink-設計目標)
  - [Primitives, 鍵集合](#primitives-鍵集合)
    - [MAC プリミティブ](#mac-)
    - [AEAD プリミティブ](#aead-)
    - [Streaming AEAD プリミティブ](#streaming-aead-)
    - [AEAD の使い方](#aead-使方)
  - [Tink-core](#tink-core)
    - [鍵](#鍵)
    - [鍵マネージャー](#鍵-1)
    - [鍵集合と鍵集合ハンドラ](#鍵集合鍵集合)
    - [レジストリ](#)
    - [鍵管理の特徴](#鍵管理特徴)
  - [可読性と監査性](#可読性監査性)
  - [拡張性](#拡張性)

<!-- vim-markdown-toc -->

## Tink とは

[Tink](https://github.com/google/tink) は、Google 社によって開発されている OSS の暗号化 API である。

### Tink の設計目標

- セキュリティ
  - 間違えにくい API
  - 証明されよくテストされたライブラリの再利用 (Wycheproof プロジェクト)
- ユーザビリティ
  - 簡潔で使いやすい API
  - ユーザーは期待する機能だけに集中できる
- 可読性と監査性
  - 機能がコードに見える
  - 採用されている暗号化スキームの制御
- 拡張性
  - 新しい機能、スキーマ、形式を追加しやすい
  - ローカルなカスタマイズのサポート

### Primitives, 鍵集合

Primitives は、暗号機能の抽象的な表現である。

- インタフェースとして機能を定義している
- 特定の実装やグローバルな列挙型を束縛しない
- セキュリティの保証がある実装

#### MAC プリミティブ

https://github.com/google/tink/blob/master/java_src/src/main/java/com/google/crypto/tink/Mac.java

```java
public interface Mac {
  /**
   * Computes message authentication code (MAC) for {@code data}.
   *
   * @return MAC value
   */
  byte[] computeMac(final byte[] data) throws GeneralSecurityException;

  /**
   * Verifies whether {@code mac} is a correct authentication code (MAC) for {@code data}.
   *
   * @throws GeneralSecurityException if {@code mac} is not a correct MAC for {@code data}
   */
  void verifyMac(final byte[] mac, final byte[] data) throws GeneralSecurityException;
}
```

#### AEAD プリミティブ

https://github.com/google/tink/blob/master/java_src/src/main/java/com/google/crypto/tink/Aead.java

#### Streaming AEAD プリミティブ

https://github.com/google/tink/blob/master/java_src/src/main/java/com/google/crypto/tink/StreamingAead.java

#### AEAD の使い方

1. 鍵マテリアルを生成するか取得する

```java
// 生成
KeysetHandle keysetHandle = KeysetHandle.generateNew(AheadKeyTemplates.AES128_GCM);
// 取得
AndroidKeysetManager keysetManager = AndroidKeysetManager.Builder()...;
KeysetHandle keysetHandle = keysetManager.getKeysetHandle();
```

2. 鍵マテリアルからプリミティブを取得する

```java
Aead aead = keysetHandle.getPrimitive(Aead.class);
```

3. 取得したプリミティブで、文字列を暗号化する

```java
byte[] ciphertext = aead.encrypt(plaintext, add);
```

### Tink-core

#### 鍵

Key は、暗号鍵のマテリアルとパラメータのコンテナである。

- 文字列 key type で固有である
- プロトコルバッファーとして実装される

```java
message AesGcmKey {
  uint32 version;
  bytes  key_value;
}
```

#### 鍵マネージャー

[Key Manager](https://github.com/google/tink/blob/master/java_src/src/main/java/com/google/crypto/tink/KeyManager.java) は、特定の key type の鍵のマネージャーである。鍵マネージャーは、どのプリミティブが key type に対応するか知っている。

```java
key type: "...tink.HmacKey"
message HmacKey
```

に対応するマネージャーは `HmacKeyManager` である。

#### 鍵集合と鍵集合ハンドラ

鍵集合は、鍵の集合である。鍵集合に含まれるすべての鍵は、一つのプリミティブに対応している。鍵ローテーションのための主ツールである。

鍵集合ハンドラは鍵集合まわりのラッパーである。鍵マテリアルや他のセンシティブデータへのアクセスを制限する。

#### レジストリ

レジストリは、アプリケーションによって使われる鍵マネージャーのコンテナである。

- key type から鍵マネージャーへのマップオブジェクトである。
- はじめに初期化される
  - 自動的に: TinkConfig.register()
  - 手動で:   Registry.registerKeyManager()
- プリミティブの取得の基本

#### 鍵管理の特徴

鍵集合を通じた鍵ローテーションは、以下の特徴を持つ。

- 暗号データ (暗号文や署名) 作成のためにプライマリー鍵が識別される
- 暗号データには鍵集合の中の適切な鍵がマッチする
- 期限切れの鍵を廃棄する
- KMS や HSM などの外部鍵に対する単一化された処理
  - 鍵集合の鍵は、KMS への参照だけを含む
  - 鍵集合は、外部鍵と標準の鍵の両方を含むことができる
- 暗号スキーマの段階的な廃止
  - 廃止されたスキーマの新しい鍵の生成を禁止することができる

### 可読性と監査性

- プリミティブのプロパティーの実装

```java
Aead aead = handle1.getPrimitive(Aead.class)
byte[] ciphertext1 = aead.encrypt(plaintext1, associatedData);

HybridEncrypt hybridEncrypt = handle2.getPrimitive(HybridEncrypt.class);
byte[] ciphertext2 = hybridEncrypt.encrypt(plaintext2, contextInfo);
```

- レジストリと設定
  - プリミティブとそれらの実装の完全な制御
  - 暗号スキーマの使い方の統計

### 拡張性

- カスタム key type と Tink プリミティブの実装
- カスタムプリミティブの定義と実装
- レジストリ、鍵集合、鍵ローテーションなどは標準コンポーネントと同様に機能する

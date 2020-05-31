## Tink で独自の鍵を使うには？

Keyczar では鍵の生成を独自に行っていて Tink に移行したいとき、あるいは、単に独自に鍵や暗号化アルゴリズムを実装したいとき、独自の鍵を登録する必要がある。

鍵は key type によって識別されるので、key type は全体でユニークな値になっていなければならない。フォーマット: `type.googleapis.com/packageName.classsName`

HMAC に関する鍵の情報は [google.crypto.tink.proto.hmac](https://github.com/google/tink/blob/master/proto/hmac.proto) に以下のように定義されている (中略あり)。

```proto
syntax = "proto3";

package google.crypto.tink;

import "proto/common.proto";

option java_package = "com.google.crypto.tink.proto";
option java_multiple_files = true;

message HmacParams {
  HashType hash = 1;    // HashType is an enum.
  uint32 tag_size = 2;
}

// key_type: type.googleapis.com/google.crypto.tink.HmacKey
message HmacKey {
  uint32 version = 1;
  HmacParams params = 2;
  bytes key_value = 3;
}

message HmacKeyFormat {
  HmacParams params = 1;
  uint32 key_size = 2;
  uint32 version = 3;
}
```

HmacParams メッセージは2つのフィールドを持つ。それぞれのフィールドはタグ番号を持っており、このタグ番号は `=` のあとで定義される。タグ番号は1以上の自然数であり、フィールドの識別のために使われるので一意に決める必要がある。

プロトコルバッファーは以下のように使用することができる。

```scala
hmacParams: HmacParams = HmacParams.newBuilder().setHash(HashType.Hoge).setTagSize(16).build()
hmacKey: HmacKey = HmacKey.newBuilder().setVersion(1).setParams(hmacParams).setKeyValue(keyValue).build()
```

key type に関する情報は別の場所で保存されているようだ。

## プリミティブのカスタム実装

https://github.com/google/tink/blob/master/docs/JAVA-HOWTO.md#custom-implementation-of-a-primitive

プリミティブのカスタム実装を作成する手順

1. カスタム実装が必要なプリミティブを決定する。
2. カスタム暗号化スキーマの鍵マテリアルとパラメータを保持するプロトコルバッファを定義する。
3. 1のプリミティブと2の鍵タイプの KeyManager インタフェースを実装する。

アプリケーションでプリミティブのカスタム実装を使用するには、Registry を用いてカスタム鍵タイプのためのカスタム KeyManager の実装を登録する。

```scala
Registry.registerKeyManager(keyManager)
```

`keysetHandle.getPrimitive` で対応するプリミティブを取得することによって、カスタムの実装に自動的にアクセスするようになる。これはまた、`Registry.getKeyManager(keyType)` と直接してもよい。

鍵マテリアルとパラメータをプロトコルバッファで定義する際、3つのメッセージを定義する必要がある。

- `...Params`: プリミティブのインスタンス化に必要なパラメータ。鍵が使用されるときに必要。
- `...Key`: 実際の Key のプロトコル。対応する `...Params` と鍵マテリアルを含む。
- `...KeyFormat`: 新しい鍵を生成するために必要なパラメータ。

そして、これらのメッセージを定義するときの規則と推奨事項を以下に示す。

- `...Key` は version フィールド `uint32 version` を持たなければならない。version は、この鍵が動作する実装のバージョンを一意に決める。
- `...Params` は、`...Key` のフィールドでなければならない。また、鍵が使われるときに必要なパラメータを含んでいる必要がある。
- `...Params` はまた `...KeyFormat` のフィールドでなければならない。


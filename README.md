# Tink sandbox

暗号処理の勉強用リポジトリ

- [Keyczar](https://github.com/google/keyczar/tree/Java_release_0.71://github.com/google/keyczar/tree/Java_release_0.71h) **開発終了**
- [Tink](https://github.com/google/tink)

## 要件

- HMAC x SHA256 で、メッセージの署名と検証を行うこと
- メッセージの内容は任意の文字列とする（細かい制限はない）
- 秘密鍵、公開鍵は環境変数で保持しておく
  - TINK_SECRET_KEY
  - TINK_PUBLIC_KEY
- 秘密鍵はソルトを用いて暗号化する
  - ソルトは都度生成し、署名後のトークンに含める


<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# AI Studio Uygulamanızı Çalıştırın ve Yayınlayın

Uygulamanızı yerel olarak çalıştırmak için ihtiyacınız olan her şey burada bulunmaktadır.

Uygulamanızı AI Studio'da görüntüleyin: https://ai.studio/apps/318ba97b-7ca6-4fa6-a387-317bfd2f918e

## Yerel Olarak Çalıştırma

**Ön Koşullar:**  [Android Studio](https://developer.android.com/studio)


1. Android Studio'yu açın
2. **Aç** seçeneğini seçin ve bu projeyi içeren dizini seçin
3. Android Studio'nun projeyi içe aktarırken uyumsuzlukları düzeltmesine izin verin
4. Proje dizininde `.env` adlı bir dosya oluşturun ve Gemini API anahtarınızı bu dosyada `GEMINI_API_KEY` olarak ayarlayın (örnek için `.env.example` dosyasına bakın)
5. Uygulamanın `build.gradle.kts` dosyasından bu satırı kaldırın: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Uygulamayı bir emülatör veya fiziksel cihazda çalıştırın
7. Uygulamanızı zaten AI Studio'da yayınladıysanız, lütfen Google Play Console'da [yükleme anahtarını sıfırlamayı talep edin](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset)

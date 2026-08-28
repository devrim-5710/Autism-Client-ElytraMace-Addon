# Elytra Mace Addon

Autism Client için Elytra Mace addonu. En yakın oyuncuya otomatik roketle uçar, göğüslük takar ve mace ile vurur.

## Özellikler

- Otomatik elytra kalkışı ve roket kullanımı
- En yakın oyuncuya kitlenme ve takip
- Havadayken göğüslük ile değiştirme (10 blok)
- Mace ile saldırı (6 blok menzil)
- Vuruştan sonra tekrar elytra giyip uçma döngüsü

## Ayarlar

| Ayar | Varsayılan | Aralık | Açıklama |
|------|-----------|--------|----------|
| Rocket Delay | 12 | 5-30 | Rketler arası gecikme (tick) |
| Search Chunk | 16 | 1-32 | Hedef arama menzili (chunk) |

## Durum Makinesi

```
TAKEOFF → ROCKETS → TRACK → SWAP_WAIT → ATTACK → RE_ELYTRA → (tekrar)
```

1. **TAKEOFF** - Yukarı bakar ve elytra'yi aktif eder
2. **ROCKETS** - 2 veya 3 roket kullanır (mesafeye göre)
3. **TRACK** - En yakın oyuncuya kilitlenir ve uçar
4. **SWAP_WAIT** - Elytra'yı göğüslük ile değiştirir
5. **ATTACK** - Mace ile saldırır (5 tick veya yere inene kadar)
6. **RE_ELYTRA** - Tekrar elytra giyer ve döngü başa döner

## Kurulum

1. Autism Client'i Minecraft 1.26.2'ye kur
2. `build/libs/elytra-mace-addon-1.0.0.jar` dosyasını mods klasörüne kopyala
3. Oyunu başlat

## Gereksinimler

- Minecraft 1.26.2
- Fabric Loader 0.19.3
- Autism Client 4.4-26.2
- Java 25

## Geliştirme

```bash
# Proje dizininde çalıştır
./gradlew clean build
```

Çıktı: `build/libs/elytra-mace-addon-1.0.0.jar`

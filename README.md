# iris-primitive-collections

Лёгкие списки над примитивными массивами для Kotlin Multiplatform.

`List<Int>` боксит каждый элемент. `IntArrayList` хранит `IntArray` и `size`.

Пакет: `iris.collections`  
Kotlin: 2.1.20

Исходники целиком в `commonMain`, без JVM-only API. В этом репозитории для сборки включён только `jvm()`. Native / JS / Android подключаются таргетами у себя — код либы менять не нужно:

```kotlin
kotlin {
    jvm()
    linuxX64()
    mingwX64()
    macosArm64()
    // iosArm64(); androidTarget(); js { nodejs() }
}
```

## Типы

Списки: `Byte`, `Short`, `Char`, `Int`, `Long`, `Float`, `Double`

Для каждого списка:

- `{T}Collection` / `{T}List` / `{T}MutableList`
- `{T}ArrayList` — реализация
- `{t}ArrayListOf(...)`, `array.to{T}ArrayList()`, `{T}ArrayList.init(n) { i -> ... }`
- inline `forEach` / `filter` / `map` / `any` / `all` без бокса на горячем пути

Set / Map — ключ без бокса, open addressing, load 2/3:

- `IntSet`, `LongSet`
- `IntMap<V>`, `LongMap<V>` (`V` обычный объект)
- `intSetOf(...)`, `longSetOf(...)`

```kotlin
val seen = IntSet(users.size)
for (user in users) seen += user.id

val byId = IntMap<User>(users.size)
for (user in users) byId[user.id] = user
```

Конструктор с expected size / `ensureCapacity(n)` сразу выделяет таблицу нужной длины.

`nulled` — сентинел пустого слота в массиве **ключей** (`Int.MIN_VALUE` / `Long.MIN_VALUE` по умолчанию). Свободные и удалённые ячейки им заполняются. Этот ключ класть нельзя: `add`/`put` бросают `IllegalArgumentException`. Если такие id бывают, передайте другой `nulled`.

## Presized capacity

Конструктор `IntArrayList(n)` выделяет ровно `n` слотов, не раздувает до 10.

Если размер известен хотя бы примерно — передавайте его. Это главный рычаг по бенчмаркам: без presize `Long` почти не выигрывает у `ArrayList` на `add`, с presize — в 4–6.5 раза.

```kotlin
val ids = IntArrayList(users.size)
for (user in users) ids += user.id

val buf = LongArrayList(256)
buf.ensureCapacity(buf.size + incoming.size)
buf.addAll(incoming)
```

`intArrayListOf(1, 2, 3)` уже выделяет массив точной длины.  
`IntArrayList()` без аргумента начинает с `DEFAULT_CAPACITY = 10` и растёт ×1.5 — нормально для неизвестного размера, плохо для горячего цикла на `Long`.

## Пример

```kotlin
val ids = intArrayListOf(3, 1, 2)
ids += 4
ids.sort()
ids.forEach { println(it) }

val even = ids.filter { it % 2 == 0 }
println(ids.sum())

val boxed: MutableList<Int> = ids.asMutableList()
```

## Когда использовать

Типичный список в приложении часто ≤ 50 элементов. На таком размере выигрыш — десятки наносекунд на операцию. Либа окупается не «одним списком из 16 id», а объёмом: много коротких `Int`/`Long` вне кэша бокса (−128…127), меньше объектов в куче, меньше GC.

| Сценарий | Имеет смысл |
|---|---|
| Редкие списки до 50, не в горячем пути | нет, достаточно `ArrayList` |
| Много коротких `Int`/`Long`, значения не из кэша | да — память и GC |
| Горячий цикл сборки + обход, размер известен | да, всегда presized |
| Список иногда вырастает до тысяч | да |
| Короткий `Byte` / флаг | слабо: бокс и так кэширован; для флагов лучше битсет |

## Сравнение с JDK (JMH, avgt + `-prof gc`)

JVM, Kotlin 2.1.20, G1, `-Xmx256m`.  
**×** в таблицах времени — во сколько раз примитив быстрее бокса. Жирным — заметный отрыв.  
**B/op** — `gc.alloc.rate.norm`, байт на одну операцию (создание коллекции + наполнение / один get). `≈0` — шум профилировщика.

### IntArrayList vs ArrayList&lt;Int&gt; — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.6 | ×1.4 | ×1.6 |
| add presized | **×4.0** | **×6.2** | **×7.5** |
| get | ×1.5 | ×2.3 | ×2.5 |
| forEach | ×2.0 | ×2.4 | ×2.6 |
| iterator | ×1.7 | ×2.3 | ×2.6 |
| contains | ×1.9 | ×3.4 | **×3.8** |

### LongArrayList vs ArrayList&lt;Long&gt; — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.05 | ×1.1 | ×1.1 |
| add presized | **×4.1** | **×4.5** | **×6.5** |
| get | ×1.5 | ×2.1 | **×2.8** |
| forEach | ×2.1 | ×2.3 | **×2.8** |
| iterator | ×1.7 | ×2.3 | **×2.8** |
| contains | ×1.6 | ×3.1 | **×2.7** |

На 16 элементах абсолюты Int: add 113 нс vs 70 нс, presized add 64 нс vs 16 нс, обход 16–20 нс vs 9–12 нс.  
На 100k обход примитива ~30 µs и у Int, и у Long.

### IntMap vs HashMap&lt;Int, V&gt; — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | ×0.7 | ×0.7 | ×1.0 |
| put presized | **×2.6** | **×2.3** | **×2.3** |
| get / contains | ~1 | ~1 | ~1 |
| forEach | ×1.4 | ×2.2 | ×0.5 |

### LongMap vs HashMap&lt;Long, V&gt; — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | ×0.6 | ×0.6 | ×0.8 |
| put presized | **×2.3** | **×1.9** | **×2.7** |
| get / contains | ~1 | ~1 | ~1 |
| forEach | ×1.4 | ×2.2 | ×0.6 |

### IntSet vs HashSet&lt;Int&gt; — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.0 | ×0.9 | ×2.0 |
| add presized | **×4.8** | **×3.2** | **×7.6** |
| contains | ~1 | ~1 | ~1 |
| forEach | **×3.3** | **×7.7** | **×6.1** |

`get`/`contains` у map/set — 5–8 нс, шум.  
`forEach` map на 100k медленнее: open addressing сканирует пустые слоты. У set слотов меньше — обход выигрывает.

---

### Списки — аллокации, B/op

Число = boxed / primitive.

**IntArrayList**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 240 / 240 | 29 341 / 15 002 | 2.88M / 1.28M |
| add presized | 104 / **80** | 18 475 / **4 113** | 2.00M / **0.40M** |
| get / forEach / iterator | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |
| contains | ≈0 / ≈0 | 16 / ≈0 | 16 / ≈0 |

**LongArrayList**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 240 / 424 | 36 510 / 29 733 | 3.68M / 2.56M |
| add presized | 104 / 144 | 25 644 / **8 209** | 2.80M / **0.80M** |
| get / forEach / iterator | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |
| contains | ≈0 / ≈0 | 24 / ≈0 | 24 / ≈0 |

Без presize на 16 Int платит столько же: рост backing-массива. С presize на 100k — **×5** меньше кучи у Int, **×3.5** у Long.  
`contains` у `ArrayList` боксит искомое значение (16 B Int / 24 B Long). Примитив — нет.

### Map — аллокации, B/op

**IntMap**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | 784 / 688 | 63 610 / 37 270 | 6.90M / 4.73M |
| put presized | 784 / **336** | 59 482 / **18 483** | 6.38M / **2.36M** |
| get / contains | ≈0 / ≈0 | **16** / ≈0 | **16** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

**LongMap**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | 784 / 920 | 70 779 / 53 633 | 7.70M / 6.82M |
| put presized | 784 / **464** | 66 651 / **26 676** | 7.18M / **3.41M** |
| get / contains | ≈0 / ≈0 | **24** / ≈0 | **24** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

HashMap на каждый `get`/`contains` боксит ключ. Presized IntMap на 100k: **×2.7** меньше байт. `gc.time` put HashMap 100k ≈ 1.4–1.6 с за прогон, presized IntMap ≈ 0.45 с.

### IntSet — аллокации, B/op

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 784 / 416 | 63 610 / 20 771 | 6.91M / 2.62M |
| add presized | 784 / **192** | 59 498 / **10 274** | 6.38M / **1.31M** |
| contains | ≈0 / ≈0 | **16** / ≈0 | **16** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

Presized Set на 16: **×4** меньше кучи (192 vs 784). На 100k: **×4.9**.

`Byte`/`Boolean` по CPU так не разгонятся: `Byte.valueOf` кэширует все 256 значений. Выигрыш там в плотности массива, не в аллокациях lookup.

## Контракт

- `containsAll` — все элементы аргумента есть в списке
- `toArray(dest)` — если `dest` короткий, возвращается новый массив
- `equals`/`hashCode` как у `List` (Float/Double — по битам, включая NaN)
- `empty()` каждый раз новый список, не синглтон
- конструктор `{T}ArrayList(n)` выделяет ровно `n`
- итератор fail-fast через `modCount`
- `asMutableList()` — полный адаптер, не `TODO`

## Как гонять бенчи

```bash
./gradlew jvmBenchmarkBenchmark          # время, kotlinx summary
./gradlew jmhProfGc                      # сырой JMH + -prof gc, куча 256m
```

`jvmBenchmarkGcBenchmark` **не** печатает `gc.alloc.rate.norm`: kotlinx-runner игнорирует `-prof` и в summary оставляет Score.

`jmhProfGc` — `org.openjdk.jmh.Main -prof gc`:

- `gc.alloc.rate.norm` — байт на операцию
- `gc.count` / `gc.time` — сколько раз и сколько собирали за измерение

Сценарии: `*ArrayListBenchmark`, `IntMapBenchmark`, `LongMapBenchmark`, `IntSetBenchmark`. Размеры: 16 / 1024 / 100000.

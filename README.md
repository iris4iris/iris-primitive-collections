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

## Сборка из Iterable

Расширения сразу выделяют capacity по `Collection.size` / `Array.size`.

```kotlin
val ids = users.mapInts { it.id }
val byId = users.associateIntsBy { it.id }
val names = users.associateInts({ it.id }, { it.name })
val seen = users.toIntSet { it.id }
val grouped = users.groupIntsBy { it.roleId }
```

Есть `mapLongs` / `associateLongsBy` / `toLongSet` / `groupLongsBy`, плюс те же функции на `Array<T>`, `toIntArrayList()` / `toIntSet()` на `Iterable<Int>` и `IntArray`.

## Когда использовать

Типичный список в приложении часто ≤ 50 элементов. На таком размере выигрыш — десятки наносекунд на операцию. Либа окупается не «одним списком из 16 id», а объёмом: много коротких `Int`/`Long` вне кэша бокса (−128…127), меньше объектов в куче, меньше GC.

| Сценарий | Имеет смысл |
|---|---|
| Редкие списки до 50, не в горячем пути | нет, достаточно `ArrayList` |
| Много коротких `Int`/`Long`, значения не из кэша | да — память и GC |
| Горячий цикл сборки + обход, размер известен | да, всегда presized |
| Список иногда вырастает до тысяч | да |
| Буфер / маска / сериализация `Byte` | да — плотность: 1 байт вместо ссылки (~4 B) |
| Флаги `Boolean` | нет в либе; нужен битсет, не `ArrayList<Boolean>` |

## Сравнение с JDK (JMH, avgt + `-prof gc`)

JVM, Kotlin 2.1.20, G1, `-Xmx256m`. Значения `i + 128` — вне кэша `Integer`/`Long` (−128…127).  
**×** — во сколько раз примитив быстрее бокса. Жирным — заметный отрыв.  
**B/op** — `gc.alloc.rate.norm`. `≈0` — шум профилировщика.

### IntArrayList vs ArrayList<Int> — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.5 | ×1.8 | ×1.5 |
| add presized | **×4.1** | **×6.8** | **×9.0** |
| get | ×1.5 | ×2.6 | ×2.5 |
| forEach | ×1.9 | ×2.6 | ×2.6 |
| iterator | ×1.5 | ×2.6 | ×2.6 |
| contains | ×1.9 | ×3.0 | **×3.9** |

### LongArrayList vs ArrayList<Long> — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.1 | ×1.2 | ×1.2 |
| add presized | **×4.0** | **×4.2** | **×5.9** |
| get | ×1.6 | ×2.3 | **×3.2** |
| forEach | ×1.9 | ×2.1 | ×2.7 |
| iterator | ×1.7 | ×2.0 | ×2.7 |
| contains | ×1.7 | ×2.8 | ×2.6 |

На 16 Int: add 112 нс vs 73 нс, presized 74 нс vs 18 нс.  
На 100k обход примитива ~30 µs и у Int, и у Long.

### IntMap vs HashMap<Int, V> — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | ×0.7 | ×0.5 | ×0.9 |
| put presized | **×2.5** | **×2.2** | **×2.4** |
| get / contains | ~1 | ~1 | ~1 |
| forEach | ×1.5 | ×2.1 | ×0.5 |

### LongMap vs HashMap<Long, V> — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | ×0.7 | ×0.6 | ×1.3 |
| put presized | **×2.6** | **×2.1** | **×3.8** |
| get / contains | ~1 | ~1 | ~1 |
| forEach | ×1.4 | ×2.2 | ×0.6 |

### IntSet vs HashSet<Int> — время

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.1 | ×0.9 | ×2.3 |
| add presized | **×5.1** | **×3.1** | **×9.5** |
| contains | ~1 | ~1 | ~1 |
| forEach | **×3.2** | **×7.7** | **×6.2** |

`get`/`contains` у map/set — 5–8 нс, шум.  
`forEach` map на 100k медленнее: open addressing сканирует пустые слоты.

---

### Списки — аллокации, B/op

Число = boxed / primitive.

**IntArrayList**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 496 / 240 | 31 389 / 15 002 | 2.88M / 1.28M |
| add presized | 360 / **80** | 20 523 / **4 113** | 2.00M / **0.40M** |
| get / forEach / iterator | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |
| contains | **16** / ≈0 | **16** / ≈0 | **16** / ≈0 |

**LongArrayList**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 624 / 424 | 39 582 / 29 733 | 3.68M / 2.56M |
| add presized | 488 / 144 | 28 717 / **8 209** | 2.80M / **0.80M** |
| get / forEach / iterator | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |
| contains | **24** / ≈0 | **24** / ≈0 | **24** / ≈0 |

Вне кэша `Integer` presized Int на 16: **×4.5** меньше кучи (360 vs 80). На 100k — **×5**.  
`contains` у `ArrayList` боксит искомое (16 B Int / 24 B Long) на любом размере.

### Map — аллокации, B/op

**IntMap**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | 1 040 / 688 | 65 659 / 37 270 | 6.91M / 4.73M |
| put presized | 1 040 / **336** | 61 530 / **18 483** | 6.38M / **2.36M** |
| get / contains | **16** / ≈0 | **16** / ≈0 | **16** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

**LongMap**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| put | 1 168 / 920 | 73 852 / 53 633 | 7.71M / 6.83M |
| put presized | 1 168 / **464** | 69 724 / **26 676** | 7.18M / **3.41M** |
| get / contains | **24** / ≈0 | **24** / ≈0 | **24** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

HashMap на каждый `get`/`contains` боксит ключ. Presized IntMap на 100k: **×2.7** меньше байт.

### IntSet — аллокации, B/op

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | 1 040 / 416 | 65 659 / 20 772 | 6.91M / 2.62M |
| add presized | 1 040 / **192** | 61 530 / **10 274** | 6.38M / **1.31M** |
| contains | **16** / ≈0 | **16** / ≈0 | **16** / ≈0 |
| forEach | ≈0 / ≈0 | ≈0 / ≈0 | ≈0 / ≈0 |

Presized Set на 16: **×5.4** меньше кучи (192 vs 1 040). На 100k: **×4.9**.

`Byte.valueOf` кэширует все 256 значений: бокс на CPU почти бесплатный, новых объектов нет. Смысл `ByteArrayList` не в этом, а в плотности. `ArrayList<Byte>` хранит ссылки (~4 байта на элемент при сжатых oops) плюс общий пул из 256 `Byte`. `ByteArrayList` — сырой `ByteArray`, 1 байт на элемент, без индирекции. На 16 элементах разница копеечная. На 100k — ~400 КБ ссылок против ~100 КБ байт и плотный обход.

`Boolean` в либе нет: два синглтона, `boolean[]` уже байт на флаг. Для флагов — битсет.

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

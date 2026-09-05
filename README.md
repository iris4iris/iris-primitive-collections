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

`Byte`, `Short`, `Char`, `Int`, `Long`, `Float`, `Double`

Для каждого:

- `{T}Collection` / `{T}List` / `{T}MutableList`
- `{T}ArrayList` — реализация
- `{t}ArrayListOf(...)`, `array.to{T}ArrayList()`, `{T}ArrayList.init(n) { i -> ... }`
- inline `forEach` / `filter` / `map` / `any` / `all` без бокса на горячем пути

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

## Сравнение с ArrayList (JMH, avgt)

Во сколько раз `*ArrayList` быстрее боксящего `ArrayList<T>`. JVM, Kotlin 2.1.20.

**IntArrayList vs ArrayList&lt;Int&gt;**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.6 | ×1.4 | ×1.6 |
| add presized | **×4.0** | **×6.2** | **×7.5** |
| get | ×1.5 | ×2.3 | ×2.5 |
| forEach | ×2.0 | ×2.4 | ×2.6 |
| iterator | ×1.7 | ×2.3 | ×2.6 |
| contains | ×1.9 | ×3.4 | **×3.8** |

**LongArrayList vs ArrayList&lt;Long&gt;**

| Операция | 16 | 1 024 | 100 000 |
| --- | ---: | ---: | ---: |
| add | ×1.05 | ×1.1 | ×1.1 |
| add presized | **×4.1** | **×4.5** | **×6.5** |
| get | ×1.5 | ×2.1 | **×2.8** |
| forEach | ×2.1 | ×2.3 | **×2.8** |
| iterator | ×1.7 | ×2.3 | **×2.8** |
| contains | ×1.6 | ×3.1 | **×2.7** |

На 16 элементах абсолютные числа для Int: add 113 нс vs 70 нс, presized add 64 нс vs 16 нс, обход 16–20 нс vs 9–12 нс.

На 100k обход примитива ~30 µs и у Int, и у Long; `get` / `forEach` / `iterator` / `sum` совпадают — `inline forEach` не хуже сырого цикла.

`Byte`/`Boolean` так не разгонятся: `Byte.valueOf` кэширует все 256 значений, `Boolean.TRUE`/`FALSE` — синглтоны. Там выигрыш в плотности массива, не в аллокациях.

## Контракт

- `containsAll` — все элементы аргумента есть в списке
- `toArray(dest)` — если `dest` короткий, возвращается новый массив
- `equals`/`hashCode` как у `List` (Float/Double — по битам, включая NaN)
- `empty()` каждый раз новый список, не синглтон
- конструктор `{T}ArrayList(n)` выделяет ровно `n`
- итератор fail-fast через `modCount`
- `asMutableList()` — полный адаптер, не `TODO`

## Бенчмарки

```bash
./gradlew jvmBenchmarkBenchmark
./gradlew jvmBenchmarkShort30Benchmark   # только Long, если включён конфиг short30
```

Сценарии: `src/jvmBenchmark/kotlin/iris/collections/*ArrayListBenchmark.kt`  
add (grow / presized), get, forEach, sum, contains, iterator. Размеры: 16 / 1024 / 100000.

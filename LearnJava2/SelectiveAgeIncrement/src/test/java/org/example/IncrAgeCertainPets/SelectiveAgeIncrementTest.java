package org.example.IncrAgeCertainPets;

import org.example.SelectiveAgeIncrement.Animal;
import org.example.SelectiveAgeIncrement.Cat;
import org.example.SelectiveAgeIncrement.Dog;
import org.example.SelectiveAgeIncrement.SelectiveAgeIncrement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SelectiveAgeIncrement — полный набор тестов")
class SelectiveAgeIncrementTest {

    // ==========================================================
    //  Общие утилиты
    // ==========================================================

    private ByteArrayOutputStream outBuf;
    private PrintStream out;
    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalIn  = System.in;
        outBuf = new ByteArrayOutputStream();
        out = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
        System.setOut(out);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private String outText() {
        return outBuf.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private void setInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0, idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    // ==========================================================
    //  1. Модель
    // ==========================================================

    @Nested
    @DisplayName("Модель Animal / Cat / Dog")
    class ModelTests {

        @Test
        @DisplayName("Cat: поля + toString")
        void cat() {
            Cat c = new Cat("Murka", 3);
            assertEquals("Murka", c.getName());
            assertEquals(3, c.getAge());
            assertEquals("Cat name = Murka, age = 3", c.toString());
        }

        @Test
        @DisplayName("Dog: поля + toString")
        void dog() {
            Dog d = new Dog("Rex", 5);
            assertEquals("Rex", d.getName());
            assertEquals(5, d.getAge());
            assertEquals("Dog name = Rex, age = 5", d.toString());
        }

        @ParameterizedTest(name = "age = {0}")
        @CsvSource({"0", "1", "-1", "10", "11", "2147483647", "-2147483648"})
        @DisplayName("Граничные значения возраста сохраняются в объекте")
        void ageBoundaries(int age) {
            assertEquals(age, new Cat("X", age).getAge());
            assertEquals(age, new Dog("X", age).getAge());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null / пустое имя не валит конструктор")
        void nullOrEmptyName(String name) {
            assertEquals(name, new Cat(name, 1).getName());
            assertEquals(name, new Dog(name, 1).getName());
        }
    }

    // ==========================================================
    //  2. TYPES
    // ==========================================================

    @Nested
    @DisplayName("TYPES")
    class TypesTests {

        @Test
        @DisplayName("Ровно 2 элемента")
        void size() {
            assertEquals(2, SelectiveAgeIncrement.TYPES.size());
        }

        @Test
        @DisplayName("Содержит dog и cat")
        void contents() {
            assertEquals(Set.of("dog", "cat"), SelectiveAgeIncrement.TYPES);
        }

        @Test
        @DisplayName("Иммутабельный")
        void immutable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> SelectiveAgeIncrement.TYPES.add("hamster"));
        }

        @Test
        @DisplayName("Все в нижнем регистре")
        void allLowercase() {
            for (String t : SelectiveAgeIncrement.TYPES) {
                assertEquals(t.toLowerCase(), t);
            }
        }
    }

    // ==========================================================
    //  3. createPet
    // ==========================================================

    @Nested
    @DisplayName("createPet")
    class CreatePetTests {

        @ParameterizedTest(name = "type = {0} → {3}")
        @CsvSource({
                "dog, Rex,   1, Dog",
                "cat, Murka, 2, Cat"
        })
        @DisplayName("Валидные типы → ожидаемый класс")
        void valid(String type, String name, int age, String expectedClass) {
            Animal a = SelectiveAgeIncrement.createPet(type, name, age);
            assertEquals(expectedClass, a.getClass().getSimpleName());
            assertEquals(name, a.getName());
            assertEquals(age, a.getAge());
        }

        @ParameterizedTest(name = "type = ''{0}''")
        @ValueSource(strings = {"DOG", "Cat", "dog ", " cat", "", "   ", "hamster", "rabbit"})
        @DisplayName("Регистр/пробелы/неизвестный → IllegalStateException")
        void unexpected(String type) {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> SelectiveAgeIncrement.createPet(type, "X", 1));
            assertTrue(e.getMessage().contains("Unexpected value"));
        }

        @Test
        @DisplayName("null type → NPE (текущее поведение switch)")
        void nullType() {
            assertThrows(NullPointerException.class,
                    () -> SelectiveAgeIncrement.createPet(null, "X", 1));
        }

        @Test
        @DisplayName("age = null сохраняется как null")
        void nullAge() {
            assertNull(SelectiveAgeIncrement.createPet("dog", "Rex", null).getAge());
        }
    }

    // ==========================================================
    //  4. readInt
    // ==========================================================

    @Nested
    @DisplayName("readInt")
    class ReadIntTests {

        private int callReadInt(String input) {
            try (Scanner sc = new Scanner(input)) {
                return SelectiveAgeIncrement.readInt(sc);
            }
        }

        @Test
        @DisplayName("Простое число")
        void simple() {
            assertEquals(42, callReadInt("42\n"));
            assertEquals("", outText());
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"0", "-1", "1", "2147483647", "-2147483648"})
        @DisplayName("Границы int")
        void boundaries(String value) {
            assertEquals(Integer.parseInt(value), callReadInt(value + "\n"));
        }

        @Test
        @DisplayName("Мусор → рекурсия, потом число")
        void garbageRetries() {
            assertEquals(7, callReadInt("abc\n7\n"));
            assertEquals("Could not parse a number. Please, try again\n", outText());
        }

        @Test
        @DisplayName("Переполнение сверху → рекурсия")
        void overflowRetries() {
            assertEquals(5, callReadInt("2147483648\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Переполнение снизу → рекурсия")
        void underflowRetries() {
            assertEquals(5, callReadInt("-2147483649\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пустая строка → рекурсия")
        void emptyLineRetries() {
            assertEquals(3, callReadInt("\n3\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пробелы вокруг → NumberFormatException (parseInt не тримит)")
        void whitespaceNotTrimmed() {
            assertEquals(9, callReadInt(" 5 \n9\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Несколько ошибок подряд — порядок сообщений сохраняется")
        void multipleErrors() {
            assertEquals(1, callReadInt("x\ny\n1\n"));
            assertEquals(2, countOccurrences(outText(), "Could not parse a number"));
        }

        @Test
        @DisplayName("Много ошибок → глубокая рекурсия, но не падает (100 раз)")
        void manyErrorsRecursion() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) sb.append("x\n");
            sb.append("42\n");
            assertEquals(42, callReadInt(sb.toString()));
            assertEquals(100, countOccurrences(outText(), "Could not parse a number"));
        }

        @Test
        @DisplayName("EOF → NoSuchElementException (текущее поведение)")
        void eofThrows() {
            try (Scanner sc = new Scanner("")) {
                assertThrows(NoSuchElementException.class,
                        () -> SelectiveAgeIncrement.readInt(sc));
            }
        }
    }

    // ==========================================================
    //  5. readPet
    // ==========================================================

    @Nested
    @DisplayName("readPet")
    class ReadPetTests {

        private Optional<Animal> callReadPet(String input) {
            try (Scanner sc = new Scanner(input)) {
                return SelectiveAgeIncrement.readPet(sc);
            }
        }

        @Test
        @DisplayName("dog валидный")
        void dog() {
            Optional<Animal> p = callReadPet("dog\nRex\n2\n");
            assertInstanceOf(Dog.class, p.orElseThrow());
            assertEquals("Rex", p.get().getName());
            assertEquals(2, p.get().getAge());
        }

        @Test
        @DisplayName("cat валидный")
        void cat() {
            assertInstanceOf(Cat.class, callReadPet("cat\nMurka\n3\n").orElseThrow());
        }

        @ParameterizedTest(name = "raw = ''{0}''")
        @ValueSource(strings = {"DOG", "Dog", "  dog  ", "\tcat\t", " CAT "})
        @DisplayName("trim + toLowerCase для type")
        void typeNormalization(String raw) {
            assertTrue(callReadPet(raw + "\nX\n1\n").isPresent());
        }

        @ParameterizedTest(name = "type = ''{0}''")
        @ValueSource(strings = {"hamster", "rabbit", "", "   ", "do g"})
        @DisplayName("Неподдерживаемый type → empty + сообщение")
        void unsupportedType(String type) {
            Optional<Animal> p = callReadPet(type + "\n");
            assertTrue(p.isEmpty());
            assertEquals("Incorrect input. Unsupported pet type\n", outText());
        }

        @Test
        @DisplayName("Пустое имя допустимо")
        void emptyName() {
            Optional<Animal> p = callReadPet("dog\n\n1\n");
            assertEquals("", p.orElseThrow().getName());
        }

        @Test
        @DisplayName("Имя с пробелами сохраняется")
        void nameWithSpaces() {
            assertEquals("  Rex  ",
                    callReadPet("dog\n  Rex  \n1\n").orElseThrow().getName());
        }

        @Test
        @DisplayName("age = 1 — минимальный валидный")
        void minValidAge() {
            assertEquals(1, callReadPet("dog\nRex\n1\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = 0 → empty + сообщение")
        void ageZero() {
            assertTrue(callReadPet("dog\nRex\n0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = -1 → empty")
        void ageNegative() {
            assertTrue(callReadPet("dog\nRex\n-1\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE допустим")
        void maxAge() {
            assertEquals(Integer.MAX_VALUE,
                    callReadPet("dog\nRex\n2147483647\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = Integer.MIN_VALUE → empty")
        void minAge() {
            assertTrue(callReadPet("dog\nRex\n-2147483648\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age мусором → рекурсия, потом валиден")
        void ageRetry() {
            Optional<Animal> p = callReadPet("dog\nRex\nxyz\n2\n");
            assertEquals(2, p.orElseThrow().getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Невалидный type: name и age не читаются")
        void invalidTypeDoesNotConsumeName() {
            try (Scanner sc = new Scanner("rabbit\nIGNORED\n42\n")) {
                Optional<Animal> p = SelectiveAgeIncrement.readPet(sc);
                assertTrue(p.isEmpty());
                assertEquals("IGNORED", sc.nextLine());
            }
        }
    }

    // ==========================================================
    //  6. readPets(Scanner, int) — рекурсивный обход
    // ==========================================================

    @Nested
    @DisplayName("readPets(Scanner, int)")
    class ReadPetsWithSizeTests {

        private List<Animal> call(String input, int size) {
            try (Scanner sc = new Scanner(input)) {
                return SelectiveAgeIncrement.readPets(sc, size);
            }
        }

        @Test
        @DisplayName("size = 0 → пустой список, ничего не читается")
        void zero() {
            List<Animal> pets = call("ANYTHING\n", 0);
            assertTrue(pets.isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список")
        void negative() {
            assertTrue(call("", -1).isEmpty());
            assertTrue(call("", -100).isEmpty());
        }

        @Test
        @DisplayName("size = 1 — минимальный непустой")
        void one() {
            assertEquals(1, call("dog\nRex\n1\n", 1).size());
        }

        // ---------- ВАЖНО: рекурсивная реализация ----------

        @Test
        @DisplayName("size = 2 — порядок СОХРАНЯЕТСЯ (рекурсия + add в хвост)")
        void twoOrder() {
            List<Animal> pets = call("dog\nRex\n2\ncat\nMurka\n1\n", 2);
            assertEquals(2, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertInstanceOf(Cat.class, pets.get(1));
            assertEquals("Rex", pets.get(0).getName());
            assertEquals("Murka", pets.get(1).getName());
        }

        @Test
        @DisplayName("size = 5 — порядок сохраняется, все валидные")
        void fiveOrder() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) sb.append("dog\nD").append(i).append("\n").append(i + 1).append("\n");
            List<Animal> pets = call(sb.toString(), 5);
            assertEquals(5, pets.size());
            for (int i = 0; i < 5; i++) {
                assertEquals("D" + i, pets.get(i).getName());
                assertEquals(i + 1, pets.get(i).getAge());
            }
        }

        @Test
        @DisplayName("Все три слота невалидны по разным причинам — пустой результат")
        void allSlotsInvalid() {
            // 3 слота: rabbit (bad type), dog+age=0 (bad age), hamster (bad type)
            List<Animal> pets = call("rabbit\ndog\nRex\n0\nhamster\n", 3);
            assertTrue(pets.isEmpty());
            String o = outText();
            assertEquals(2, countOccurrences(o, "Unsupported pet type")); // rabbit + hamster
            assertEquals(1, countOccurrences(o, "Age <= 0"));             // dog
        }

        @Test
        @DisplayName("Глубокая рекурсия — size = 1000 не падает (StackOverflow?)")
        void deepRecursion() {
            // 1000 валидных слотов — проверка, что рекурсия выдерживает
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) sb.append("dog\nD\n1\n");
            List<Animal> pets = call(sb.toString(), 1000);
            assertEquals(1000, pets.size());
        }

        @Test
        @DisplayName("EOF раньше времени → NoSuchElementException")
        void eofEarly() {
            assertThrows(NoSuchElementException.class,
                    () -> call("dog\n", 1));
        }
    }

    // ==========================================================
    //  7. readPets() — читает из System.in
    // ==========================================================

    @Nested
    @DisplayName("readPets()")
    class ReadPetsTests {

        private List<Animal> call(String input) {
            setInput(input);
            return SelectiveAgeIncrement.readPets();
        }

        @Test
        @DisplayName("size = 0 → пустой список")
        void zero() {
            assertTrue(call("0\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список")
        void negative() {
            assertTrue(call("-1\n").isEmpty());
        }

        @Test
        @DisplayName("size мусором → рекурсия, потом корректно")
        void sizeRetry() {
            List<Animal> pets = call("abc\n1\ndog\nRex\n1\n");
            assertEquals(1, pets.size());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Несколько валидных, порядок сохранён")
        void multiple() {
            List<Animal> pets = call("3\ndog\nRex\n2\ncat\nMurka\n1\ndog\nBobik\n4\n");
            assertEquals(3, pets.size());
            assertEquals("Rex", pets.get(0).getName());
            assertEquals("Murka", pets.get(1).getName());
            assertEquals("Bobik", pets.get(2).getName());
        }

        @Test
        @DisplayName("Смесь валидных и невалидных")
        void mixed() {
            List<Animal> pets = call("3\nrabbit\ndog\nRex\n0\ncat\nMurka\n3\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Cat.class, pets.get(0));
            String o = outText();
            assertTrue(o.contains("Unsupported pet type"));
            assertTrue(o.contains("Age <= 0"));
        }
    }

    // ==========================================================
    //  8. incrementAge
    // ==========================================================

    @Nested
    @DisplayName("incrementAge")
    class IncrementAgeTests {

        @Test
        @DisplayName("Dog → новый Dog, age + 1, имя сохранено")
        void dog() {
            Animal a = SelectiveAgeIncrement.incrementAge(new Dog("Rex", 5));
            assertInstanceOf(Dog.class, a);
            assertEquals("Rex", a.getName());
            assertEquals(6, a.getAge());
        }

        @Test
        @DisplayName("Cat → новый Cat, age + 1, имя сохранено")
        void cat() {
            Animal a = SelectiveAgeIncrement.incrementAge(new Cat("Murka", 5));
            assertInstanceOf(Cat.class, a);
            assertEquals("Murka", a.getName());
            assertEquals(6, a.getAge());
        }

        @Test
        @DisplayName("ВСЁ, что НЕ Dog → создаётся Cat (важный побочный эффект else)")
        void nonDogBecomesCat() {
            // Если когда-нибудь появятся Hamster / GuineaPig — они превратятся в Cat!
            // Сейчас TYPES содержит только dog/cat, но сам метод открыт.
            assertInstanceOf(Cat.class,
                    SelectiveAgeIncrement.incrementAge(new Cat("C", 1)));
        }

        @Test
        @DisplayName("Возвращается НОВЫЙ объект, исходный не мутируется")
        void newObjectReturned() {
            Dog original = new Dog("Rex", 5);
            Animal updated = SelectiveAgeIncrement.incrementAge(original);
            assertNotSame(original, updated);
            assertEquals(5, original.getAge()); // исходный не изменён
            assertEquals(6, updated.getAge());
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE → переполнение в MIN_VALUE (текущее поведение)")
        void overflow() {
            Animal updated = SelectiveAgeIncrement.incrementAge(new Dog("Rex", Integer.MAX_VALUE));
            assertEquals(Integer.MIN_VALUE, updated.getAge());
        }

        @Test
        @DisplayName("age = -1 → 0")
        void negativeToZero() {
            assertEquals(0, SelectiveAgeIncrement.incrementAge(new Cat("C", -1)).getAge());
        }

        @Test
        @DisplayName("null name сохраняется")
        void nullName() {
            assertNull(SelectiveAgeIncrement.incrementAge(new Dog(null, 5)).getName());
        }
    }

    // ==========================================================
    //  9. updatePets — ГЛАВНОЕ правило: возраст > 10 → +1
    // ==========================================================

    @Nested
    @DisplayName("updatePets")
    class UpdatePetsTests {

        @Test
        @DisplayName("Пустой список → пустой")
        void empty() {
            assertTrue(SelectiveAgeIncrement.updatePets(List.of()).isEmpty());
            assertEquals("", outText());
        }

        // ---------- граница age > 10 ----------

        @ParameterizedTest(name = "age = {0} → age' = {1}")
        @CsvSource({
                // age, ожидаемый возраст после updatePets
                "0,   0",   // не меняется
                "1,   1",
                "9,   9",
                "10,  10",  // ГРАНИЦА: не инкрементируется (условие > 10)
                "11,  12",  // ГРАНИЦА: первый инкремент
                "12,  13",
                "100, 101"
        })
        @DisplayName("Граница > 10: 10 не инкрементируется, 11 → 12")
        void ageBoundary(int age, int expected) {
            List<Animal> result = SelectiveAgeIncrement.updatePets(List.of(new Dog("Rex", age)));
            assertEquals(1, result.size());
            assertEquals(expected, result.get(0).getAge());
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE → переполнение в MIN_VALUE")
        void maxAgeOverflow() {
            List<Animal> result = SelectiveAgeIncrement.updatePets(
                    List.of(new Dog("Rex", Integer.MAX_VALUE)));
            assertEquals(Integer.MIN_VALUE, result.get(0).getAge());
        }

        @Test
        @DisplayName("age = Integer.MIN_VALUE → не инкрементируется")
        void minAgeUntouched() {
            List<Animal> result = SelectiveAgeIncrement.updatePets(
                    List.of(new Dog("Rex", Integer.MIN_VALUE)));
            assertEquals(Integer.MIN_VALUE, result.get(0).getAge());
        }

        // ---------- тип ----------

        @Test
        @DisplayName("Dog и Cat обрабатываются одинаково (оба > 10)")
        void bothTypesIncremented() {
            List<Animal> result = SelectiveAgeIncrement.updatePets(List.of(
                    new Dog("Rex", 11),
                    new Cat("Murka", 15)));
            assertEquals(12, result.get(0).getAge());
            assertEquals(16, result.get(1).getAge());
            assertInstanceOf(Dog.class, result.get(0));
            assertInstanceOf(Cat.class, result.get(1));
        }

        // ---------- исходный список не мутируется ----------

        @Test
        @DisplayName("Исходный список и его элементы не мутируются")
        void originalNotMutated() {
            Animal a = new Dog("Rex", 11);
            List<Animal> input = List.of(a);
            SelectiveAgeIncrement.updatePets(input);
            assertEquals(11, a.getAge());
            assertEquals(11, input.get(0).getAge());
        }

        @Test
        @DisplayName("Возвращается НОВЫЙ список, не тот же самый")
        void newListReturned() {
            List<Animal> input = List.of(new Dog("Rex", 11));
            List<Animal> result = SelectiveAgeIncrement.updatePets(input);
            assertNotSame(input, result);
        }

        // ---------- смеси ----------

        @Test
        @DisplayName("Смесь: 5, 10, 11, 15 → 5, 10, 12, 16; порядок сохранён")
        void mixed() {
            List<Animal> result = SelectiveAgeIncrement.updatePets(List.of(
                    new Dog("A", 5),
                    new Cat("B", 10),
                    new Dog("C", 11),
                    new Cat("D", 15)));
            assertEquals(4, result.size());
            assertEquals(5, result.get(0).getAge());
            assertEquals(10, result.get(1).getAge());
            assertEquals(12, result.get(2).getAge());
            assertEquals(16, result.get(3).getAge());
            assertEquals("A", result.get(0).getName());
            assertEquals("D", result.get(3).getName());
        }

        @Test
        @DisplayName("Много элементов (>10) — все инкрементируются, порядок сохранён")
        void many() {
            List<Animal> input = new ArrayList<>();
            for (int i = 0; i < 100; i++) input.add(new Dog("D" + i, 20));
            List<Animal> result = SelectiveAgeIncrement.updatePets(input);
            assertEquals(100, result.size());
            for (int i = 0; i < 100; i++) {
                assertEquals(21, result.get(i).getAge());
                assertEquals("D" + i, result.get(i).getName());
            }
        }

        @Test
        @DisplayName("Иммутабельный входной список — не падает")
        void immutableInput() {
            List<Animal> input = List.of(new Dog("Rex", 11));
            assertEquals(12, SelectiveAgeIncrement.updatePets(input).get(0).getAge());
        }
    }

    // ==========================================================
    //  10. printListPets
    // ==========================================================

    @Nested
    @DisplayName("printListPets")
    class PrintListPetsTests {

        @Test
        @DisplayName("Пустой список — пустой вывод")
        void empty() {
            SelectiveAgeIncrement.printListPets(List.of());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Один элемент")
        void one() {
            SelectiveAgeIncrement.printListPets(List.of(new Dog("Rex", 2)));
            assertEquals("Dog name = Rex, age = 2\n", outText());
        }

        @Test
        @DisplayName("Несколько — по строке на каждый, порядок сохранён")
        void many() {
            SelectiveAgeIncrement.printListPets(List.of(
                    new Dog("Rex", 2),
                    new Cat("Murka", 1),
                    new Dog("Bobik", 4)));
            assertEquals(
                    "Dog name = Rex, age = 2\n" +
                            "Cat name = Murka, age = 1\n" +
                            "Dog name = Bobik, age = 4\n",
                    outText());
        }

        @Test
        @DisplayName("Иммутабельный список — не падает")
        void immutableList() {
            SelectiveAgeIncrement.printListPets(List.of(new Cat("M", 1)));
            assertEquals("Cat name = M, age = 1\n", outText());
        }
    }

    // ==========================================================
    //  11. Интеграционный тест: readPets → updatePets → printListPets
    // ==========================================================

    @Nested
    @DisplayName("Интеграция: readPets → updatePets → printListPets")
    class IntegrationTests {

        @Test
        @DisplayName("Сквозной сценарий: ввод → обновление → вывод")
        void endToEnd() {
            setInput("3\ndog\nRex\n5\ncat\nMurka\n11\nDOG\nBobik\n15\n");
            List<Animal> pets = SelectiveAgeIncrement.readPets();
            List<Animal> updated = SelectiveAgeIncrement.updatePets(pets);
            SelectiveAgeIncrement.printListPets(updated);

            assertEquals(3, updated.size());
            assertEquals(5, updated.get(0).getAge());   // 5 не > 10
            assertEquals(12, updated.get(1).getAge());  // 11 > 10
            assertEquals(16, updated.get(2).getAge());  // 15 > 10

            assertEquals(
                    "Dog name = Rex, age = 5\n" +
                            "Cat name = Murka, age = 12\n" +
                            "Dog name = Bobik, age = 16\n",
                    outText());
        }

        @Test
        @DisplayName("Все животные младше 10 — вывод без изменений возраста")
        void allYoung() {
            setInput("2\ndog\nA\n1\ncat\nB\n10\n");
            List<Animal> pets = SelectiveAgeIncrement.readPets();
            SelectiveAgeIncrement.printListPets(SelectiveAgeIncrement.updatePets(pets));
            assertEquals(
                    "Dog name = A, age = 1\n" +
                            "Cat name = B, age = 10\n",
                    outText());
        }
    }
}
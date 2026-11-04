package org.example.FoodDose;

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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FoodDose — полный набор тестов")
class FoodDoseTest {

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

    private int callReadInt(String input) {
        try (Scanner sc = new Scanner(input)) {
            return FoodDose.readInt(sc);
        }
    }

    private double callReadDouble(String input) {
        try (Scanner sc = new Scanner(input)) {
            return FoodDose.readDouble(sc);
        }
    }

    private List<Animal> callReadPets(String input) {
        setInput(input);
        return FoodDose.readPets();
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0, idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    /** Оборачиваем вызов printListPets: private static, пишет в подменённый System.out. */
    private void invokePrintListPets(List<Animal> pets) throws Exception {
        Method m = FoodDose.class.getDeclaredMethod("printListPets", List.class);
        m.setAccessible(true);
        m.invoke(null, pets);
    }

    // ==========================================================
    //  1. Доменная модель
    // ==========================================================

    @Nested
    @DisplayName("Модель Animal / Cat / Dog")
    class ModelTests {

        @Test
        @DisplayName("Cat: сохраняет имя, возраст, массу")
        void catStoresFields() {
            Cat c = new Cat("Murka", 3, 4.5);
            assertEquals("Murka", c.getName());
            assertEquals(3, c.getAge());
            assertEquals(4.5, c.getMass());
        }

        @Test
        @DisplayName("Dog: сохраняет имя, возраст, массу")
        void dogStoresFields() {
            Dog d = new Dog("Rex", 5, 10.0);
            assertEquals("Rex", d.getName());
            assertEquals(5, d.getAge());
            assertEquals(10.0, d.getMass());
        }

        // ---------- getFeedInfoKg ----------

        @Test
        @DisplayName("Cat.getFeedInfoKg = mass * 0.1")
        void catFeed() {
            assertEquals(0.45, new Cat("M", 1, 4.5).getFeedInfoKg());
        }

        @Test
        @DisplayName("Dog.getFeedInfoKg = mass * 0.3")
        void dogFeed() {
            assertEquals(3.0, new Dog("R", 1, 10.0).getFeedInfoKg());
        }

        @ParameterizedTest(name = "mass = {0}")
        @CsvSource({
                "0.0",
                "1.0",
                "0.1",
                "-1.0",
                "4.9E-324",   // Double.MIN_VALUE
                "1.7976931348623157E308" // Double.MAX_VALUE
        })
        @DisplayName("getFeedInfoKg на границах double (Cat: *0.1, Dog: *0.3)")
        void feedBoundaries(double mass) {
            assertEquals(mass * 0.1, new Cat("X", 1, mass).getFeedInfoKg());
            assertEquals(mass * 0.3, new Dog("X", 1, mass).getFeedInfoKg());
        }

        @Test
        @DisplayName("getFeedInfoKg: NaN / Infinity пробрасываются как есть")
        void feedNaNInfinity() {
            assertTrue(Double.isNaN(new Cat("X", 1, Double.NaN).getFeedInfoKg()));
            assertEquals(Double.POSITIVE_INFINITY,
                    new Dog("X", 1, Double.POSITIVE_INFINITY).getFeedInfoKg());
            assertEquals(Double.NEGATIVE_INFINITY,
                    new Dog("X", 1, Double.NEGATIVE_INFINITY).getFeedInfoKg());
        }

        // ---------- toString ----------

        @Test
        @DisplayName("Cat.toString включает feed")
        void catToString() {
            assertEquals(
                    "Cat name = Murka, age = 3, mass = 4.5, feed = 0.45",
                    new Cat("Murka", 3, 4.5).toString());
        }

        @Test
        @DisplayName("Dog.toString включает feed")
        void dogToString() {
            assertEquals(
                    "Dog name = Rex, age = 5, mass = 10.0, feed = 3.0",
                    new Dog("Rex", 5, 10.0).toString());
        }

        // ---------- границы полей ----------

        @ParameterizedTest(name = "age = {0}")
        @CsvSource({"0", "1", "-1", "2147483647", "-2147483648"})
        @DisplayName("Возраст принимается любым (валидация — в readPets)")
        void ageBoundaries(int age) {
            assertEquals(age, new Cat("X", age, 1.0).getAge());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null / пустое имя не валит конструктор")
        void nullOrEmptyName(String name) {
            assertEquals(name, new Cat(name, 1, 1.0).getName());
        }
    }

    // ==========================================================
    //  2. readInt
    // ==========================================================

    @Nested
    @DisplayName("readInt")
    class ReadIntTests {

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
        @DisplayName("Нечисловой ввод — сообщение и повтор")
        void garbageRetries() {
            assertEquals(7, callReadInt("abc\n7\n"));
            assertEquals("Could not parse a number. Please, try again\n", outText());
        }

        @Test
        @DisplayName("Переполнение (> MAX_VALUE) — повтор")
        void overflowRetries() {
            assertEquals(5, callReadInt("2147483648\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Дробное число — NumberFormatException у parseInt, повтор")
        void decimalRetries() {
            assertEquals(3, callReadInt("2.5\n3\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пустая строка — повтор")
        void emptyLineRetries() {
            assertEquals(3, callReadInt("\n3\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пробелы вокруг — NumberFormatException (parseInt не тримит)")
        void whitespaceNotTrimmed() {
            assertEquals(9, callReadInt(" 5 \n9\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Несколько ошибок подряд")
        void multipleErrors() {
            assertEquals(1, callReadInt("x\ny\n1\n"));
            assertEquals(2, countOccurrences(outText(), "Could not parse a number"));
        }

        @Test
        @DisplayName("EOF → NoSuchElementException (текущее поведение)")
        void eofThrows() {
            try (Scanner sc = new Scanner("")) {
                assertThrows(NoSuchElementException.class,
                        () -> FoodDose.readInt(sc));
            }
        }
    }

    // ==========================================================
    //  3. readDouble
    // ==========================================================

    @Nested
    @DisplayName("readDouble")
    class ReadDoubleTests {

        @Test
        @DisplayName("Обычное дробное число")
        void simple() {
            assertEquals(3.14, callReadDouble("3.14\n"));
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Целое число как double")
        void integerAsDouble() {
            assertEquals(5.0, callReadDouble("5\n"));
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "0", "-1", "0.0", "-0.0",
                "1.7976931348623157E308",  // Double.MAX_VALUE
                "4.9E-324",                 // Double.MIN_VALUE
                "1e10", "-1e-10"
        })
        @DisplayName("Границы и формы записи double")
        void boundaries(String value) {
            assertEquals(Double.parseDouble(value), callReadDouble(value + "\n"));
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"NaN", "Infinity", "-Infinity"})
        @DisplayName("NaN / Infinity принимаются (parseDouble их знает)")
        void specialValues(String value) {
            assertEquals(Double.parseDouble(value), callReadDouble(value + "\n"));
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Мусор — сообщение и повтор")
        void garbageRetries() {
            assertEquals(2.5, callReadDouble("abc\n2.5\n"));
            assertEquals("Could not parse a number. Please, try again\n", outText());
        }

        @Test
        @DisplayName("Запятая как разделитель — NumberFormatException, повтор")
        void commaFails() {
            assertEquals(1.5, callReadDouble("1,5\n1.5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Переполнение (> MAX_VALUE) → Infinity без исключения")
        void overflowBecomesInfinity() {
            double v = callReadDouble("1e400\n");
            assertEquals(Double.POSITIVE_INFINITY, v);
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Пустая строка — повтор")
        void emptyLineRetries() {
            assertEquals(1.0, callReadDouble("\n1.0\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("EOF → NoSuchElementException (текущее поведение)")
        void eofThrows() {
            try (Scanner sc = new Scanner("")) {
                assertThrows(NoSuchElementException.class,
                        () -> FoodDose.readDouble(sc));
            }
        }
    }

    // ==========================================================
    //  4. readPets
    // ==========================================================

    @Nested
    @DisplayName("readPets")
    class ReadPetsTests {

        // ---------- size ----------

        @Test
        @DisplayName("size = 0 → пустой список, вывод пуст")
        void zeroSize() {
            assertTrue(callReadPets("0\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список")
        void negativeSize() {
            assertTrue(callReadPets("-1\n").isEmpty());
        }

        @Test
        @DisplayName("size = 1 — минимальный непустой")
        void sizeOne() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n2.0\n").size());
        }

        @Test
        @DisplayName("size мусором — повтор, затем корректно")
        void sizeRetry() {
            List<Animal> pets = callReadPets("abc\n1\ndog\nRex\n1\n2.0\n");
            assertEquals(1, pets.size());
            assertTrue(outText().contains("Could not parse a number"));
        }

        // ---------- типы ----------

        @Test
        @DisplayName("dog создаёт Dog")
        void dog() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n2\n5.5\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertEquals("Rex", pets.get(0).getName());
            assertEquals(2, pets.get(0).getAge());
            assertEquals(5.5, pets.get(0).getMass());
        }

        @Test
        @DisplayName("cat создаёт Cat")
        void cat() {
            List<Animal> pets = callReadPets("1\ncat\nMurka\n3\n4.0\n");
            assertInstanceOf(Cat.class, pets.get(0));
        }

        @Test
        @DisplayName("регистр типа не важен")
        void typeCaseInsensitive() {
            assertEquals(1, callReadPets("1\nDOG\nRex\n1\n2\n").size());
            assertEquals(1, callReadPets("1\nCat\nM\n1\n2\n").size());
            assertEquals(1, callReadPets("1\ndOg\nR\n1\n2\n").size());
        }

        @Test
        @DisplayName("неподдерживаемый тип — сообщение, элемент не добавлен")
        void unsupportedType() {
            assertTrue(callReadPets("1\nhamster\n").isEmpty());
            assertEquals("Incorrect input. Unsupported pet type\n", outText());
        }

        // ---------- возраст ----------

        @Test
        @DisplayName("age = 1 — минимальный валидный")
        void minValidAge() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n2.0\n").get(0).getAge());
        }

        @Test
        @DisplayName("age = 0 — невалиден, mass не читается")
        void ageZero() {
            assertTrue(callReadPets("1\ndog\nRex\n0\n2.0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
            assertFalse(outText().contains("Mass <= 0"),
                    "mass не должна читаться, т.к. age невалиден");
        }

        @Test
        @DisplayName("age = -1 — невалиден")
        void ageNegative() {
            assertTrue(callReadPets("1\ncat\nM\n-1\n2.0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE допустим")
        void maxAge() {
            assertEquals(Integer.MAX_VALUE,
                    callReadPets("1\ndog\nRex\n2147483647\n1.0\n").get(0).getAge());
        }

        @Test
        @DisplayName("age = Integer.MIN_VALUE — Age <= 0")
        void minAge() {
            assertTrue(callReadPets("1\ndog\nRex\n-2147483648\n1.0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("возраст мусором — повтор, затем валиден")
        void ageRetry() {
            List<Animal> pets = callReadPets("1\ndog\nRex\nxyz\n2\n3.0\n");
            assertEquals(2, pets.get(0).getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }

        // ---------- масса ----------

        @Test
        @DisplayName("mass = Double.MIN_VALUE — валидна (mass > 0)")
        void minPositiveMass() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n1\n4.9E-324\n");
            assertEquals(1, pets.size());
            assertEquals(Double.MIN_VALUE, pets.get(0).getMass());
        }

        @Test
        @DisplayName("mass = 0 — невалидна")
        void massZero() {
            assertTrue(callReadPets("1\ndog\nRex\n1\n0\n").isEmpty());
            assertTrue(outText().contains("Mass <= 0"));
        }

        @Test
        @DisplayName("mass = 0.0 — невалидна")
        void massZeroD() {
            assertTrue(callReadPets("1\ndog\nRex\n1\n0.0\n").isEmpty());
            assertTrue(outText().contains("Mass <= 0"));
        }

        @Test
        @DisplayName("mass = -1.0 — невалидна")
        void massNegative() {
            assertTrue(callReadPets("1\ncat\nM\n1\n-1.0\n").isEmpty());
            assertTrue(outText().contains("Mass <= 0"));
        }

        @Test
        @DisplayName("mass = -Double.MIN_VALUE — невалидна (mass > 0)")
        void massNegativeTiny() {
            assertTrue(callReadPets("1\ncat\nM\n1\n-4.9E-324\n").isEmpty());
            assertTrue(outText().contains("Mass <= 0"));
        }

        @Test
        @DisplayName("mass = Double.MAX_VALUE допустима")
        void maxMass() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n1\n1.7976931348623157E308\n");
            assertEquals(1, pets.size());
            assertEquals(Double.MAX_VALUE, pets.get(0).getMass());
        }

        @Test
        @DisplayName("mass мусором — повтор, затем валидна")
        void massRetry() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n1\nabc\n2.5\n");
            assertEquals(2.5, pets.get(0).getMass());
            assertTrue(outText().contains("Could not parse a number"));
        }

        // ---------- имя ----------

        @Test
        @DisplayName("пустое имя допустимо")
        void emptyName() {
            List<Animal> pets = callReadPets("1\ndog\n\n1\n1.0\n");
            assertEquals("", pets.get(0).getName());
        }

        @Test
        @DisplayName("имя с пробелами сохраняется как есть")
        void nameWithSpaces() {
            List<Animal> pets = callReadPets("1\ndog\n  Rex the Great  \n1\n1.0\n");
            assertEquals("  Rex the Great  ", pets.get(0).getName());
        }

        // ---------- комбинации ----------

        @Test
        @DisplayName("несколько животных, порядок сохранён")
        void multiple() {
            List<Animal> pets = callReadPets(
                    "3\ndog\nRex\n2\n5.0\ncat\nMurka\n1\n3.0\ndog\nBobik\n4\n7.0\n");
            assertEquals(3, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertInstanceOf(Cat.class, pets.get(1));
            assertInstanceOf(Dog.class, pets.get(2));
        }

        @Test
        @DisplayName("смесь валидных и невалидных (тип / возраст / масса)")
        void mixed() {
            // 1) hamster          → Unsupported
            // 2) dog Rex age=0    → Age <= 0
            // 3) cat Murka 3 4.0  → OK
            // 4) dog Bobik 1 -1.0 → Mass <= 0
            List<Animal> pets = callReadPets(
                    "4\nhamster\ndog\nRex\n0\ncat\nMurka\n3\n4.0\ndog\nBobik\n1\n-1.0\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Cat.class, pets.get(0));
            String o = outText();
            assertTrue(o.contains("Unsupported pet type"));
            assertTrue(o.contains("Age <= 0"));
            assertTrue(o.contains("Mass <= 0"));
        }

        @Test
        @DisplayName("size больше, чем введено — «пустые» слоты дают Unsupported")
        void sizeLargerThanInput() {
            List<Animal> pets = callReadPets("3\ndog\nRex\n1\n2.0\n\n\n");
            assertEquals(1, pets.size());
            assertEquals(2, countOccurrences(outText(), "Unsupported pet type"));
        }
    }

    // ==========================================================
    //  5. printListPets
    // ==========================================================

    @Nested
    @DisplayName("printListPets")
    class PrintListPetsTests {

        @Test
        @DisplayName("Пустой список — пустой вывод")
        void empty() throws Exception {
            invokePrintListPets(List.of());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Один элемент — одна строка с feed")
        void one() throws Exception {
            invokePrintListPets(List.of(new Dog("Rex", 2, 10.0)));
            assertEquals("Dog name = Rex, age = 2, mass = 10.0, feed = 3.0\n", outText());
        }

        @Test
        @DisplayName("Несколько — по строке на каждый, порядок сохранён")
        void many() throws Exception {
            invokePrintListPets(List.of(
                    new Dog("Rex", 2, 10.0),
                    new Cat("Murka", 1, 4.0)));
            assertEquals(
                    "Dog name = Rex, age = 2, mass = 10.0, feed = 3.0\n" +
                            "Cat name = Murka, age = 1, mass = 4.0, feed = 0.4\n",
                    outText());
        }
    }
}
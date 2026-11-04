package org.example.ListPets;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ListPets — полный набор тестов")
class ListPetsTest {

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
            return ListPets.readInt(sc);
        }
    }

    private List<Animal> callReadPets(String input) {
        setInput(input);
        return ListPets.readPets();
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
    //  1. Доменная модель
    // ==========================================================

    @Nested
    @DisplayName("Модель Animal / Cat / Dog")
    class ModelTests {

        @Test
        @DisplayName("Cat: сохраняет имя и возраст")
        void catStoresFields() {
            Cat c = new Cat("Murka", 3);
            assertEquals("Murka", c.getName());
            assertEquals(3, c.getAge());
        }

        @Test
        @DisplayName("Dog: сохраняет имя и возраст")
        void dogStoresFields() {
            Dog d = new Dog("Rex", 5);
            assertEquals("Rex", d.getName());
            assertEquals(5, d.getAge());
        }

        @Test
        @DisplayName("Cat.toString")
        void catToString() {
            assertEquals("Cat name = Murka, age = 3", new Cat("Murka", 3).toString());
        }

        @Test
        @DisplayName("Dog.toString")
        void dogToString() {
            assertEquals("Dog name = Rex, age = 5", new Dog("Rex", 5).toString());
        }

        @ParameterizedTest(name = "age = {0}")
        @CsvSource({"0", "1", "-1", "2147483647", "-2147483648"})
        @DisplayName("Граничные значения возраста сохраняются как есть")
        void boundaryAges(int age) {
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
        @DisplayName("Переполнение (< MIN_VALUE) — повтор")
        void underflowRetries() {
            assertEquals(5, callReadInt("-2147483649\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пустая строка — повтор")
        void emptyLineRetries() {
            assertEquals(3, callReadInt("\n3\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Пробелы вокруг числа — NumberFormatException (parseInt не тримит)")
        void whitespaceNotTrimmed() {
            assertEquals(9, callReadInt(" 5 \n9\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Несколько сообщений об ошибке подряд")
        void multipleErrors() {
            assertEquals(1, callReadInt("x\ny\n1\n"));
            assertEquals(2, countOccurrences(outText(), "Could not parse a number"));
        }

        @Test
        @DisplayName("EOF на входе → NoSuchElementException (текущее поведение)")
        void eofThrows() {
            try (Scanner sc = new Scanner("")) {
                assertThrows(NoSuchElementException.class,
                        () -> ListPets.readInt(sc));
            }
        }
    }

    // ==========================================================
    //  3. readPets
    // ==========================================================

    @Nested
    @DisplayName("readPets")
    class ReadPetsTests {

        @Test
        @DisplayName("size = 0 → пустой список, ничего не выведено")
        void zeroSize() {
            assertTrue(callReadPets("0\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список (цикл не выполняется)")
        void negativeSize() {
            assertTrue(callReadPets("-1\n").isEmpty());
        }

        @Test
        @DisplayName("size = 1 — минимальный непустой")
        void sizeOne() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n").size());
        }

        @Test
        @DisplayName("size задан мусором — повтор, затем корректно")
        void sizeRetry() {
            List<Animal> pets = callReadPets("abc\n1\ndog\nRex\n2\n");
            assertEquals(1, pets.size());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("size больше, чем реально введено — «пустые» слоты дают Unsupported")
        void sizeLargerThanInput() {
            List<Animal> pets = callReadPets("3\ndog\nRex\n2\n\n\n");
            assertEquals(1, pets.size());
            assertEquals(2, countOccurrences(outText(), "Unsupported pet type"));
        }

        @Test
        @DisplayName("dog создаёт Dog")
        void dog() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n2\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertEquals("Rex", pets.get(0).getName());
            assertEquals(2, pets.get(0).getAge());
        }

        @Test
        @DisplayName("cat создаёт Cat")
        void cat() {
            List<Animal> pets = callReadPets("1\ncat\nMurka\n3\n");
            assertInstanceOf(Cat.class, pets.get(0));
        }

        @Test
        @DisplayName("регистр типа не важен: DOG, Cat, dOg")
        void typeCaseInsensitive() {
            assertEquals(1, callReadPets("1\nDOG\nRex\n1\n").size());
            assertEquals(1, callReadPets("1\nCat\nM\n1\n").size());
            assertEquals(1, callReadPets("1\ndOg\nR\n1\n").size());
        }

        @Test
        @DisplayName("неподдерживаемый тип — сообщение, элемент не добавлен")
        void unsupportedType() {
            assertTrue(callReadPets("1\nhamster\n").isEmpty());
            assertEquals("Incorrect input. Unsupported pet type\n", outText());
        }

        @Test
        @DisplayName("age = 1 — минимальный валидный")
        void minValidAge() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n").get(0).getAge());
        }

        @Test
        @DisplayName("age = 0 — невалиден")
        void ageZero() {
            assertTrue(callReadPets("1\ndog\nRex\n0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = -1 — невалиден")
        void ageNegative() {
            assertTrue(callReadPets("1\ncat\nM\n-1\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE допустим")
        void maxAge() {
            List<Animal> pets = callReadPets("1\ndog\nRex\n2147483647\n");
            assertEquals(Integer.MAX_VALUE, pets.get(0).getAge());
        }

        @Test
        @DisplayName("age = Integer.MIN_VALUE — невалиден (Age <= 0)")
        void minAge() {
            assertTrue(callReadPets("1\ndog\nRex\n-2147483648\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("возраст задан мусором — повтор, затем валиден")
        void ageRetry() {
            List<Animal> pets = callReadPets("1\ndog\nRex\nxyz\n2\n");
            assertEquals(1, pets.size());
            assertEquals(2, pets.get(0).getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("пустое имя допустимо")
        void emptyName() {
            List<Animal> pets = callReadPets("1\ndog\n\n1\n");
            assertEquals(1, pets.size());
            assertEquals("", pets.get(0).getName());
        }

        @Test
        @DisplayName("имя с пробелами сохраняется как есть")
        void nameWithSpaces() {
            List<Animal> pets = callReadPets("1\ndog\n  Rex the Great  \n1\n");
            assertEquals("  Rex the Great  ", pets.get(0).getName());
        }

        @Test
        @DisplayName("несколько животных подряд, порядок сохранён")
        void multiple() {
            List<Animal> pets = callReadPets("3\ndog\nRex\n2\ncat\nMurka\n1\ndog\nBobik\n4\n");
            assertEquals(3, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertInstanceOf(Cat.class, pets.get(1));
            assertInstanceOf(Dog.class, pets.get(2));
        }

        @Test
        @DisplayName("смесь валидных и невалидных")
        void mixed() {
            List<Animal> pets = callReadPets("3\nhamster\ndog\nRex\n0\ncat\nMurka\n3\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Cat.class, pets.get(0));
            String out = outText();
            assertTrue(out.contains("Unsupported pet type"));
            assertTrue(out.contains("Age <= 0"));
        }
    }

    // ==========================================================
    //  4. printListPets
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
        @DisplayName("Один элемент — одна строка")
        void one() throws Exception {
            invokePrintListPets(List.of(new Dog("Rex", 2)));
            assertEquals("Dog name = Rex, age = 2\n", outText());
        }

        @Test
        @DisplayName("Несколько элементов — по строке на каждый, порядок сохранён")
        void many() throws Exception {
            invokePrintListPets(List.of(
                    new Dog("Rex", 2),
                    new Cat("Murka", 1),
                    new Dog("Bobik", 4)));
            assertEquals(
                    "Dog name = Rex, age = 2\n" +
                            "Cat name = Murka, age = 1\n" +
                            "Dog name = Bobik, age = 4\n",
                    outText());
        }

        /** printListPets(List<Animal>) пишет в System.out (мы его уже подменили в setUp). */
        private void invokePrintListPets(List<Animal> pets) throws Exception {
            // если printListPets стал package-private (не private) — можно вызвать напрямую:
            // ListPets.printListPets(pets);
            // Если остался private — через рефлексию:
            var m = ListPets.class.getDeclaredMethod("printListPets", List.class);
            m.setAccessible(true);
            m.invoke(null, pets);
        }
    }
}
package org.example.IterPets;

import org.example.IterPets.model.Animal;
import org.example.IterPets.model.AnimalIterator;
import org.example.IterPets.model.Cat;
import org.example.IterPets.model.Dog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IterPets — полный набор тестов")
class IterPetsTest {

    // ==========================================================
    //  Общие утилиты
    // ==========================================================

    private ByteArrayOutputStream outBuf;
    private PrintStream out;
    private PrintStream originalOut;
    private InputStream originalIn;

    private static final Method M_READ_INT;
    private static final Method M_READ_PETS;
    private static final Method M_READ_PET;
    private static final Method M_CREATE_PET;
    private static final Method M_PRINT_LIST;

    static {
        try {
            M_READ_INT    = IterPets.class.getDeclaredMethod("readInt", Scanner.class);
            M_READ_PETS   = IterPets.class.getDeclaredMethod("readPets");
            M_READ_PET    = IterPets.class.getDeclaredMethod("readPet", Scanner.class);
            M_CREATE_PET  = IterPets.class.getDeclaredMethod("createPet", String.class, String.class, Integer.class);
            M_PRINT_LIST  = IterPets.class.getDeclaredMethod("printListPets", List.class);
            for (Method m : new Method[]{M_READ_INT, M_READ_PETS, M_READ_PET, M_CREATE_PET, M_PRINT_LIST}) {
                m.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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

    // ---------- проброс настоящего исключения сквозь reflection ----------

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    private static RuntimeException rethrow(Throwable t) {
        Throwable cause = (t instanceof InvocationTargetException ite) ? ite.getCause() : t;
        return IterPetsTest.<RuntimeException>sneakyThrow(cause);
    }

    // ---------- обёртки ----------

    private int callReadInt(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (int) M_READ_INT.invoke(null, sc);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Animal> callReadPet(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (Optional<Animal>) M_READ_PET.invoke(null, sc);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Animal> callReadPets(String input) {
        setInput(input);
        try {
            return (List<Animal>) M_READ_PETS.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    private Animal callCreatePet(String type, String name, Integer age) {
        try {
            return (Animal) M_CREATE_PET.invoke(null, type, name, age);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void callPrintListPets(List<Animal> pets) {
        try {
            M_PRINT_LIST.invoke(null, pets);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    // ==========================================================
    //  1. Модель
    // ==========================================================

    @Nested
    @DisplayName("Модель Animal / Cat / Dog")
    class ModelTests {

        @Test
        @DisplayName("Dog: поля + toString")
        void dog() {
            Dog d = new Dog("Rex", 3);
            assertEquals("Rex", d.getName());
            assertEquals(3, d.getAge());
            assertEquals("Dog name = Rex, age = 3", d.toString());
        }

        @Test
        @DisplayName("Cat: поля + toString")
        void cat() {
            Cat c = new Cat("Murka", 2);
            assertEquals("Murka", c.getName());
            assertEquals(2, c.getAge());
            assertEquals("Cat name = Murka, age = 2", c.toString());
        }

        @ParameterizedTest(name = "age = {0}")
        @CsvSource({"0", "1", "-1", "2147483647", "-2147483648"})
        @DisplayName("Возраст сохраняется как есть")
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
        void boundaries(String v) {
            assertEquals(Integer.parseInt(v), callReadInt(v + "\n"));
        }

        @Test
        @DisplayName("Мусор → сообщение и повтор")
        void garbageRetries() {
            assertEquals(7, callReadInt("abc\n7\n"));
            assertEquals("Could not parse a number. Please, try again\n", outText());
        }

        @Test
        @DisplayName("Переполнение сверху — повтор")
        void overflowRetries() {
            assertEquals(5, callReadInt("2147483648\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Переполнение снизу — повтор")
        void underflowRetries() {
            assertEquals(5, callReadInt("-2147483649\n5\n"));
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Дробное — повтор")
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
        @DisplayName("Пробелы вокруг — NumberFormatException, повтор")
        void whitespaceNotTrimmed() {
            assertEquals(9, callReadInt(" 5 \n9\n"));
        }

        @Test
        @DisplayName("Несколько ошибок подряд")
        void multipleErrors() {
            assertEquals(1, callReadInt("x\ny\n1\n"));
            assertEquals(2, countOccurrences(outText(), "Could not parse a number"));
        }

        @Test
        @DisplayName("EOF → NoSuchElementException")
        void eofThrows() {
            assertThrows(NoSuchElementException.class, () -> callReadInt(""));
        }
    }

    // ==========================================================
    //  3. createPet
    // ==========================================================

    @Nested
    @DisplayName("createPet")
    class CreatePetTests {

        @ParameterizedTest(name = "type={0} → {3}")
        @CsvSource({"dog, Rex, 1, Dog", "cat, Murka, 2, Cat"})
        @DisplayName("Валидные типы")
        void valid(String type, String name, int age, String cls) {
            Animal a = callCreatePet(type, name, age);
            assertEquals(cls, a.getClass().getSimpleName());
            assertEquals(name, a.getName());
            assertEquals(age, a.getAge());
        }

        @ParameterizedTest(name = "type=''{0}''")
        @ValueSource(strings = {"DOG", "Cat", "dog ", "", "   ", "hamster", "rabbit"})
        @DisplayName("Некорректный type → IllegalStateException")
        void unexpected(String type) {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> callCreatePet(type, "X", 1));
            assertTrue(e.getMessage().contains("Unexpected value"));
        }

        @Test
        @DisplayName("null type → NPE")
        void nullType() {
            assertThrows(NullPointerException.class,
                    () -> callCreatePet(null, "X", 1));
        }

        @Test
        @DisplayName("age = null допустим на уровне createPet")
        void nullAge() {
            assertNull(callCreatePet("dog", "Rex", null).getAge());
        }
    }

    // ==========================================================
    //  4. readPet
    // ==========================================================

    @Nested
    @DisplayName("readPet")
    class ReadPetTests {

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

        @ParameterizedTest(name = "raw=''{0}''")
        @ValueSource(strings = {"DOG", "Dog", "  dog  ", "\tcat\t", " CAT "})
        @DisplayName("trim + toLowerCase для type")
        void normalization(String raw) {
            assertTrue(callReadPet(raw + "\nX\n1\n").isPresent());
        }

        @ParameterizedTest(name = "type=''{0}''")
        @ValueSource(strings = {"hamster", "rabbit", "", "   ", "do g"})
        @DisplayName("Некорректный type → empty + сообщение")
        void unsupportedType(String type) {
            assertTrue(callReadPet(type + "\n").isEmpty());
            assertEquals("Incorrect input. Unsupported pet type\n", outText());
        }

        @Test
        @DisplayName("Пустое имя допустимо")
        void emptyName() {
            assertEquals("", callReadPet("dog\n\n1\n").orElseThrow().getName());
        }

        @Test
        @DisplayName("Имя с пробелами сохраняется")
        void nameWithSpaces() {
            assertEquals("  Rex  ",
                    callReadPet("dog\n  Rex  \n1\n").orElseThrow().getName());
        }

        @Test
        @DisplayName("age = 1 — минимальный валидный")
        void minAge() {
            assertEquals(1, callReadPet("dog\nRex\n1\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = 0 → empty + сообщение")
        void ageZero() {
            assertTrue(callReadPet("dog\nRex\n0\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = -1 → empty + сообщение")
        void ageNegative() {
            assertTrue(callReadPet("dog\nRex\n-1\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = MAX_VALUE допустим")
        void maxAge() {
            assertEquals(Integer.MAX_VALUE,
                    callReadPet("dog\nRex\n2147483647\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = MIN_VALUE → empty")
        void minAgeInt() {
            assertTrue(callReadPet("dog\nRex\n-2147483648\n").isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age мусором → повтор, затем валиден")
        void ageRetry() {
            assertEquals(2, callReadPet("dog\nRex\nxyz\n2\n").orElseThrow().getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Невалидный type не съедает name/age")
        void invalidTypeDoesNotConsume() {
            try (Scanner sc = new Scanner("rabbit\nIGNORED\n42\n")) {
                Optional<Animal> p;
                try {
                    @SuppressWarnings("unchecked")
                    Optional<Animal> r = (Optional<Animal>) M_READ_PET.invoke(null, sc);
                    p = r;
                } catch (ReflectiveOperationException e) {
                    throw rethrow(e);
                }
                assertTrue(p.isEmpty());
                assertEquals("IGNORED", sc.nextLine());
            }
        }
    }

    // ==========================================================
    //  5. readPets()
    // ==========================================================

    @Nested
    @DisplayName("readPets")
    class ReadPetsTests {

        @Test
        @DisplayName("size = 0 → пустой список")
        void zero() {
            assertTrue(callReadPets("0\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список")
        void negative() {
            assertTrue(callReadPets("-1\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size = 1 — минимальный непустой")
        void sizeOne() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n").size());
        }

        @Test
        @DisplayName("size мусором → повтор, потом корректно")
        void sizeRetry() {
            List<Animal> pets = callReadPets("abc\n1\ndog\nRex\n1\n");
            assertEquals(1, pets.size());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Несколько валидных, порядок сохранён")
        void multiple() {
            List<Animal> pets = callReadPets("3\ndog\nRex\n2\ncat\nMurka\n1\ndog\nBobik\n4\n");
            assertEquals(3, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertInstanceOf(Cat.class, pets.get(1));
            assertInstanceOf(Dog.class, pets.get(2));
            assertEquals("Rex", pets.get(0).getName());
            assertEquals("Murka", pets.get(1).getName());
            assertEquals("Bobik", pets.get(2).getName());
        }

        @Test
        @DisplayName("Смесь валидных и невалидных")
        void mixed() {
            List<Animal> pets = callReadPets("3\nrabbit\ndog\nRex\n0\ncat\nMurka\n3\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Cat.class, pets.get(0));
            String o = outText();
            assertTrue(o.contains("Unsupported pet type"));
            assertTrue(o.contains("Age <= 0"));
        }

        @Test
        @DisplayName("size больше, чем введено — оставшиеся строки съедаются как type")
        void sizeLargerThanInput() {
            // size = 3, введено только одно животное + пустые строки
            List<Animal> pets = callReadPets("3\ndog\nRex\n1\n\n\n");
            assertEquals(1, pets.size());
            assertEquals(2, countOccurrences(outText(), "Unsupported pet type"));
        }
    }

    // ==========================================================
    //  6. AnimalIterator
    // ==========================================================

    @Nested
    @DisplayName("AnimalIterator")
    class AnimalIteratorTests {

        @Test
        @DisplayName("Пустой список → hasNext сразу false")
        void empty() {
            AnimalIterator it = new AnimalIterator(List.of());
            assertFalse(it.hasNext());
        }

        @Test
        @DisplayName("Один элемент: hasNext true → next → hasNext false")
        void single() {
            Dog d = new Dog("Rex", 1);
            AnimalIterator it = new AnimalIterator(List.of(d));
            assertTrue(it.hasNext());
            assertSame(d, it.next());
            assertFalse(it.hasNext());
        }

        @Test
        @DisplayName("Несколько элементов: перебираются в порядке списка")
        void multipleInOrder() {
            Dog d1 = new Dog("A", 1);
            Cat c = new Cat("B", 2);
            Dog d2 = new Dog("C", 3);
            AnimalIterator it = new AnimalIterator(List.of(d1, c, d2));
            assertSame(d1, it.next());
            assertSame(c, it.next());
            assertSame(d2, it.next());
            assertFalse(it.hasNext());
        }

        @Test
        @DisplayName("hasNext идемпотентен (несколько вызовов подряд без next)")
        void hasNextIdempotent() {
            AnimalIterator it = new AnimalIterator(List.of(new Dog("R", 1)));
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
            assertNotNull(it.next());
            assertFalse(it.hasNext());
            assertFalse(it.hasNext());
        }

        @Test
        @DisplayName("next() на пустом — IllegalArgumentException или NoSuchElementException (зависит от реализации)")
        void nextOnEmpty() {
            AnimalIterator it = new AnimalIterator(List.of());
            // Если AnimalIterator.get(0) на пустом — IndexOutOfBounds.
            // Если AnimalIterator бросает NoSuchElementException — увидите его.
            // Фиксируем текущее поведение: assertThrows(RuntimeException.class, it::next)
            assertThrows(RuntimeException.class, it::next);
        }

        @Test
        @DisplayName("Проход по неизменяемому списку не падает")
        void immutableList() {
            AnimalIterator it = new AnimalIterator(List.of(
                    new Dog("A", 1), new Cat("B", 2)));
            int count = 0;
            while (it.hasNext()) { it.next(); count++; }
            assertEquals(2, count);
        }

        @Test
        @DisplayName("AnimalIterator не мутирует исходный список")
        void doesNotMutateSource() {
            List<Animal> source = new ArrayList<>(List.of(
                    new Dog("A", 1), new Cat("B", 2)));
            AnimalIterator it = new AnimalIterator(source);
            while (it.hasNext()) it.next();
            assertEquals(2, source.size());
        }
    }

    // ==========================================================
    //  7. printListPets (использует AnimalIterator)
    // ==========================================================

    @Nested
    @DisplayName("printListPets")
    class PrintListPetsTests {

        @Test
        @DisplayName("Пустой список → пустой вывод")
        void empty() {
            callPrintListPets(List.of());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Один элемент — одна строка")
        void one() {
            callPrintListPets(List.of(new Dog("Rex", 2)));
            assertEquals("Dog name = Rex, age = 2\n", outText());
        }

        @Test
        @DisplayName("Несколько — по строке на каждый, порядок сохранён")
        void many() {
            callPrintListPets(List.of(
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
            callPrintListPets(List.of(new Cat("M", 1)));
            assertEquals("Cat name = M, age = 1\n", outText());
        }

        @Test
        @DisplayName("Ровно N строк для списка из N")
        void lineCountMatches() {
            callPrintListPets(List.of(
                    new Dog("A", 1),
                    new Cat("B", 2),
                    new Dog("C", 3),
                    new Cat("D", 4)));
            String o = outText();
            assertEquals(4, countOccurrences(o, "\n"));
        }
    }

    // ==========================================================
    //  8. Интеграция: readPets → printListPets
    // ==========================================================

    @Nested
    @DisplayName("Интеграция")
    class IntegrationTests {

        @Test
        @DisplayName("Сквозной сценарий: ввод → вывод через AnimalIterator")
        void endToEnd() {
            List<Animal> pets = callReadPets("2\ndog\nRex\n1\ncat\nMurka\n2\n");
            callPrintListPets(pets);
            assertEquals(
                    "Dog name = Rex, age = 1\n" +
                            "Cat name = Murka, age = 2\n",
                    outText());
        }
    }
}
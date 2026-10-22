package org.example.ListsHerbiAndOmniPets;

import org.example.ListsHerbiAndOmniPets.interfaces.Herbivore;
import org.example.ListsHerbiAndOmniPets.interfaces.Omnivore;
import org.example.ListsHerbiAndOmniPets.model.*;
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

@DisplayName("ListsHerbiAndOmniPets — полный набор тестов")
class ListsHerbiAndOmniPetsTest {

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
            M_READ_INT    = ListsHerbiAndOmniPets.class.getDeclaredMethod("readInt", Scanner.class);
            M_READ_PETS   = ListsHerbiAndOmniPets.class.getDeclaredMethod("readPets");
            M_READ_PET    = ListsHerbiAndOmniPets.class.getDeclaredMethod("readPet", Scanner.class);
            M_CREATE_PET  = ListsHerbiAndOmniPets.class.getDeclaredMethod("createPet", String.class, String.class, Integer.class);
            M_PRINT_LIST  = ListsHerbiAndOmniPets.class.getDeclaredMethod("printListPets", List.class);
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

    private Throwable unwrap(Throwable t) {
        return (t instanceof InvocationTargetException ite) ? ite.getCause() : t;
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0, idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    // ---------- обёртки над приватными методами ----------

    private int callReadInt(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (int) M_READ_INT.invoke(null, sc);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Animal> callReadPet(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (Optional<Animal>) M_READ_PET.invoke(null, sc);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Animal> callReadPets(String input) {
        setInput(input);
        try {
            return (List<Animal>) M_READ_PETS.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    private Animal callCreatePet(String type, String name, Integer age) {
        try {
            return (Animal) M_CREATE_PET.invoke(null, type, name, age);
        } catch (ReflectiveOperationException e) {
            // Извлекаем оригинальную ошибку (например, IllegalStateException)
            Throwable original = unwrap(e);

            if (original instanceof RuntimeException) {
                throw (RuntimeException) original; // Выбрасываем оригинал как есть
            }
            throw new RuntimeException(original); // Для checked-исключений, если они будут
        }
    }

    @SuppressWarnings("unchecked")
    private void invokePrintListPets(List<Animal> pets) {
        try {
            M_PRINT_LIST.invoke(null, pets);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    // ==========================================================
    //  1. Модель
    // ==========================================================

    @Nested
    @DisplayName("Модель Animal и наследники")
    class ModelTests {

        @Test
        @DisplayName("Cat: поля + hunt + toString")
        void cat() {
            Cat c = new Cat("Murka", 3);
            assertEquals("Murka", c.getName());
            assertEquals(3, c.getAge());
            assertEquals("I can hunt for mice", c.hunt());
            assertEquals("Cat name = Murka, age = 3. I can hunt for mice", c.toString());
        }

        @Test
        @DisplayName("Dog: поля + hunt + toString")
        void dog() {
            Dog d = new Dog("Rex", 5);
            assertEquals("Rex", d.getName());
            assertEquals(5, d.getAge());
            assertEquals("I can hunt for robbers", d.hunt());
            assertEquals("Dog name = Rex, age = 5. I can hunt for robbers", d.toString());
        }

        @Test
        @DisplayName("Hamster: поля + chill + toString")
        void hamster() {
            Hamster h = new Hamster("Homa", 1);
            assertEquals("Homa", h.getName());
            assertEquals(1, h.getAge());
            assertEquals("I can chill for 8 hours", h.chill());
            assertEquals("Hamster name = Homa, age = 1. I can chill for 8 hours", h.toString());
        }

        @Test
        @DisplayName("GuineaPig: поля + chill + toString")
        void guinea() {
            GuineaPig g = new GuineaPig("Piggy", 2);
            assertEquals("Piggy", g.getName());
            assertEquals(2, g.getAge());
            assertEquals("I can chill for 12 hours", g.chill());
            assertEquals("GuineaPig name = Piggy, age = 2. I can chill for 12 hours", g.toString());
        }

        // ---------- контракты интерфейсов ----------

        @Test
        @DisplayName("Herbivore: только Hamster и GuineaPig")
        void herbivoreSet() {
            assertInstanceOf(Herbivore.class, new Hamster("H", 1));
            assertInstanceOf(Herbivore.class, new GuineaPig("G", 1));
            assertFalse(new Dog("D", 1) instanceof Herbivore);
            assertFalse(new Cat("C", 1) instanceof Herbivore);
        }

        @Test
        @DisplayName("Omnivore: только Dog и Cat")
        void omnivoreSet() {
            assertInstanceOf(Omnivore.class, new Dog("D", 1));
            assertInstanceOf(Omnivore.class, new Cat("C", 1));
            assertFalse(new Hamster("H", 1) instanceof Omnivore);
            assertFalse(new GuineaPig("G", 1) instanceof Omnivore);
        }

        // ---------- границы полей ----------

        @ParameterizedTest(name = "age = {0}")
        @CsvSource({"0", "1", "-1", "2147483647", "-2147483648"})
        @DisplayName("Возраст принимается любым (валидация — в readPet)")
        void ageBoundaries(int age) {
            assertEquals(age, new Cat("X", age).getAge());
            assertEquals(age, new Dog("X", age).getAge());
            assertEquals(age, new Hamster("X", age).getAge());
            assertEquals(age, new GuineaPig("X", age).getAge());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null / пустое имя не валит конструктор")
        void nullOrEmptyName(String name) {
            assertEquals(name, new Cat(name, 1).getName());
            assertEquals(name, new Dog(name, 1).getName());
            assertEquals(name, new Hamster(name, 1).getName());
            assertEquals(name, new GuineaPig(name, 1).getName());
        }
    }

    // ==========================================================
    //  2. TYPES
    // ==========================================================

    @Nested
    @DisplayName("TYPES")
    class TypesTests {

        @Test
        @DisplayName("Ровно 4 элемента")
        void size() {
            assertEquals(4, ListsHerbiAndOmniPets.TYPES.size());
        }

        @Test
        @DisplayName("Содержит ровно dog/cat/hamster/guinea в нижнем регистре")
        void contents() {
            assertEquals(Set.of("dog", "cat", "hamster", "guinea"),
                    ListsHerbiAndOmniPets.TYPES);
        }

        @Test
        @DisplayName("Все в нижнем регистре")
        void allLowercase() {
            for (String t : ListsHerbiAndOmniPets.TYPES) {
                assertEquals(t.toLowerCase(), t);
            }
        }

        @Test
        @DisplayName("Иммутабельный — add бросает UnsupportedOperationException")
        void immutable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> ListsHerbiAndOmniPets.TYPES.add("rabbit"));
        }
    }

    // ==========================================================
    //  3. readInt
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
        @DisplayName("Мусор — сообщение и повтор")
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
                Throwable t = assertThrows(Throwable.class,
                        () -> M_READ_INT.invoke(null, sc));
                assertInstanceOf(NoSuchElementException.class, unwrap(t));
            }
        }
    }

    // ==========================================================
    //  4. createPet
    // ==========================================================

    @Nested
    @DisplayName("createPet")
    class CreatePetTests {

        @ParameterizedTest(name = "type = {0}")
        @CsvSource({
                "dog,    Rex,   1, Dog",
                "cat,    Murka, 2, Cat",
                "hamster,Homa,  3, Hamster",
                "guinea, Piggy, 4, GuineaPig"
        })
        @DisplayName("Корректный type → ожидаемый класс, имя и возраст пробрасываются")
        void valid(String type, String name, int age, String expectedClass) {
            Animal a = callCreatePet(type, name, age);
            assertEquals(expectedClass, a.getClass().getSimpleName());
            assertEquals(name, a.getName());
            assertEquals(age, a.getAge());
        }

        @ParameterizedTest(name = "type = {0}")
        @ValueSource(strings = {"DOG", "Cat", "Hamster ", " rabbit", "hamster\n", "", "   "})
        @DisplayName("Регистр/пробелы/пустое — default → IllegalStateException")
        void unexpected(String type) {
            Throwable t = assertThrows(Throwable.class,
                    () -> callCreatePet(type, "X", 1));
            assertInstanceOf(IllegalStateException.class, t);
            assertTrue(t.getMessage().contains("Unexpected value"));
        }

        @Test
        @DisplayName("null type → switch NPE (текущее поведение)")
        void nullType() {
            Throwable t = assertThrows(Throwable.class,
                    () -> callCreatePet(null, "X", 1));
            // switch по null кидает NPE до default
            assertInstanceOf(RuntimeException.class, t);
        }

        @Test
        @DisplayName("age = null допустим на уровне createPet")
        void nullAge() {
            Animal a = callCreatePet("dog", "Rex", null);
            assertNull(a.getAge());
        }
    }

    // ==========================================================
    //  5. readPet
    // ==========================================================

    @Nested
    @DisplayName("readPet")
    class ReadPetTests {

        @Test
        @DisplayName("dog — валидный")
        void dog() {
            Optional<Animal> pet = callReadPet("dog\nRex\n2\n");
            assertTrue(pet.isPresent());
            assertInstanceOf(Dog.class, pet.get());
            assertEquals("Rex", pet.get().getName());
            assertEquals(2, pet.get().getAge());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("cat — валидный")
        void cat() {
            Optional<Animal> pet = callReadPet("cat\nMurka\n3\n");
            assertInstanceOf(Cat.class, pet.orElseThrow());
        }

        @Test
        @DisplayName("hamster — валидный")
        void hamster() {
            Optional<Animal> pet = callReadPet("hamster\nHoma\n1\n");
            assertInstanceOf(Hamster.class, pet.orElseThrow());
        }

        @Test
        @DisplayName("guinea — валидный")
        void guinea() {
            Optional<Animal> pet = callReadPet("guinea\nPiggy\n2\n");
            assertInstanceOf(GuineaPig.class, pet.orElseThrow());
        }

        // ---------- type: trim + toLowerCase ----------

        @ParameterizedTest(name = "input type = ''{0}''")
        @ValueSource(strings = {"DOG", "Dog", "dOg", "  dog  ", "\tdog\t", "CAT", "  Guinea  "})
        @DisplayName("Регистр и пробелы вокруг type нормализуются")
        void typeNormalization(String rawType) {
            String name = "X";
            Optional<Animal> pet = callReadPet(rawType + "\n" + name + "\n1\n");
            assertTrue(pet.isPresent(), "ожидался валидный питомец для type='" + rawType + "'");
        }

        @Test
        @DisplayName("Внутренние пробелы в type не нормализуются — Unsupported")
        void typeInnerSpaces() {
            Optional<Animal> pet = callReadPet("gu inea\nX\n1\n");
            assertTrue(pet.isEmpty());
            assertTrue(outText().contains("Unsupported pet type"));
        }

        @ParameterizedTest(name = "type = ''{0}''")
        @ValueSource(strings = {"rabbit", "fish", "cow", "", "   ", "dogy", "guineapig"})
        @DisplayName("Неподдерживаемый type → empty + сообщение")
        void unsupportedType(String type) {
            Optional<Animal> pet = callReadPet(type + "\n");
            assertTrue(pet.isEmpty());
            assertEquals("Incorrect input. Unsupported pet type\n", outText());
        }

        // ---------- name ----------

        @Test
        @DisplayName("Пустое имя допустимо")
        void emptyName() {
            Optional<Animal> pet = callReadPet("dog\n\n1\n");
            assertTrue(pet.isPresent());
            assertEquals("", pet.get().getName());
        }

        @Test
        @DisplayName("Имя с пробелами сохраняется как есть (name не тримится)")
        void nameWithSpaces() {
            Optional<Animal> pet = callReadPet("dog\n  Rex the Great  \n1\n");
            assertEquals("  Rex the Great  ", pet.orElseThrow().getName());
        }

        // ---------- age ----------

        @Test
        @DisplayName("age = 1 — минимальный валидный")
        void minValidAge() {
            Optional<Animal> pet = callReadPet("dog\nRex\n1\n");
            assertEquals(1, pet.orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = 0 → empty + сообщение")
        void ageZero() {
            Optional<Animal> pet = callReadPet("dog\nRex\n0\n");
            assertTrue(pet.isEmpty());
            assertEquals("Incorrect input. Age <= 0\n", outText());
        }

        @Test
        @DisplayName("age = -1 → empty + сообщение")
        void ageNegative() {
            Optional<Animal> pet = callReadPet("dog\nRex\n-1\n");
            assertTrue(pet.isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE допустим")
        void maxAge() {
            Optional<Animal> pet = callReadPet("dog\nRex\n2147483647\n");
            assertEquals(Integer.MAX_VALUE, pet.orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = Integer.MIN_VALUE → empty")
        void minAge() {
            Optional<Animal> pet = callReadPet("dog\nRex\n-2147483648\n");
            assertTrue(pet.isEmpty());
            assertTrue(outText().contains("Age <= 0"));
        }

        @Test
        @DisplayName("age мусором → повтор, затем валидный")
        void ageRetry() {
            Optional<Animal> pet = callReadPet("dog\nRex\nxyz\n2\n");
            assertEquals(2, pet.orElseThrow().getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }

        // ---------- порядок валидаций ----------

        @Test
        @DisplayName("Невалидный type: name и age НЕ читаются (Scanner остаётся на следующей строке)")
        void invalidTypeStopsBeforeNameAndAge() {
            try (Scanner sc = new Scanner("hamster2\nIGNORED\n42\n")) {
                Optional<Animal> pet = callReadPetWithScanner(sc);
                assertTrue(pet.isEmpty());
                assertTrue(outText().contains("Unsupported pet type"));
                // следующая строка после "hamster2" — "IGNORED", значит name/age не трогали
                assertEquals("IGNORED", sc.nextLine());
            } catch (Exception e) {
                fail(e);
            }
        }

        @Test
        @DisplayName("Невалидный age: Optional.empty, mass-подобных полей нет")
        void invalidAgeReturnsEmpty() {
            assertTrue(callReadPet("dog\nRex\n0\n").isEmpty());
        }

        private Optional<Animal> callReadPetWithScanner(Scanner sc) throws Exception {
            @SuppressWarnings("unchecked")
            Optional<Animal> r = (Optional<Animal>) M_READ_PET.invoke(null, sc);
            return r;
        }
    }

    // ==========================================================
    //  6. readPets
    // ==========================================================

    @Nested
    @DisplayName("readPets")
    class ReadPetsTests {

        @Test
        @DisplayName("size = 0 → пустой список, вывод пуст")
        void zeroSize() {
            assertTrue(callReadPets("0\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size < 0 → пустой список (for не выполняется)")
        void negativeSize() {
            assertTrue(callReadPets("-1\n").isEmpty());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("size = 1 — минимальный непустой")
        void sizeOne() {
            assertEquals(1, callReadPets("1\ndog\nRex\n1\n").size());
        }

        @Test
        @DisplayName("size мусором → повтор, затем корректно")
        void sizeRetry() {
            List<Animal> pets = callReadPets("abc\n1\ndog\nRex\n1\n");
            assertEquals(1, pets.size());
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("size больше, чем валидных — невалидные слоты не добавляются")
        void sizeWithInvalidSlots() {
            // 3 слота: hamster(ok), rabbit(invalid type), dog age=0 (invalid age)
            List<Animal> pets = callReadPets("3\nhamster\nHoma\n1\nrabbit\ndog\nRex\n0\n");
            assertEquals(1, pets.size());
            assertInstanceOf(Hamster.class, pets.get(0));
            String o = outText();
            assertTrue(o.contains("Unsupported pet type"));
            assertTrue(o.contains("Age <= 0"));
        }

        @Test
        @DisplayName("Несколько валидных, порядок сохранён")
        void multiple() {
            List<Animal> pets = callReadPets(
                    "4\ndog\nRex\n2\ncat\nMurka\n1\nhamster\nHoma\n3\nguinea\nPiggy\n4\n");
            assertEquals(4, pets.size());
            assertInstanceOf(Dog.class, pets.get(0));
            assertInstanceOf(Cat.class, pets.get(1));
            assertInstanceOf(Hamster.class, pets.get(2));
            assertInstanceOf(GuineaPig.class, pets.get(3));
        }

        @Test
        @DisplayName("Все типы невалидны — пустой список")
        void allInvalid() {
            List<Animal> pets = callReadPets("3\nrabbit\ndog\nRex\n0\nfish\n");
            assertTrue(pets.isEmpty());
        }

        @Test
        @DisplayName("Возраст мусором в середине — повтор, затем валидный")
        void ageRetryInMiddle() {
            List<Animal> pets = callReadPets("1\ndog\nRex\nnope\n2\n");
            assertEquals(1, pets.size());
            assertEquals(2, pets.get(0).getAge());
            assertTrue(outText().contains("Could not parse a number"));
        }
    }

    // ==========================================================
    //  7. printListPets
    // ==========================================================

    @Nested
    @DisplayName("printListPets")
    class PrintListPetsTests {

        @Test
        @DisplayName("Пустой список — пустой вывод")
        void empty() {
            invokePrintListPets(List.of());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Только травоядные — сначала herbivore-секция, omni пуст")
        void onlyHerbivores() {
            invokePrintListPets(List.of(new Hamster("Homa", 1), new GuineaPig("Piggy", 2)));
            assertEquals(
                    "Hamster name = Homa, age = 1. I can chill for 8 hours\n" +
                            "GuineaPig name = Piggy, age = 2. I can chill for 12 hours\n",
                    outText());
        }

        @Test
        @DisplayName("Только хищники — herbivore-секция пуста, потом omni")
        void onlyOmnivores() {
            invokePrintListPets(List.of(new Dog("Rex", 2), new Cat("Murka", 1)));
            assertEquals(
                    "Dog name = Rex, age = 2. I can hunt for robbers\n" +
                            "Cat name = Murka, age = 1. I can hunt for mice\n",
                    outText());
        }

        @Test
        @DisplayName("Смешанный список — Herbivore-блок ВСЕГДА перед Omnivore, независимо от исходного порядка")
        void mixedReordered() {
            // во входном списке Dog, Hamster, Cat, GuineaPig
            invokePrintListPets(List.of(
                    new Dog("Rex", 2),
                    new Hamster("Homa", 1),
                    new Cat("Murka", 3),
                    new GuineaPig("Piggy", 4)));
            // Herbivore (Hamster, GuineaPig) — сначала в исходном порядке,
            // потом Omnivore (Dog, Cat) — тоже в исходном порядке
            assertEquals(
                    "Hamster name = Homa, age = 1. I can chill for 8 hours\n" +
                            "GuineaPig name = Piggy, age = 4. I can chill for 12 hours\n" +
                            "Dog name = Rex, age = 2. I can hunt for robbers\n" +
                            "Cat name = Murka, age = 3. I can hunt for mice\n",
                    outText());
        }

        @Test
        @DisplayName("Порядок внутри группы сохраняется (стабильность stream)")
        void orderWithinGroups() {
            invokePrintListPets(List.of(
                    new Cat("C1", 1),
                    new Cat("C2", 2),
                    new Cat("C3", 3)));
            assertEquals(
                    "Cat name = C1, age = 1. I can hunt for mice\n" +
                            "Cat name = C2, age = 2. I can hunt for mice\n" +
                            "Cat name = C3, age = 3. I can hunt for mice\n",
                    outText());
        }

        @Test
        @DisplayName("Неизменяемый список не ломает метод")
        void immutableList() {
            invokePrintListPets(List.of(new Dog("Rex", 1)));
            assertEquals("Dog name = Rex, age = 1. I can hunt for robbers\n", outText());
        }
    }
}
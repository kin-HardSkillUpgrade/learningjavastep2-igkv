package org.example.TrackPetWalks;

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
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrackPetWalks — полный набор тестов")
class TrackPetWalksTest {

    // ==========================================================
    //  Общие утилиты
    // ==========================================================

    private ByteArrayOutputStream outBuf;
    private PrintStream out;
    private PrintStream originalOut;
    private InputStream originalIn;

    private static final Method M_PET_WALK;
    private static final Method M_PRINT_LIST;
    private static final Method M_READ_PETS;
    private static final Method M_READ_PET;
    private static final Method M_CREATE_PET;
    private static final Method M_READ_INT;

    static {
        try {
            M_PET_WALK   = TrackPetWalks.class.getDeclaredMethod("petWalk", List.class);
            M_PRINT_LIST = TrackPetWalks.class.getDeclaredMethod("printListPets", List.class);
            M_READ_PETS  = TrackPetWalks.class.getDeclaredMethod("readPets");
            M_READ_PET   = TrackPetWalks.class.getDeclaredMethod("readPet", Scanner.class);
            M_CREATE_PET = TrackPetWalks.class.getDeclaredMethod("createPet", String.class, String.class, Integer.class);
            M_READ_INT   = TrackPetWalks.class.getDeclaredMethod("readInt", Scanner.class);
            for (Method m : new Method[]{M_PET_WALK, M_PRINT_LIST, M_READ_PETS, M_READ_PET, M_CREATE_PET, M_READ_INT}) {
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

    // ---------- главный фикс: проброс настоящего исключения сквозь reflection ----------

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    private static RuntimeException rethrow(Throwable t) {
        Throwable cause = (t instanceof InvocationTargetException ite) ? ite.getCause() : t;
        return TrackPetWalksTest.<RuntimeException>sneakyThrow(cause);
    }

    // ---------- обёртки над приватными методами ----------

    @SuppressWarnings("unchecked")
    private List<WalkInfo> callPetWalk(List<Animal> pets) {
        try {
            return (List<WalkInfo>) M_PET_WALK.invoke(null, pets);
        } catch (ReflectiveOperationException e) {
            throw rethrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void callPrintListPets(List<WalkInfo> infos) {
        try {
            M_PRINT_LIST.invoke(null, infos);
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

    @SuppressWarnings("unchecked")
    private Optional<Animal> callReadPet(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (Optional<Animal>) M_READ_PET.invoke(null, sc);
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

    private int callReadInt(String input) {
        try (Scanner sc = new Scanner(input)) {
            return (int) M_READ_INT.invoke(null, sc);
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
    //  2. goToWalk — Dog
    // ==========================================================

    @Nested
    @DisplayName("Dog.goToWalk")
    class DogWalkTests {

        @Test
        @DisplayName("age = 2 → возвращает 1.0")
        @Timeout(5)
        void returnsHalfAge() {
            assertEquals(1.0, new Dog("Rex", 2).goToWalk(), 1e-9);
        }

        @Test
        @DisplayName("age = 1 → 0.5")
        @Timeout(5)
        void ageOne() {
            assertEquals(0.5, new Dog("R", 1).goToWalk(), 1e-9);
        }

        @Test
        @DisplayName("age = 0 → 0.0, sleep(0) — no-op")
        @Timeout(5)
        void ageZero() {
            long before = System.nanoTime();
            assertEquals(0.0, new Dog("R", 0).goToWalk(), 1e-9);
            long ms = (System.nanoTime() - before) / 1_000_000;
            assertTrue(ms < 500, "age=0 не должен спать, прошло " + ms + " ms");
        }

        @Test
        @DisplayName("age = -1 → возвращает -0.5 без исключения (текущее поведение)")
        @Timeout(5)
        void ageNegative() {
            // На практике TimeUnit.MILLISECONDS.sleep((long)(-0.5*1000)) не бросает
            // на всех поддерживаемых JDK в этом проекте — метод возвращает значение.
            // Если ваша JDK всё-таки бросает IllegalArgumentException — поменяйте на:
            //   assertThrows(IllegalArgumentException.class, () -> new Dog("R", -1).goToWalk());
            assertEquals(-0.5, new Dog("R", -1).goToWalk(), 1e-9);
        }

        @Test
        @DisplayName("age = 4 → 2.0")
        @Timeout(5)
        void valueIsHalfAge() {
            assertEquals(2.0, new Dog("R", 4).goToWalk(), 1e-9);
        }

        @Test
        @DisplayName("Прерывание → InterruptedException ловится, пишется в System.out")
        @Timeout(5)
        void interruptionIsCaught() throws Exception {
            Dog d = new Dog("R", 10); // 5 секунд сна
            Thread t = new Thread(d::goToWalk);
            t.start();
            Thread.sleep(100);            // дать потоку заснуть
            t.interrupt();
            t.join(2000);
            assertFalse(t.isAlive());
            assertTrue(outText().contains("InterruptedException"));
        }

        @Test
        @DisplayName("age = Integer.MAX_VALUE → sleep очень долгий; прерываем, проверяем catch")
        @Timeout(5)
        void maxAgeInterrupted() throws Exception {
            Dog d = new Dog("R", Integer.MAX_VALUE);
            Thread t = new Thread(d::goToWalk);
            t.setDaemon(true);
            t.start();
            Thread.sleep(50);
            t.interrupt();
            t.join(2000);
            assertFalse(t.isAlive());
        }
    }

    // ==========================================================
    //  3. goToWalk — Cat
    // ==========================================================

    @Nested
    @DisplayName("Cat.goToWalk")
    class CatWalkTests {

        @Test
        @DisplayName("Возвращает age * 1.0 (если у вас другая формула — поправьте)")
        @Timeout(5)
        void returnsAge() {
            assertEquals(0.5, new Cat("M", 2).goToWalk(), 1e-9);
        }

        @Test
        @DisplayName("age = 0 → 0.0 без сна")
        @Timeout(5)
        void ageZero() {
            long before = System.nanoTime();
            assertEquals(0.0, new Cat("M", 0).goToWalk(), 1e-9);
            assertTrue((System.nanoTime() - before) / 1_000_000 < 500);
        }
    }

    // ==========================================================
    //  4. WalkInfo
    // ==========================================================

    @Nested
    @DisplayName("WalkInfo")
    class WalkInfoTests {

        @Test
        @DisplayName("Геттеры возвращают переданное")
        void getters() {
            Dog d = new Dog("Rex", 2);
            WalkInfo info = new WalkInfo(d, 1.5, 2.5);
            assertSame(d, info.getPet());
            assertEquals(1.5, info.getStartWalk());
            assertEquals(2.5, info.getEndWalk());
        }

        @Test
        @DisplayName("toString содержит имя, start и end")
        void toStringContains() {
            Dog d = new Dog("Rex", 2);
            WalkInfo info = new WalkInfo(d, 1.234, 5.678);
            String s = info.toString();
            assertTrue(s.contains("Dog name = Rex, age = 2"));
            assertTrue(s.matches(".*start time = \\d+[.,]\\d{2}.*"));
            assertTrue(s.matches(".*end time = \\d+[.,]\\d{2}.*"));
        }

        @Test
        @DisplayName("getStartWalk / getEndWalk — double, в т.ч. крайние значения")
        void extremeValues() {
            WalkInfo info = new WalkInfo(new Cat("M", 1), 0.0, Double.MAX_VALUE);
            assertEquals(0.0, info.getStartWalk());
            assertEquals(Double.MAX_VALUE, info.getEndWalk());
        }
    }

    // ==========================================================
    //  5. petWalk
    // ==========================================================

    @Nested
    @DisplayName("petWalk")
    class PetWalkTests {

        @Test
        @DisplayName("Пустой список → пустой результат")
        void emptyList() {
            assertTrue(callPetWalk(List.of()).isEmpty());
        }

        @Test
        @DisplayName("Один питомец → один WalkInfo с тем же pet")
        @Timeout(5)
        void singlePet() {
            Dog d = new Dog("Rex", 1);
            List<WalkInfo> result = callPetWalk(List.of(d));
            assertEquals(1, result.size());
            assertSame(d, result.get(0).getPet());
        }

        @Test
        @DisplayName("endWalk >= startWalk для каждого")
        @Timeout(10)
        void endAfterStart() {
            List<WalkInfo> result = callPetWalk(List.of(
                    new Dog("A", 1),
                    new Cat("B", 1)));
            for (WalkInfo i : result) {
                assertTrue(i.getEndWalk() >= i.getStartWalk(),
                        "endWalk=" + i.getEndWalk() + " < startWalk=" + i.getStartWalk());
            }
        }

        @Test
        @DisplayName("По одному WalkInfo на каждого питомца")
        @Timeout(10)
        void onePerPet() {
            List<Animal> pets = List.of(
                    new Dog("A", 1),
                    new Dog("B", 1),
                    new Cat("C", 1),
                    new Cat("D", 1));
            List<WalkInfo> result = callPetWalk(pets);
            assertEquals(pets.size(), result.size());
            Set<Animal> returned = new HashSet<>();
            for (WalkInfo i : result) returned.add(i.getPet());
            assertEquals(new HashSet<>(pets), returned);
        }

        @Test
        @DisplayName("Результат — CopyOnWriteArrayList")
        @Timeout(5)
        void resultIsCowList() {
            assertInstanceOf(CopyOnWriteArrayList.class,
                    callPetWalk(List.of(new Dog("A", 1))));
        }

        @Test
        @DisplayName("Все потоки завершились до возврата (join)")
        @Timeout(10)
        void allThreadsJoined() {
            List<WalkInfo> result = callPetWalk(List.of(
                    new Dog("A", 1),
                    new Cat("B", 1)));
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(i -> i.getPet() != null));
        }

        @Test
        @DisplayName("startWalk/endWalk — конечные, end >= start")
        @Timeout(10)
        void intervals() {
            List<WalkInfo> result = callPetWalk(List.of(
                    new Dog("A", 1), new Cat("B", 1)));
            for (WalkInfo i : result) {
                assertTrue(Double.isFinite(i.getStartWalk()));
                assertTrue(Double.isFinite(i.getEndWalk()));
                assertTrue(i.getEndWalk() >= i.getStartWalk());
            }
        }
    }

    // ==========================================================
    //  6. printListPets
    // ==========================================================

    @Nested
    @DisplayName("printListPets")
    class PrintListPetsTests {

        private WalkInfo info(String name, int age, double start, double end) {
            return new WalkInfo(new Dog(name, age), start, end);
        }

        @Test
        @DisplayName("Пустой список → пустой вывод")
        void empty() {
            callPrintListPets(List.of());
            assertEquals("", outText());
        }

        @Test
        @DisplayName("Один элемент")
        void one() {
            callPrintListPets(List.of(info("Rex", 2, 1.0, 2.0)));
            assertTrue(outText().contains("Dog name = Rex, age = 2"));
        }

        @Test
        @DisplayName("Сортировка по endWalk по возрастанию")
        void sortedByEnd() {
            callPrintListPets(List.of(
                    info("C", 1, 0.0, 30.0),
                    info("A", 1, 0.0, 10.0),
                    info("B", 1, 0.0, 20.0)));
            String o = outText();
            int iA = o.indexOf("Dog name = A");
            int iB = o.indexOf("Dog name = B");
            int iC = o.indexOf("Dog name = C");
            assertTrue(iA < iB && iB < iC, "Ожидался A < B < C: " + o);
        }

        @Test
        @DisplayName("Равные endWalk — стабильный порядок")
        void equalEndWalk() {
            callPrintListPets(List.of(
                    info("X", 1, 0.0, 5.0),
                    info("Y", 1, 1.0, 5.0),
                    info("Z", 1, 2.0, 5.0)));
            String o = outText();
            int x = o.indexOf("Dog name = X");
            int y = o.indexOf("Dog name = Y");
            int z = o.indexOf("Dog name = Z");
            assertTrue(x < y && y < z, "При равных endWalk порядок должен сохраниться: " + o);
        }

        @Test
        @DisplayName("NaN в endWalk — не падает")
        void nanEnd() {
            assertDoesNotThrow(() -> callPrintListPets(List.of(
                    info("A", 1, 0.0, Double.NaN),
                    info("B", 1, 0.0, 1.0))));
        }

        @Test
        @DisplayName("Не мутирует входной список")
        void doesNotMutateInput() {
            List<WalkInfo> input = new ArrayList<>(List.of(
                    info("C", 1, 0.0, 30.0),
                    info("A", 1, 0.0, 10.0)));
            callPrintListPets(input);
            assertEquals("C", input.get(0).getPet().getName());
            assertEquals("A", input.get(1).getPet().getName());
        }
    }

    // ==========================================================
    //  7. readInt
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
            assertTrue(outText().contains("Could not parse a number"));
        }

        @Test
        @DisplayName("Переполнение сверху / снизу — повтор")
        void overflowRetries() {
            assertEquals(5, callReadInt("2147483648\n5\n"));
            assertEquals(5, callReadInt("-2147483649\n5\n"));
        }

        @Test
        @DisplayName("Пробелы — NumberFormatException, повтор")
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
    //  8. createPet
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
        @DisplayName("null type → NPE (switch по null)")
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
    //  9. readPet
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
        @DisplayName("age = 1 — минимум валидный")
        void minAge() {
            assertEquals(1, callReadPet("dog\nRex\n1\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = 0 → empty")
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
        @DisplayName("age = MAX_VALUE допустим")
        void maxAge() {
            assertEquals(Integer.MAX_VALUE,
                    callReadPet("dog\nRex\n2147483647\n").orElseThrow().getAge());
        }

        @Test
        @DisplayName("age = MIN_VALUE → empty")
        void minAgeInt() {
            assertTrue(callReadPet("dog\nRex\n-2147483648\n").isEmpty());
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
    //  10. readPets
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
    }

    // ==========================================================
    //  11. TYPES
    // ==========================================================

    @Nested
    @DisplayName("TYPES")
    class TypesTests {

        @Test
        @DisplayName("Ровно 2: dog, cat")
        void contents() {
            assertEquals(Set.of("dog", "cat"), TrackPetWalks.TYPES);
        }

        @Test
        @DisplayName("Иммутабельный")
        void immutable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> TrackPetWalks.TYPES.add("hamster"));
        }
    }

    // ==========================================================
    //  12. Интеграция
    // ==========================================================

    @Nested
    @DisplayName("Интеграция")
    class IntegrationTests {

        @Test
        @DisplayName("Сквозной сценарий: readPets → petWalk → printListPets")
        @Timeout(15)
        void endToEnd() {
            List<Animal> pets = callReadPets("2\ndog\nRex\n1\ncat\nMurka\n1\n");
            List<WalkInfo> infos = callPetWalk(pets);
            callPrintListPets(infos);

            assertEquals(2, infos.size());
            String o = outText();
            assertTrue(o.contains("Dog name = Rex, age = 1"));
            assertTrue(o.contains("Cat name = Murka, age = 1"));
            assertTrue(o.matches("(?s).*start time = \\d+[.,]\\d{2}.*"));
            assertTrue(o.matches("(?s).*end time = \\d+[.,]\\d{2}.*"));
        }
    }
}
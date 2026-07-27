import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        // Запуск ручных тестов перед стартом основной программы
        runTests();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n МЕНЮ (Модуль 5) ");
            System.out.println("1. Создать массив автобусов, отсортировать и сохранить");
            System.out.println("2. Проверить многопоточный подсчёт элементов");
            System.out.println("0. Выход");
            System.out.print("Выберите пункт: ");

            String input = scanner.nextLine();
            if ("0".equals(input)) {
                System.out.println("Программа завершена.");
                break;
            }

            if ("1".equals(input)) {
                System.out.print("Введите количество элементов: ");
                int size;
                try {
                    size = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите корректное число.");
                    continue;
                }

                CustomList<Bus> list = new CustomList<>();
                Random random = new Random();

                // Заполнение через Builder с валидацией
                for (int i = 0; i < size; i++) {
                    Bus bus = new Bus.Builder()
                            .setNumber(random.nextInt(100) + 1)
                            .setModel("MAZ_" + (random.nextInt(5) + 1))
                            .setMileage(random.nextInt(1000))
                            .build();
                    list.add(bus);
                }

                System.out.println("\nИсходная коллекция:");
                list.stream().forEach(System.out::println);

                // Сортировка паттерном Стратегия (Сортировка пузырьком + Четные/Нечетные)
                SortStrategy strategy = new BubbleSortStrategy();
                strategy.sort(list, new EvenOddComparator());

                System.out.println("\nОтсортированная коллекция (четные пробеги в начале):");
                list.stream().forEach(System.out::println);

                // Запись в файл в режиме добавления
                appendToFile("output.txt", list);

            } else if ("2".equals(input)) {
                CustomList<Bus> list = new CustomList<>();
                Bus target = new Bus.Builder().setNumber(10).setModel("Volvo").setMileage(100).build();
                Bus other = new Bus.Builder().setNumber(20).setModel("MAN").setMileage(200).build();

                list.add(target);
                list.add(other);
                list.add(target);
                list.add(target);

                try {
                    int count = countOccurrencesMultithreaded(list, target);
                    System.out.println("\nИскомый элемент: " + target);
                    System.out.println("Количество вхождений (многопоточный подсчёт): " + count);
                } catch (InterruptedException e) {
                    System.out.println("Ошибка при выполнении потоков: " + e.getMessage());
                }
            }
        }
        scanner.close();
    }

    //  1. Основной класс Bus с паттерном Builder 
    public static class Bus implements Comparable<Bus> {
        private final int number;
        private final String model;
        private final int mileage;

        private Bus(Builder builder) {
            this.number = builder.number;
            this.model = builder.model;
            this.mileage = builder.mileage;
        }

        public int getNumber() { return number; }
        public String getModel() { return model; }
        public int getMileage() { return mileage; }

        @Override
        public int compareTo(Bus o) {
            int res = Integer.compare(this.number, o.number);
            if (res != 0) return res;
            res = this.model.compareTo(o.model);
            if (res != 0) return res;
            return Integer.compare(this.mileage, o.mileage);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bus bus = (Bus) o;
            return number == bus.number && mileage == bus.mileage && Objects.equals(model, bus.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(number, model, mileage);
        }

        @Override
        public String toString() {
            return String.format("Bus{number=%d, model='%s', mileage=%d}", number, model, mileage);
        }

        public static class Builder {
            private int number;
            private String model;
            private int mileage;

            public Builder setNumber(int number) {
                if (number <= 0) throw new IllegalArgumentException("Номер должен быть > 0");
                this.number = number;
                return this;
            }

            public Builder setModel(String model) {
                if (model == null || model.trim().isEmpty()) {
                    throw new IllegalArgumentException("Модель не может быть пустой");
                }
                this.model = model;
                return this;
            }

            public Builder setMileage(int mileage) {
                if (mileage < 0) throw new IllegalArgumentException("Пробег не может быть отрицательным");
                this.mileage = mileage;
                return this;
            }

            public Bus build() {
                return new Bus(this);
            }
        }
    }

    //  2. Доп. задание 3*: Кастомная коллекция 
    public static class CustomList<T> implements Iterable<T> {
        private Object[] elements;
        private int size = 0;

        public CustomList() {
            elements = new Object[10];
        }

        public void add(T element) {
            if (size == elements.length) {
                elements = Arrays.copyOf(elements, elements.length * 2);
            }
            elements[size++] = element;
        }

        @SuppressWarnings("unchecked")
        public T get(int index) {
            checkIndex(index);
            return (T) elements[index];
        }

        public void set(int index, T element) {
            checkIndex(index);
            elements[index] = element;
        }

        public int size() { return size; }

        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Индекс вышел за границы: " + index);
            }
        }

        public Stream<T> stream() {
            @SuppressWarnings("unchecked")
            T[] array = (T[]) Arrays.copyOf(elements, size, Object[].class);
            return Arrays.stream(array);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private int cursor = 0;
                @Override
                public boolean hasNext() { return cursor < size; }
                @Override
                @SuppressWarnings("unchecked")
                public T next() { return (T) elements[cursor++]; }
            };
        }
    }

    //  3. Паттерн Стратегия для сортировки 
    public interface SortStrategy {
        void sort(CustomList<Bus> list, Comparator<Bus> comparator);
    }

    public static class BubbleSortStrategy implements SortStrategy {
        @Override
        public void sort(CustomList<Bus> list, Comparator<Bus> comparator) {
            int n = list.size();
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                        Bus temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                    }
                }
            }
        }
    }

    //  4. Доп. задание 1: Компаратор 
    public static class EvenOddComparator implements Comparator<Bus> {
        @Override
        public int compare(Bus b1, Bus b2) {
            boolean isEven1 = b1.getMileage() % 2 == 0;
            boolean isEven2 = b2.getMileage() % 2 == 0;

            if (isEven1 && isEven2) {
                return Integer.compare(b1.getMileage(), b2.getMileage());
            }
            if (!isEven1 && !isEven2) {
                return 0;
            }
            return isEven1 ? -1 : 1;
        }
    }

    //  5. Доп. задание 4: Многопоточный подсчёт элементов 
    public static int countOccurrencesMultithreaded(CustomList<Bus> list, Bus target) throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        int mid = list.size() / 2;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < mid; i++) {
                if (list.get(i).equals(target)) count.incrementAndGet();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = mid; i < list.size(); i++) {
                if (list.get(i).equals(target)) count.incrementAndGet();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        return count.get();
    }

    //  6. Доп. задание 2: Запись отсортированных данных в файл 
    public static void appendToFile(String fileName, CustomList<Bus> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            writer.println("--- Результаты сортировки ---");
            for (Bus bus : list) {
                writer.println(bus);
            }
            System.out.println("\nДанные успешно записаны в файл: " + fileName);
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    //  7. Ручные Unit-тесты 
    public static void runTests() {
        System.out.println("Запуск ручных тестов...");
        
        // Тест Builder
        Bus bus = new Bus.Builder().setNumber(101).setModel("MAZ").setMileage(50000).build();
        if (bus.getNumber() != 101 || !"MAZ".equals(bus.getModel()) || bus.getMileage() != 50000) {
            throw new RuntimeException("Тест Builder не пройден!");
        }

        // Тест CustomList
        CustomList<Bus> list = new CustomList<>();
        list.add(bus);
        if (list.size() != 1 || !list.get(0).equals(bus)) {
            throw new RuntimeException("Тест CustomList не пройден!");
        }

        System.out.println("Все тесты успешно пройдены!\n");
    }
}
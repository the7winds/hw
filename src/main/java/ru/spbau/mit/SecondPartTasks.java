package ru.spbau.mit;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream()
                .flatMap(s -> {
                    try {
                        if (s != null) {
                            Path p = Paths.get(s);
                            return Files.lines(p);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Stream.empty();
                }).filter(s -> s.contains(sequence))
                .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        class Point {
            public double x;
            public double y;
            public Point(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

        int limit = 100000;
        double r = 1;

        return (double) new Random().ints()
                .limit(limit)
                .mapToObj(x -> {
                    Random random = new Random(x);
                    return new Point((random.nextBoolean() ? -1 : 1) * random.nextDouble(),
                            (random.nextBoolean() ? -1 : 1) * random.nextDouble());
                }).reduce(0,
                        (Integer cnt, Point point) -> Math.pow(point.x, 2) + Math.pow(point.y, 2) <= r * r ? ++cnt : cnt,
                        (s1, s2) -> s1 + s2
                ) / limit;
    }


    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(),
                        e.getValue().stream()
                                .mapToInt(String::length)
                                .sum()))
                .max(Comparator.comparingInt(Pair::getValue))
                .orElse(new Pair<>(null, null))
                .getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));
    }
}

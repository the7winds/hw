package ru.spbau.mit;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes(){
        List<String> filesPrefixes = new LinkedList<>();
        filesPrefixes.add("test1");
        filesPrefixes.add("test2");
        filesPrefixes.add("test3");

        List<List<String>> texts = new LinkedList();
        List<String> list = new LinkedList<>();
        list.add("abacaba");
        list.add("wwww");
        list.add("asdwe1 www");
        texts.add(list);

        list = new LinkedList<>();
        list.add("weqweasd");
        list.add("wwww");
        list.add("asdwe2 www");
        list.add("weqweasd");
        texts.add(list);

        list = new LinkedList<>();
        list.add("");
        texts.add(list);

        List<String> paths = new LinkedList<>();
        for (String prefix : filesPrefixes) {
            try {
                File file = File.createTempFile(prefix, null);
                file.deleteOnExit();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : texts.get(0)) {
                        writer.write(line);
                        writer.newLine();
                    }
                    texts.remove(0);
                }

                paths.add(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> answer = new LinkedList<>();
        answer.add("asdwe1 www");
        answer.add("weqweasd");
        answer.add("asdwe2 www");
        answer.add("weqweasd");

        try {
            assertEquals(answer, SecondPartTasks.findQuotes(paths, "asd"));
            assertEquals(Collections.emptyList(), SecondPartTasks.findQuotes(Collections.emptyList(), "asd"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPiDividedBy4() {
        double eps = 0.1;
        assertTrue(Math.abs(Math.PI / 4 - SecondPartTasks.piDividedBy4()) < eps);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> authors = new HashMap<>();
        List<String> texts = new LinkedList<>();
        texts.add("a");
        texts.add("aaa");
        authors.put("A", texts);

        texts.clear();
        texts.add("b");
        authors.put("B", texts);

        assertEquals("A", SecondPartTasks.findPrinter(authors));
        assertNull(SecondPartTasks.findPrinter(Collections.emptyMap()));
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> orders = new LinkedList<>();
        Map<String, Integer> order = new HashMap<>();

        order.put("A", 4);
        order.put("B", 1);
        order.put("C", 4);
        orders.add(order);

        order = new HashMap<>();
        order.put("A", 4);
        order.put("B", 1);
        orders.add(order);

        Map<String, Integer> sumOrder = new HashMap<>();
        sumOrder.put("A", 8);
        sumOrder.put("B", 2);
        sumOrder.put("C", 4);

        assertEquals(sumOrder, SecondPartTasks.calculateGlobalOrder(orders));
        assertEquals(Collections.emptyMap(), SecondPartTasks.calculateGlobalOrder(Collections.emptyList()));
    }
}
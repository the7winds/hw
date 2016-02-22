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
    public void testFindQuotes() throws IOException {
        List<String> filesPrefixes = Arrays.asList("test1", "test2", "test3");

        List<List<String>> texts = new LinkedList<>();
        texts.add(Arrays.asList("abacaba", "wwww", "asdwe1 www"));
        texts.add(Arrays.asList("weqweasd", "wwww", "asdwe2 www", "weqweasd"));
        texts.add(new LinkedList<>());

        List<String> paths = new LinkedList<>();
        for (String prefix : filesPrefixes) {
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
        }

        List<String> answer = Arrays.asList("asdwe1 www", "weqweasd", "asdwe2 www", "weqweasd");

        assertEquals(answer, SecondPartTasks.findQuotes(paths, "asd"));
    }

    @Test
    public void testFindQuotesNoFiles() throws IOException {
        assertEquals(Collections.emptyList(), SecondPartTasks.findQuotes(Collections.emptyList(), "asd"));
    }

    @Test
    public void testFindQuotesNotReadableFile() throws IOException {
        final String testname = "test";
        File test = File.createTempFile(testname, null);
        test.setReadable(false);
        test.deleteOnExit();

        assertEquals(Collections.emptyList(), SecondPartTasks.findQuotes(Arrays.asList(testname), "asd"));
    }

    @Test
    public void testPiDividedBy4() {
        double eps = 0.01;
        assertTrue(Math.abs(Math.PI / 4 - SecondPartTasks.piDividedBy4()) < eps);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> authors = new HashMap<>();

        List<String> texts = Arrays.asList("a", "aaa");
        authors.put("A", texts);

        texts = Arrays.asList("b", "c");
        authors.put("B", texts);

        assertEquals("A", SecondPartTasks.findPrinter(authors));
    }

    @Test
    public void testFindPrinterEmptyArg() {
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
    }

    @Test
    public void testCalculateGlobalOrderEmptyArg() {
        assertEquals(Collections.emptyMap(), SecondPartTasks.calculateGlobalOrder(Collections.emptyList()));
    }
}
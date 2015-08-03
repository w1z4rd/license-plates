package org.costa;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class LicensePlateStream {

  private static final String OUTPUT_FILE = "target/license_plates_stream.txt";
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final Path path = Paths.get(OUTPUT_FILE);
  private static BufferedWriter writer;

  public static void generate(Generator<String> generator) {
    long startTime = System.currentTimeMillis();
    try {
      writer = Files.newBufferedWriter(path, ENCODING);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    List<String> letters = getLetters(generator);
    letters.parallelStream().filter(s -> filterLetters(s)).map(s -> createPlates(s)).flatMap(list -> list.stream())
        .forEach(LicensePlateStream::write);
    long endTime = System.currentTimeMillis();
    System.out.println("Streams - " + (endTime - startTime));
  }

  private static synchronized void write(String str) {
    try {
      writer.write(str);
      writer.newLine();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private static ConcurrentLinkedQueue<String> createPlates(String letters) {
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    Arrays.asList(LicensePlateApp.regions).parallelStream().forEach(region -> regionPlates(region, letters, queue));
    return queue;
  }

  private static void regionPlates(String region, String letters, ConcurrentLinkedQueue<String> queue) {
    queue.addAll(IntStream.range(1, getUpperBound(region)).parallel()
        .mapToObj(i -> String.format("%s-%02d-%s", new Object[] { region, i, letters })).collect(Collectors.toList()));
  }

  private static int getUpperBound(String region) {
    int n = 100;
    if (region.equals("B")) {
      n = 1000;
    }
    return n;
  }

  private static boolean filterLetters(String letters) {
    if (letters.startsWith("I"))
      return false;
    if (letters.startsWith("O"))
      return false;
    for (String curseWord : LicensePlateApp.curseWords) {
      if (letters.equals(curseWord)) {
        return false;
      }
    }
    return true;
  }

  private static List<String> getLetters(Generator<String> generator) {
    List<String> letters = new ArrayList<String>();
    for (ICombinatoricsVector<String> permutation : generator) {
      letters.add(getChars(permutation));
    }
    return letters;
  }

  private static String getChars(ICombinatoricsVector<String> perm) {
    List<String> charsVector = perm.getVector();
    StringBuilder sb = new StringBuilder(charsVector.size());
    for (String ch : charsVector) {
      sb.append(ch);
    }
    return sb.toString();
  }
}

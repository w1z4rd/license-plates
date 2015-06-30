package org.costa;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class LicensePlateStream {

  private static final String OUTPUT_FILE = "target/license_plates_stream.txt";
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final Path path = Paths.get(OUTPUT_FILE);
  private static BufferedWriter writer;

  private static String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
      "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
  private static final String[] curseWords = { "MUE", "CUR", "SEX", "PZD", "PLM" };
  private static String[] regions = { "AB", "AR", "AG", "B", "BC", "BH", "BN", "BT", "BV", "BR", "BZ", "CS", "CL",
      "CJ", "CT", "CV", "DB", "DJ", "GL", "GR", "GJ", "HR", "HD", "IL", "IS", "IF", "MM", "MH", "MS", "NT", "OT", "PH",
      "SM", "SJ", "SB", "SV", "TR", "TM", "TL", "VS", "VL", "VN" };

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    try {
      writer = Files.newBufferedWriter(path, ENCODING);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    List<String> letters = getLetters();
    letters.parallelStream().filter(s -> filterLetters(s)).map(s -> createPlates(s)).flatMap(list -> list.stream())
        .forEach(LicensePlateStream::write);
    long endTime = System.currentTimeMillis();
    System.out.println(endTime - startTime);
  }

  private static void write(String str) {
    try {
      writer.write(str);
      writer.newLine();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

  }

  private static List<String> createPlates(String letters) {
    List<String> list = new ArrayList<String>();
    for (String region : regions) {
      for (int i = 1; i < getUpperBound(region); i++) {
        list.add(String.format("%s-%02d-%s", new Object[] { region, Integer.valueOf(i), letters }));
      }
    }
    return list;
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
    for (String curseWord : curseWords) {
      if (letters.equals(curseWord)) {
        return false;
      }
    }
    return true;
  }

  private static List<String> getLetters() {
    List<String> letters = new ArrayList<String>();
    ICombinatoricsVector<String> originalVector = Factory.createVector(alphabet);
    Generator<String> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, 3);
    for (ICombinatoricsVector<String> perm : gen) {
      letters.add(getChars(perm));
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

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

public class LicensePlateGenerator {

  private static final String OUTPUT_FILE = "target/license_plates_serial.txt";
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final Path path = Paths.get(OUTPUT_FILE);

  public static void generate(Generator<String> generator) {

    long startTime = System.currentTimeMillis();
    List<String> lettersList = filterChars(generator);
    try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
      for (String region : LicensePlateApp.regions) {
        int upperBound = getUpperBound(region);
        for (int i = 1; i < upperBound; i++) {
          for (String letters : lettersList) {
            writer.write(String.format("%s-%02d-%s", new Object[] { region, Integer.valueOf(i), letters }));
            writer.newLine();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Serial - " + (endTime - startTime));
  }

  private static int getUpperBound(String region) {
    int n = 100;
    if (region.equals("B")) {
      n = 1000;
    }
    return n;
  }

  private static List<String> filterChars(Generator<String> gen) {
    List<String> lettersList = new ArrayList<String>();
    for (ICombinatoricsVector<String> perm : gen) {
      String letters = getChars(perm);
      if (!letters.startsWith("I")) {
        if (!letters.startsWith("O")) {
          boolean curse = false;
          for (String curseWord : LicensePlateApp.curseWords) {
            if (letters.equals(curseWord)) {
              curse = true;
            }
          }
          if (!curse) {
            lettersList.add(letters);
          }
        }
      }
    }
    return lettersList;
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

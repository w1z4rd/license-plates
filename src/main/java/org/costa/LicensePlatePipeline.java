package org.costa;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

//#TODO: fix threads not stoping after completion!!!
public class LicensePlatePipeline {

  private static final String[] REGIONS_A = { "AB", "AR", "AG", "BC", "BH", "BN", "BT", "BV", "BR", "BZ" };
  private static final String[] REGIONS_B = { "CS", "CL", "CJ", "CT", "CV", "DB", "DJ", "GL", "GR", "GJ" };
  private static final String[] REGIONS_C = { "HR", "HD", "IL", "IS", "IF", "MM", "MH", "MS", "NT", "OT" };
  private static final String[] REGIONS_D = { "PH", "SM", "SJ", "SB", "SV", "TR", "TM", "TL", "VS", "VL", "VN" };
  private static final String BUCHAREST = "B";
  private static BlockingQueue<String> lettersQueue = new LinkedBlockingQueue<>(30000);
  private static BlockingQueue<String> filtredLettersQueue = new LinkedBlockingQueue<>();
  private static BlockingQueue<String> licensePlates = new LinkedBlockingQueue<>(82000000);

  private static final String OUTPUT_FILE = "target/license_plates_pipeline.txt";
  private static final Charset ENCODING = StandardCharsets.UTF_8;
  private static final Path path = Paths.get(OUTPUT_FILE);

  private static CyclicBarrier barrier;
  private static volatile boolean done = false;
  private static long startTime;

  public static void generate(Generator<String> generator) {
    startTime = System.currentTimeMillis();
    barrier = new CyclicBarrier(8, new BarrierAction(filtredLettersQueue));
    Producer p = new Producer(lettersQueue, generator);
    Filter filter1 = new Filter(lettersQueue, filtredLettersQueue);
    Filter filter2 = new Filter(lettersQueue, filtredLettersQueue);
    Filter filter3 = new Filter(lettersQueue, filtredLettersQueue);
    LicensePlate l1 = new LicensePlate(filtredLettersQueue, barrier, 1, 100, REGIONS_A);
    LicensePlate l2 = new LicensePlate(filtredLettersQueue, barrier, 1, 100, REGIONS_B);
    LicensePlate l3 = new LicensePlate(filtredLettersQueue, barrier, 1, 100, REGIONS_C);
    LicensePlate l4 = new LicensePlate(filtredLettersQueue, barrier, 1, 100, REGIONS_D);
    LicensePlate l5 = new LicensePlate(filtredLettersQueue, barrier, 1, 250, BUCHAREST);
    LicensePlate l6 = new LicensePlate(filtredLettersQueue, barrier, 250, 500, BUCHAREST);
    LicensePlate l7 = new LicensePlate(filtredLettersQueue, barrier, 500, 750, BUCHAREST);
    LicensePlate l8 = new LicensePlate(filtredLettersQueue, barrier, 750, 1000, BUCHAREST);
    Observer o = new Observer(licensePlates);

    Thread producer = new Thread(p, "producer");
    producer.start();
    Thread filterOneThread = new Thread(filter1, "filterOne");
    filterOneThread.start();
    Thread filterTwoThread = new Thread(filter2, "filterTwo");
    filterTwoThread.start();
    Thread filterThreeThread = new Thread(filter3, "filterThree");
    filterThreeThread.start();
    Thread licenseThreadOne = new Thread(l1, "licenseOne");
    licenseThreadOne.start();
    Thread licenseThreadTwo = new Thread(l2, "licenseTwo");
    licenseThreadTwo.start();
    Thread licenseThreadThree = new Thread(l3, "licenseThree");
    licenseThreadThree.start();
    Thread licenseThreadFour = new Thread(l4, "licenseFour");
    licenseThreadFour.start();
    Thread licenseThreadFive = new Thread(l5, "licenseFive");
    licenseThreadFive.start();
    Thread licenseThreadSix = new Thread(l6, "licenseSix");
    licenseThreadSix.start();
    Thread licenseThreadSeven = new Thread(l7, "licenseSeven");
    licenseThreadSeven.start();
    Thread licenseThreadEight = new Thread(l8, "licenseEight");
    licenseThreadEight.start();
    Thread observer = new Thread(o, "observer");
    observer.start();
  }

  public static class Observer implements Runnable {
    BlockingQueue<String> licensePlates;
    BufferedWriter writer;

    public Observer(BlockingQueue<String> licensePlates) {
      this.licensePlates = licensePlates;
      try {
        writer = Files.newBufferedWriter(path, ENCODING);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      while (!done) {
        try {
          writer.write(licensePlates.take());
          writer.newLine();
        } catch (InterruptedException | IOException ie) {
          ie.printStackTrace();
        }
      }
    }
  }

  public static class Producer implements Runnable {
    BlockingQueue<String> queue;
    Generator<String> generator;

    public Producer(BlockingQueue<String> queue, Generator<String> generator) {
      this.queue = queue;
      this.generator = generator;
    }

    @Override
    public void run() {
      for (ICombinatoricsVector<String> permutation : generator) {
        String letters = getChars(permutation);
        try {
          queue.put(letters);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private String getChars(ICombinatoricsVector<String> perm) {
      List<String> charsVector = perm.getVector();
      StringBuilder sb = new StringBuilder(charsVector.size());
      for (String ch : charsVector) {
        sb.append(ch);
      }
      return sb.toString();
    }

  }

  public static class LicensePlate implements Runnable {
    String[] regions;
    int lowerBound;
    int upperBound;
    BlockingQueue<String> queue;
    CyclicBarrier barrier;

    @Override
    public void run() {
      while (!done) {
        try {
          if (queue.isEmpty()) {
            continue;
          }
          String letters = queue.peek();
          for (String region : regions) {
            for (int i = lowerBound; i < upperBound; i++) {
              String licensePlate = (String.format("%s-%02d-%s", new Object[] { region, Integer.valueOf(i), letters }));
              licensePlates.put(licensePlate);
            }
          }
          barrier.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    }

    public LicensePlate(BlockingQueue<String> queue, CyclicBarrier barrier, int lowerBound, int upperBound,
        String... regions) {
      this.queue = queue;
      this.regions = regions;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.barrier = barrier;
    }

  }

  public static class Filter implements Runnable {
    BlockingQueue<String> letters;
    BlockingQueue<String> filtered;

    public Filter(BlockingQueue<String> letters, BlockingQueue<String> filtered) {
      this.letters = letters;
      this.filtered = filtered;
    }

    @Override
    public void run() {
      while (!done) {
        try {
          if (letters.isEmpty()) {
            continue;
          }
          String license = letters.take();
          if (license != null && !license.isEmpty() && check(license)) {
            filtered.put(license);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private boolean check(String letters) {
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
  }

  public static class BarrierAction implements Runnable {
    BlockingQueue<String> queue;

    public BarrierAction(BlockingQueue<String> queue) {
      this.queue = queue;
    }

    @Override
    public void run() {
      try {
        queue.take();
        if (queue.isEmpty()) {
          done = true;
          System.out.println("Pipeline - " + (System.currentTimeMillis() - startTime));
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
//TODO: add cyclicbarrier to fix regions not getting all the plates
public class LicensePlatePipeline {
	// TODO: create a pipeline with the following stages:
	// - generate letters (can't break this down - due to use of 3rd party lib)
	// - split the list of generated letters into the number of cores / 2
	// - pass the sublists to instances of the filter
	// - filter letters for curse words
	// - create threads to spit out the whole number split by number regions
	// (1-250 251-500 501-750 751-1000 or 1-100)
	// - split the regions as well we have 41 of them (B) = 10 others so we have
	// B / 10 / 10 / 10 / 10
	private static String[] alphabet = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z" };
	private static final String[] REGIONS_A = { "AB", "AR", "AG", "BC", "BH",
			"BN", "BT", "BV", "BR", "BZ" };
	private static final String[] REGIONS_B = { "CS", "CL", "CJ", "CT", "CV",
			"DB", "DJ", "GL", "GR", "GJ" };
	private static final String[] REGIONS_C = { "HR", "HD", "IL", "IS", "IF",
			"MM", "MH", "MS", "NT", "OT" };
	private static final String[] REGIONS_D = { "PH", "SM", "SJ", "SB", "SV",
			"TR", "TM", "TL", "VS", "VL", "VN" };
	private static final String BUCHAREST = "B";
	private static final String[] curseWords = { "MUE", "CUR", "SEX", "PZD",
			"PLM" };
	private static volatile BlockingQueue<String> lettersQueue = new LinkedBlockingQueue<>(
			30000);
	private static volatile BlockingQueue<String> filtredLettersQueue = new LinkedBlockingQueue<>();
	private static volatile BlockingQueue<String> licensePlates = new LinkedBlockingQueue<>(
			82000000);

	private static final String OUTPUT_FILE = "D:\\Coding\\license_plates1.txt";
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	private final static Path path = Paths.get(OUTPUT_FILE);

	public static void main(String[] args) {
		LicensePlatePipeline pipeline = new LicensePlatePipeline();
		Producer p = pipeline.new Producer(lettersQueue);
		Filter filter1 = pipeline.new Filter(lettersQueue, filtredLettersQueue);
		Filter filter2 = pipeline.new Filter(lettersQueue, filtredLettersQueue);
		Filter filter3 = pipeline.new Filter(lettersQueue, filtredLettersQueue);
		LicensePlate l1 = pipeline.new LicensePlate(filtredLettersQueue, 1,
				100, REGIONS_A);
		LicensePlate l2 = pipeline.new LicensePlate(filtredLettersQueue, 1,
				100, REGIONS_B);
		LicensePlate l3 = pipeline.new LicensePlate(filtredLettersQueue, 1,
				100, REGIONS_C);
		LicensePlate l4 = pipeline.new LicensePlate(filtredLettersQueue, 1,
				100, REGIONS_D);
		LicensePlate l5 = pipeline.new LicensePlate(filtredLettersQueue, 1,
				250, BUCHAREST);
		LicensePlate l6 = pipeline.new LicensePlate(filtredLettersQueue, 251,
				500, BUCHAREST);
		LicensePlate l7 = pipeline.new LicensePlate(filtredLettersQueue, 501,
				750, BUCHAREST);
		LicensePlate l8 = pipeline.new LicensePlate(filtredLettersQueue, 751,
				999, BUCHAREST);
		Observer o = pipeline.new Observer(licensePlates);

		List<Job> jobs = new ArrayList<Job>();
		jobs.add(p);
		jobs.add(filter3);
		jobs.add(filter2);
		jobs.add(filter1);
		jobs.add(l1);
		jobs.add(l2);
		jobs.add(l3);
		jobs.add(l4);
		jobs.add(l5);
		jobs.add(l6);
		jobs.add(l7);
		jobs.add(l8);
		jobs.add(o);

		for (Job j : jobs) {
			Thread t = new Thread(j);
			t.start();
		}
	}

	public abstract class Job implements Runnable {
		protected boolean running;

		public void setRunning(boolean running) {
			this.running = running;
		}

	}

	public class Observer extends Job {
		BlockingQueue<String> licensePlates;
		BufferedWriter writer;

		public Observer(BlockingQueue<String> licensePlates) {
			this.licensePlates = licensePlates;
			setRunning(true);
			try {
				writer = Files.newBufferedWriter(path, ENCODING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (running) {
				try {
					writer.write(licensePlates.take());
				} catch (InterruptedException | IOException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	public class Producer extends Job {
		BlockingQueue<String> queue;

		public Producer(BlockingQueue<String> queue) {
			this.queue = queue;
			setRunning(true);
		}

		@Override
		public void run() {
			ICombinatoricsVector<String> originalVector = Factory
					.createVector(alphabet);
			Generator<String> gen = Factory
					.createPermutationWithRepetitionGenerator(originalVector, 3);
			for (ICombinatoricsVector<String> perm : gen) {
				String letters = getChars(perm);
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

	public class LicensePlate extends Job {
		String[] regions;
		int lowerBound;
		int upperBound;
		BlockingQueue<String> queue;

		@Override
		public void run() {
			while (running) {
				try {
					if (queue.isEmpty()) {
						continue;
					}
					String letters = queue.take();
					for (String region : regions) {
						for (int i = lowerBound; i < upperBound; i++) {
							String licensePlate = (String.format("%s-%02d-%s",
									new Object[] { region, Integer.valueOf(i),
											letters }));
							licensePlates.put(licensePlate);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public LicensePlate(BlockingQueue<String> queue, int lowerBound,
				int upperBound, String... regions) {
			this.queue = queue;
			this.regions = regions;
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			setRunning(true);
		}

	}

	public class Filter extends Job {
		BlockingQueue<String> letters;
		BlockingQueue<String> filtered;

		public void stop(boolean running) {
			this.running = running;
		}

		public Filter(BlockingQueue<String> letters,
				BlockingQueue<String> filtered) {
			this.letters = letters;
			this.filtered = filtered;
			setRunning(true);
		}

		@Override
		public void run() {
			while (running) {
				try {
					if (letters.isEmpty()) {
						continue;
					}
					String license = letters.poll();
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
			for (String curseWord : curseWords) {
				if (letters.equals(curseWord)) {
					return false;
				}
			}
			return true;
		}
	}
}

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

	private static final String OUTPUT_FILE = "D:\\Coding\\license_plates.txt";
	private final static Charset ENCODING = StandardCharsets.UTF_8;

	private static String[] alphabet = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z" };
	private static String[] regions = { "AB", "AR", "AG", "B", "BC", "BH",
			"BN", "BT", "BV", "BR", "BZ", "CS", "CL", "CJ", "CT", "CV", "DB",
			"DJ", "GL", "GR", "GJ", "HR", "HD", "IL", "IS", "IF", "MM", "MH",
			"MS", "NT", "OT", "PH", "SM", "SJ", "SB", "SV", "TR", "TM", "TL",
			"VS", "VL", "VN" };
	private static String[] curseWords = { "MUE", "CUR", "SEX", "PZD", "PLM" };

	public static void main(String[] args) {
		Path path = Paths.get(OUTPUT_FILE);
		long startTime = System.currentTimeMillis();
		ICombinatoricsVector<String> originalVector = Factory
				.createVector(alphabet);
		Generator<String> gen = Factory
				.createPermutationWithRepetitionGenerator(originalVector, 3);
		List<String> lettersList = filterChars(gen);
		try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
			for (String region : regions) {
				int upperBound = getUpperBound(region);
				for (int i = 1; i < upperBound; i++) {
					for (String letters : lettersList) {

						writer.write(String.format("%s-%02d-%s", new Object[] {
								region, Integer.valueOf(i), letters }));
						// licensePlates.add(String.format("%s-%02d-%s", new
						// Object[] {
						// region, Integer.valueOf(i), letters }));
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
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
					for (String curseWord : curseWords) {
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

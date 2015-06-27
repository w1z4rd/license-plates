package org.costa;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LicesePlatePipeline {
    //TODO: create a pipeline with the following stages:
    //    - generate letters  (can't break this down - due to use of 3rd party lib)
    //    - split the list of generated letters into the number of cores / 2
    //    - pass the sublists to instances of the filter
    //    - filter letters for curse words
    //    - create threads to spit out the whole number split by number regions
    //    (1-250 251-500 501-750 751-1000 or 1-100)
    //    - split the regions as well we have 41 of them (B) = 10 others so we have 
    //    B / 10 / 10 / 10 / 10
    private static String[] alphabet = {"A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};
    private static final String[] REGIONS_A = {"AB", "AR", "AG", "BC", "BH",
            "BN", "BT", "BV", "BR", "BZ"};
    private static final String[] REGIONS_B = {"CS", "CL", "CJ", "CT", "CV",
            "DB", "DJ", "GL", "GR", "GJ"};
    private static final String[] REGIONS_C = {"HR", "HD", "IL", "IS", "IF",
            "MM", "MH", "MS", "NT", "OT"};
    private static final String[] REGIONS_D = {"PH", "SM", "SJ", "SB", "SV",
            "TR", "TM", "TL", "VS", "VL", "VN"};
    private static final String BUCHAREST = "B";
    private static final String[] curseWords = {"MUE", "CUR", "SEX", "PZD", "PLM"};
    private static volatile BlockingQueue<String> lettersQueue = new LinkedBlockingQueue<>();
    private static volatile BlockingQueue<String> filtredLettersQueue = new LinkedBlockingQueue<>();
    private static volatile BlockingQueue<String> licensePlateQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

    }

    private static int getUpperBound(String region) {
        int n = 100;
        if (region.equals("B")) {
            n = 1000;
        }
        return n;
    }

	private static String getChars(ICombinatoricsVector<String> perm) {
		List<String> charsVector = perm.getVector();
		StringBuilder sb = new StringBuilder(charsVector.size());
		for (String ch : charsVector) {
			sb.append(ch);
		}
		return sb.toString();
	}

    class LicensePlate implements Runnable {
        @Override
        public void run() {
        }

    }

    class Filter implements Runnable {
        BlockingQueue<String> letters;
        BlockingQueue<String> filtered;
        boolean running = true;

        public void stop(boolean running) {
            this.running = running;
        }

        public Filter(BlockingQueue<String> letters, BlockingQueue<String> filterted) {
            this.letters = letters;
            this.filtered = filtered;
        }

        @Override
        public void run() {
            while (running){
                String license = letters.poll();
                if(check(license)) {
                   filtered.put(license);
                }
            }
        }

        private boolean check(String letters) {
		  if (letters.startsWith("I")) return false;
          if (letters.startsWith("O")) return false;
		  for (String curseWord : curseWords) {
		    if (letters.equals(curseWord)) {
		  	return false;
		    }
          }
          return true;
        }
    }
}

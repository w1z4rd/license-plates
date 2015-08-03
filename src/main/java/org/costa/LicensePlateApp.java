/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.costa;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 * @author costelradulescu
 *
 */
public class LicensePlateApp {

  public static String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
      "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
  public static String[] regions = { "AB", "AR", "AG", "B", "BC", "BH", "BN", "BT", "BV", "BR", "BZ", "CS", "CL", "CJ",
      "CT", "CV", "DB", "DJ", "GL", "GR", "GJ", "HR", "HD", "IL", "IS", "IF", "MM", "MH", "MS", "NT", "OT", "PH", "SM",
      "SJ", "SB", "SV", "TR", "TM", "TL", "VS", "VL", "VN" };
  public static String[] curseWords = { "MUE", "CUR", "SEX", "PZD", "PLM" };

  public static void main(String[] args) {
    final ICombinatoricsVector<String> originalVector = Factory.createVector(LicensePlateApp.alphabet);
    final Generator<String> generator = Factory.createPermutationWithRepetitionGenerator(originalVector, 3);
    Runnable serial = () -> {
      LicensePlateGenerator.generate(generator);
    };
    Runnable pipeline = () -> {
      LicensePlatePipeline.generate(generator);
    };
    Runnable streams = () -> {
      LicensePlateStream.generate(generator);
    };
    new Thread(serial).start();
    new Thread(pipeline).start();
    new Thread(streams).start();
  }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rest4j.doc;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Main {
	public static void main(String [] args) throws Exception {
		if (args.length == 0) {
			help();
			return;
		}
		if (args[0].equals("doc")) {
			DocGenerator.main(removeFirst(args));
		} else {
			System.err.println("Wrong command: "+args[0]);
			help();
			System.exit(-1);
		}
	}

	private static String[] removeFirst(String[] args) {
		String[] result = new String[args.length-1];
		System.arraycopy(args, 1, result, 0, result.length);
		return result;
	}

	private static void help() {
		System.out.println("\nRuns a Rest4J command. Syntax: ");
		System.out.println("java -jar rest4j-core-1.0-deps.jar doc doc-generation-parameters");
		System.out.println("   Generate REST API documentation.");
		System.out.println("java -jar rest4j-core-1.0-deps.jar doc -h");
		System.out.println("   Print help on documentation generator.\n");
	}
}

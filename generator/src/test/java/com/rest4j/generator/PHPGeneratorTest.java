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

package com.rest4j.generator;

import org.apache.commons.exec.*;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class PHPGeneratorTest {
	Generator gen = new Generator();

	@Test
	public void testPHPClient() throws Exception {
		gen.setStylesheet("com/rest4j/client/php.xslt");
		new File("target/php").mkdir();
		gen.setApiXmlUrl(getClass().getResource("doc-generator-graph.xml"));
		gen.setOutputDir("target/php");
		gen.addParam(new TemplateParam("common-params", "access-token"));
		gen.addParam(new TemplateParam("prefix", "Test"));
		gen.addParam(new TemplateParam("additional-client-code", "// ADDITIONAL CODE"));
		gen.generate();

		// check the syntax
		CommandLine cmdLine = CommandLine.parse("php apiclient.php");
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(new File("target/php"));
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		int exitValue = executor.execute(cmdLine);
		assertFalse(executor.isFailure(exitValue));

		// check existence of Parameter Object class
		String client = IOUtils.toString(new File("target/php/apiclient.php").toURI());
		assertTrue(client, client.contains("class TestPatchBRequest"));

		assertTrue(client.contains("ADDITIONAL CODE"));
	}


}

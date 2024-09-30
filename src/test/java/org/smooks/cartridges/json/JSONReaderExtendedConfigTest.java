/*-
 * ========================LICENSE_START=================================
 * smooks-json-cartridge
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 *
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 *
 * ======================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ======================================================================
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.json;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.StreamSource;
import org.smooks.support.SmooksUtil;
import org.smooks.support.StreamUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.smooks.testkit.Assertions.compareCharStreams;

/**
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class JSONReaderExtendedConfigTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONReaderExtendedConfigTest.class);

    @Test
    public void test_simple_smooks_config() throws Exception {
        test_config_file("simple_smooks_config");

        // Programmatic config....
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new JSONReaderConfigurator());
        test_config_file("simple_smooks_config", smooks);
    }

    @Test
    public void test_key_replacement() throws Exception {
        test_config_file("key_replacement");

        // Programmatic config....
        Smooks smooks = new Smooks();

        Map<String, String> keyMap = new HashMap<String, String>();

        keyMap.put("some key", "someKey");
        keyMap.put("some&key", "someAndKey");

        smooks.setReaderConfig(new JSONReaderConfigurator().setKeyMap(keyMap));
        test_config_file("key_replacement", smooks);
    }

    @Test
    public void test_several_replacements() throws Exception {
        test_config_file("several_replacements");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new JSONReaderConfigurator()
                .setKeyWhitspaceReplacement("_")
                .setKeyPrefixOnNumeric("n")
                .setIllegalElementNameCharReplacement(".")
                .setNullValueReplacement("##NULL##"));

        test_config_file("several_replacements", smooks);
    }

    @Test
    public void test_configured_different_node_names() throws Exception {
        test_config_file("configured_different_node_names");

        // Programmatic config....
        Smooks smooks = new Smooks();

        smooks.setReaderConfig(new JSONReaderConfigurator()
                .setRootName("root")
                .setArrayElementName("e"));
        test_config_file("configured_different_node_names", smooks);
    }

    @Test
    public void test_indent() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("/indent-config.xml"));
        StringSink sink = new StringSink();

        smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("/input-message.jsn")), sink);

        assertTrue(XMLUnit.compareXML(StreamUtils.readStreamAsString(getClass().getResourceAsStream("/indent-expected.xml"), "UTF-8"), sink.toString()).identical());
    }

    private void test_config_file(String testName) throws Exception {
        Smooks smooks = new Smooks("/test/" + testName + "/smooks-extended-config.xml");

        test_config_file(testName, smooks);
    }

    private void test_config_file(String testName, Smooks smooks) throws IOException {
        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testName + "/input-message.jsn"), smooks);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Result: {}", result);
        }

        assertEquals("/test/" + testName + "/expected.xml", result.getBytes());
    }

    private void assertEquals(String fileExpected, byte[] actual) throws IOException {

        byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream(fileExpected));

        assertTrue("Expected XML and result XML are not the same!", compareCharStreams(new ByteArrayInputStream(actual), new ByteArrayInputStream(expected)));
    }
}

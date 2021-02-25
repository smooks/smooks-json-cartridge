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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.profile.DefaultProfileSet;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.io.StreamUtils;
import org.smooks.support.SmooksUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class JSONReaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONReaderTest.class);

    @Test
    public void test_json_types() throws Exception {
        test_progammed_config("json_types");
    }

    @Test
    public void test_json_map() throws Exception {
        test_progammed_config("json_map");
    }

    @Test
    public void test_json_array() throws Exception {
        test_progammed_config("json_array");
    }

    @Test
    public void test_json_map_array() throws Exception {
        test_progammed_config("json_map_array");
    }

    @Test
    public void test_json_array_map() throws Exception {
        test_progammed_config("json_array_map");
    }

    @Test
    public void test_json_map_array_map() throws Exception {
        test_progammed_config("json_map_array_map");
    }

    @Test
    public void test_simple_smooks_config() throws Exception {
        test_config_file("simple_smooks_config");
    }

    @Test
    public void test_key_replacement() throws Exception {
        test_config_file("key_replacement");
    }

    @Test
    public void test_several_replacements() throws Exception {
        test_config_file("several_replacements");
    }

    @Test
    public void test_configured_different_node_names() throws Exception {
        test_config_file("configured_different_node_names");
    }

    private void test_progammed_config(String testNumber) throws Exception {
        Smooks smooks = new Smooks();
        ResourceConfig resourceConfig;

        resourceConfig = new DefaultResourceConfig("org.xml.sax.driver", "type:Order-List AND from:Acme", JSONReader.class.getName());
        smooks.getApplicationContext().getRegistry().registerResourceConfig(resourceConfig);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("Order-List-Acme-AcmePartner1", new String[]{"type:Order-List", "from:Acme", "to:AcmePartner1"}), smooks);

        ExecutionContext context = smooks.createExecutionContext("Order-List-Acme-AcmePartner1");
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testNumber + "/input-message.jsn"), smooks);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Result: " + result);
        }

        assertEquals("/test/" + testNumber + "/expected.xml", result.getBytes());
    }

    private void test_config_file(String testNumber) throws Exception {
        Smooks smooks = new Smooks("/test/" + testNumber + "/smooks-config.xml");

        ExecutionContext context = smooks.createExecutionContext();
        String result = SmooksUtil.filterAndSerialize(context, getClass().getResourceAsStream("/test/" + testNumber + "/input-message.jsn"), smooks);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Result: " + result);
        }

        assertEquals("/test/" + testNumber + "/expected.xml", result.getBytes());
    }

    private void assertEquals(String fileExpected, byte[] actual) throws IOException {

        byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream(fileExpected));

        assertTrue("Expected XML and result XML are not the same!", StreamUtils.compareCharStreams(new ByteArrayInputStream(actual), new ByteArrayInputStream(expected)));

    }
}

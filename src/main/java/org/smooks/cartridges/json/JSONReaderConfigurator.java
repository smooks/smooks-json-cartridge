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

import org.smooks.ReaderConfigurator;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.GenericReaderConfigurator;
import org.smooks.cdr.Parameter;
import org.smooks.assertion.AssertArgument;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * JSON Reader configurator.
 * <p/>
 * Supports programmatic {@link JSONReader} configuration on a {@link org.smooks.Smooks#setReaderConfig(org.smooks.ReaderConfigurator) Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JSONReaderConfigurator implements ReaderConfigurator {

    private String rootName = JSONReader.XML_ROOT;
    private String arrayElementName = JSONReader.XML_ARRAY_ELEMENT_NAME;
    private String keyWhitspaceReplacement;
    private String keyPrefixOnNumeric;
    private String illegalElementNameCharReplacement;
    private String nullValueReplacement = JSONReader.DEFAULT_NULL_VALUE_REPLACEMENT;
    private Charset encoding = Charset.forName("UTF-8");
    private Map<String, String> keyMap;
    private String targetProfile;

    public JSONReaderConfigurator setRootName(String rootName) {
        AssertArgument.isNotNull(rootName, "rootName");
        this.rootName = rootName;
        return this;
    }

    public JSONReaderConfigurator setArrayElementName(String arrayElementName) {
        AssertArgument.isNotNull(arrayElementName, "arrayElementName");
        this.arrayElementName = arrayElementName;
        return this;
    }

    public JSONReaderConfigurator setKeyWhitspaceReplacement(String keyWhitspaceReplacement) {
        AssertArgument.isNotNull(keyWhitspaceReplacement, "keyWhitspaceReplacement");
        this.keyWhitspaceReplacement = keyWhitspaceReplacement;
        return this;
    }

    public JSONReaderConfigurator setKeyPrefixOnNumeric(String keyPrefixOnNumeric) {
        AssertArgument.isNotNull(keyPrefixOnNumeric, "keyPrefixOnNumeric");
        this.keyPrefixOnNumeric = keyPrefixOnNumeric;
        return this;
    }

    public JSONReaderConfigurator setIllegalElementNameCharReplacement(String illegalElementNameCharReplacement) {
        AssertArgument.isNotNull(illegalElementNameCharReplacement, "illegalElementNameCharReplacement");
        this.illegalElementNameCharReplacement = illegalElementNameCharReplacement;
        return this;
    }

    public JSONReaderConfigurator setNullValueReplacement(String nullValueReplacement) {
        AssertArgument.isNotNull(nullValueReplacement, "nullValueReplacement");
        this.nullValueReplacement = nullValueReplacement;
        return this;
    }

    public JSONReaderConfigurator setEncoding(Charset encoding) {
        AssertArgument.isNotNull(encoding, "encoding");
        this.encoding = encoding;
        return this;
    }

    public JSONReaderConfigurator setKeyMap(Map<String, String> keyMap) {
        AssertArgument.isNotNull(keyMap, "keyMap");
        this.keyMap = keyMap;
        return this;
    }

    public JSONReaderConfigurator setTargetProfile(String targetProfile) {
        AssertArgument.isNotNullAndNotEmpty(targetProfile, "targetProfile");
        this.targetProfile = targetProfile;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(JSONReader.class);

        configurator.getParameters().setProperty("rootName", rootName);
        configurator.getParameters().setProperty("arrayElementName", arrayElementName);
        if(keyWhitspaceReplacement != null) {
            configurator.getParameters().setProperty("keyWhitspaceReplacement", keyWhitspaceReplacement);
        }
        if(keyPrefixOnNumeric != null) {
            configurator.getParameters().setProperty("keyPrefixOnNumeric", keyPrefixOnNumeric);
        }
        if(illegalElementNameCharReplacement != null) {
            configurator.getParameters().setProperty("illegalElementNameCharReplacement", illegalElementNameCharReplacement);
        }
        configurator.getParameters().setProperty("nullValueReplacement", nullValueReplacement);
        configurator.getParameters().setProperty("encoding", encoding.name());

        List<SmooksResourceConfiguration> configList = configurator.toConfig();
        SmooksResourceConfiguration config = configList.get(0);

        if(keyMap != null) {
            Parameter keyMapParam = new Parameter(JSONReader.CONFIG_PARAM_KEY_MAP, keyMap);
            config.setParameter(keyMapParam);
        }

        config.setTargetProfile(targetProfile);

        return configList;
    }
}

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

import org.smooks.api.SmooksConfigException;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;

public final class KeyMapDigester {

	private static final String KEY_MAP_KEY_ELEMENT = "key";

	private static final String KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE = "from";

	private static final String KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE = "to";

	private KeyMapDigester() {

	}

	public static HashMap<String, String> digest(Element keyMapElement) {
		HashMap<String, String> keyMap = new HashMap<String, String>();

		NodeList keys = keyMapElement.getElementsByTagNameNS("*", KEY_MAP_KEY_ELEMENT);

		for (int i = 0; i < keys.getLength(); i++) {
			Element keyElement = (Element) keys.item(i);

			String from = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE);

			if (from == null || from.trim().length() == 0) {
				throw new SmooksConfigException("The '" + KEY_MAP_KEY_ELEMENT_FROM_ATTRIBUTE + "' attribute isn't defined or is empty for the key element: " + keyElement);
			}
			from = from.trim();

			String value = DomUtils.getAttributeValue(keyElement, KEY_MAP_KEY_ELEMENT_TO_ATTRIBUTE);
			if (value == null) {
				value = DomUtils.getAllText(keyElement, true);
				if (value.trim().length() == 0) {
					value = null;
				}
			}
			keyMap.put(from, value);
		}

		return keyMap;
	}

}

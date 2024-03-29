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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.w3c.dom.Element;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * JSON to SAX event reader.
 * <p/>
 * This JSON Reader can be plugged into Smooks in order to convert a
 * JSON based message stream into a stream of SAX events to be consumed by the other
 * Smooks resources.
 *
 * <h3>Configuration</h3>
 * <pre>
 * &lt;resource-config selector="org.xml.sax.driver"&gt;
 *  &lt;resource&gt;org.smooks.json.JSONReader&lt;/resource&gt;
 *  &lt;!--
 *      (Optional) The element name of the SAX document root. Default of 'json'.
 *  --&gt;
 *  &lt;param name="<b>rootName</b>"&gt;<i>&lt;root-name&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The element name of a array element. Default of 'element'.
 *  --&gt;
 *  &lt;param name="<b>arrayElementName</b>"&gt;<i>&lt;array-element-name&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The replacement string for JSON NULL values. Default is an empty string.
 *  --&gt;
 *  &lt;param name="<b>nullValueReplacement</b>"&gt;<i>&lt;null-value-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The replacement character for whitespaces in a json map key. By default this not defined, so that the reader doesn't search for whitespaces.
 *  --&gt;
 *  &lt;param name="<b>keyWhitspaceReplacement</b>"&gt;<i>&lt;key-whitspace-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) The prefix character to add if the JSON node name starts with a number. By default this is not defined, so that the reader doesn't search for element names that start with a number.
 *  --&gt;
 *  &lt;param name="<b>keyPrefixOnNumeric</b>"&gt;<i>&lt;key-prefix-on-numeric&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) If illegal characters are encountered in a JSON element name then they are replaced with this value. By default this is not defined, so that the reader doesn't doesn't search for illegal characters.
 *  --&gt;
 *  &lt;param name="<b>illegalElementNameCharReplacement</b>"&gt;<i>&lt;illegal-element-name-char-replacement&gt;</i>&lt;/param&gt;
 *  &lt;!--
 *      (Optional) Defines a map of keys and there replacement. The from key will be replaced with the to key or the contents of the element.
 *  --&gt;
 *  &lt;param name="<b>keyMap</b>"&gt;
 *   &lt;key from="fromKey" to="toKey" /&gt;
 *   &lt;key from="fromKey"&gt;&lt;to&gt;&lt;/key&gt;
 *  &lt;/param&gt;
 *  &lt;!--
 *      (Optional) The encoding of the input stream. Default of 'UTF-8'
 *  --&gt;
 *  &lt;param name="<b>encoding</b>"&gt;<i>&lt;encoding&gt;</i>&lt;/param&gt;
 *
 * &lt;/resource-config&gt;
 * </pre>
 *
 * <h3>Example Usage</h3>
 * So the following configuration could be used to parse a JSON stream into
 * a stream of SAX events:
 * <pre>
 * &lt;resource-config selector="org.xml.sax.driver"&gt;
 *  &lt;resource&gt;org.smooks.json.JSONReader&lt;/resource&gt;
 * &lt;/smooks-resource&gt;</pre>
 * <p>
 * The "Acme-Order-List" input JSON message:
 * <pre>
 * [
 *  {
 *   "name" : "Maurice Zeijen",
 *   "address" : "Netherlands",
 *   "item" : "V1234",
 *   "quantity" : 3
 *  },
 *  {
 *   "name" : "Joe Bloggs",
 *   "address" : "England",
 *   "item" : "D9123",
 *   "quantity" : 7
 *  },
 * ]</pre>
 * <p>
 * Within Smooks, the stream of SAX events generated by the "Acme-Order-List" message (and this reader) will generate
 * a DOM equivalent to the following:
 * <pre>
 * &lt;json&gt;
 *  &lt;element&gt;
 *   &lt;name&gt;Maurice Zeijen&lt;/name&gt;
 *   &lt;address&gt;Netherlands&lt;/address&gt;
 *   &lt;item&gt;V1234&lt;/item&gt;
 *   &lt;quantity&gt;3&lt;/quantity&gt;
 *  &lt;element&gt;
 *  &lt;element&gt;
 *   &lt;name&gt;Joe Bloggs&lt;/name&gt;
 *   &lt;address&gt;England&lt;/address&gt;
 *   &lt;item&gt;D9123&lt;/item&gt;
 *   &lt;quantity&gt;7&lt;/quantity&gt;
 *  &lt;element&gt;
 * &lt;/json&gt;</pre>
 * <p/>
 *
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
@SuppressWarnings("unchecked")
public class JSONReader implements SmooksXMLReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONReader.class);

    public static final String CONFIG_PARAM_KEY_MAP = "keyMap";

    public static final String XML_ROOT = "json";

    public static final String XML_ARRAY_ELEMENT_NAME = "element";

    public static final String DEFAULT_NULL_VALUE_REPLACEMENT = "";

    private static final Attributes EMPTY_ATTRIBS = new AttributesImpl();

    private static final JsonFactory jsonFactory = new JsonFactory();

    private ContentHandler contentHandler;

    private ExecutionContext executionContext;


    @Inject
    private String rootName = XML_ROOT;

    @Inject
    private String arrayElementName = XML_ARRAY_ELEMENT_NAME;

    @Inject
    private Optional<String> keyWhitspaceReplacement;

    @Inject
    private Optional<String> keyPrefixOnNumeric;

    @Inject
    private Optional<String> illegalElementNameCharReplacement;

    @Inject
    private String nullValueReplacement = DEFAULT_NULL_VALUE_REPLACEMENT;

    @Inject
    private Charset encoding = StandardCharsets.UTF_8;

    @Inject
    private Boolean indent = false;

    @Inject
    private ResourceConfig resourceConfig;

    private boolean doKeyReplacement = false;

    private boolean doKeyWhitspaceReplacement = false;

    private boolean doPrefixOnNumericKey = false;

    private boolean doIllegalElementNameCharReplacement = false;

    private HashMap<String, String> keyMap = new HashMap<String, String>();

    private enum Type {
        OBJECT,
        ARRAY
    }

    @PostConstruct
    public void initialize() {
        initKeyMap();

        doKeyReplacement = !keyMap.isEmpty();
        doKeyWhitspaceReplacement = keyWhitspaceReplacement.isPresent();
        doPrefixOnNumericKey = keyPrefixOnNumeric.isPresent();
        doIllegalElementNameCharReplacement = illegalElementNameCharReplacement.isPresent();
    }


    /*
     * (non-Javadoc)
     * @see org.smooks.xml.SmooksXMLReader#setExecutionContext(org.smooks.container.ExecutionContext)
     */
    public void setExecutionContext(ExecutionContext request) {
        this.executionContext = request;
    }

    /*
     * (non-Javadoc)
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    public void parse(InputSource csvInputSource) throws IOException, SAXException {
        if (contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse JSON stream.");
        }
        if (executionContext == null) {
            throw new IllegalStateException("Smooks container 'executionContext' not set.  Cannot parse JSON stream.");
        }

        try {
            // Get a reader for the JSON source...
            Reader jsonStreamReader = csvInputSource.getCharacterStream();
            if (jsonStreamReader == null) {
                jsonStreamReader = new InputStreamReader(csvInputSource.getByteStream(), encoding);
            }

            // Create the JSON parser...
            JsonParser jp = null;
            try {

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Creating JSON parser");
                }

                jp = jsonFactory.createJsonParser(jsonStreamReader);

                // Start the document and add the root "csv-set" element...
                contentHandler.startDocument();
                startElement(rootName, 0);

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Starting JSON parsing");
                }

                boolean first = true;
                Stack<String> elementStack = new Stack<String>();
                Stack<Type> typeStack = new Stack<Type>();
                JsonToken t;
                while ((t = jp.nextToken()) != null) {

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Token: " + t.name());
                    }

                    switch (t) {

                        case START_OBJECT:
                        case START_ARRAY:
                            if (!first) {
                                if (!typeStack.empty() && typeStack.peek() == Type.ARRAY) {
                                    startElement(arrayElementName, typeStack.size());
                                }
                            }
                            typeStack.push(t == JsonToken.START_ARRAY ? Type.ARRAY : Type.OBJECT);
                            break;

                        case END_OBJECT:
                        case END_ARRAY:

                            typeStack.pop();

                            boolean typeStackPeekIsArray = !typeStack.empty() && typeStack.peek() == Type.ARRAY;

                            if (!elementStack.empty() && !typeStackPeekIsArray) {
                                endElement(elementStack.pop(), typeStack.size());
                            }


                            if (typeStackPeekIsArray) {
                                endElement(arrayElementName, typeStack.size());
                            }
                            break;

                        case FIELD_NAME:

                            String text = jp.getText();

                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Field name: " + text);
                            }

                            String name = getElementName(text);

                            startElement(name, typeStack.size());
                            elementStack.add(name);


                            break;

                        default:

                            String value;

                            if (t == JsonToken.VALUE_NULL) {
                                value = nullValueReplacement;
                            } else {
                                value = jp.getText();
                            }

                            if (typeStack.peek() == Type.ARRAY) {

                                startElement(arrayElementName, typeStack.size());
                            }

                            contentHandler.characters(value.toCharArray(), 0, value.length());

                            if (typeStack.peek() == Type.ARRAY) {

                                endElement(arrayElementName);

                            } else {

                                endElement(elementStack.pop());

                            }

                            break;


                    }

                    first = false;
                }
                endElement(rootName, 0);
                contentHandler.endDocument();
            } finally {

                try {
                    jp.close();
                } catch (Exception e) {
                }


            }
        } finally {
            // These properties need to be reset for every execution (e.g. when reader is pooled).
            contentHandler = null;
            executionContext = null;
        }
    }

    private static char[] INDENT = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".toCharArray();

    private void startElement(String name, int indent) throws SAXException {
        indent(indent);
        contentHandler.startElement(XMLConstants.NULL_NS_URI, name, "", EMPTY_ATTRIBS);
    }

    private void endElement(String name, int indent) throws SAXException {
        indent(indent);
        endElement(name);
    }

    private void endElement(String name) throws SAXException {
        contentHandler.endElement(XMLConstants.NULL_NS_URI, name, "");
    }

    private void indent(int indentAmount) throws SAXException {
        if (indent) {
            if (indentAmount > 0) {
                contentHandler.characters(INDENT, 0, indentAmount + 1);
            } else {
                contentHandler.characters(INDENT, 0, 1);
            }
        }
    }

    /**
     * @param text
     * @return
     */
    private String getElementName(String text) {

        boolean replacedKey = false;
        if (doKeyReplacement) {

            String mappedKey = keyMap.get(text);

            replacedKey = mappedKey != null;
            if (replacedKey) {
                text = mappedKey;
            }

        }

        if (!replacedKey) {
            if (doKeyWhitspaceReplacement) {
                text = text.replace(" ", keyWhitspaceReplacement.get());
            }

            if (doPrefixOnNumericKey && Character.isDigit(text.charAt(0))) {
                text = keyPrefixOnNumeric.get() + text;
            }

            if (doIllegalElementNameCharReplacement) {
                text = text.replaceAll("^[.]|[^a-zA-Z0-9_.-]", illegalElementNameCharReplacement.get());
            }
        }
        return text;
    }


    /**
     *
     */
    private void initKeyMap() {
        Parameter<?> keyMapParam = resourceConfig.getParameter(CONFIG_PARAM_KEY_MAP, Object.class);

        if (keyMapParam != null) {
            Object objValue = keyMapParam.getValue();

            if (objValue instanceof Map) {
                keyMap = (HashMap<String, String>) objValue;
            } else {
                Element keyMapParamElement = keyMapParam.getXml();

                if (keyMapParamElement != null) {

                    setKeyMap(KeyMapDigester.digest(keyMapParamElement));

                } else {
                    LOGGER.error("Sorry, the key properties must be available as XML DOM. Please configure using XML.");
                }
            }
        }
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }


    /**
     * @return the keyMap
     */
    public HashMap<String, String> getKeyMap() {
        return keyMap;
    }

    /**
     * @param keyMap the keyMap to set
     */
    public void setKeyMap(HashMap<String, String> keyMap) {
        this.keyMap = keyMap;
    }


    /**
     * @return the rootName
     */
    public String getRootName() {
        return rootName;
    }


    /**
     * @param rootName the rootName to set
     */
    public void setRootName(String rootName) {
        this.rootName = rootName;
    }


    /**
     * @return the arrayElementName
     */
    public String getArrayElementName() {
        return arrayElementName;
    }


    /**
     * @param arrayElementName the arrayElementName to set
     */
    public void setArrayElementName(String arrayElementName) {
        this.arrayElementName = arrayElementName;
    }


    /**
     * @return the keyWhitspaceReplacement
     */
    public String getKeyWhitspaceReplacement() {
        return keyWhitspaceReplacement.orElse(nullValueReplacement);
    }


    /**
     * @param keyWhitspaceReplacement the keyWhitspaceReplacement to set
     */
    public void setKeyWhitspaceReplacement(String keyWhitspaceReplacement) {
        this.keyWhitspaceReplacement = Optional.ofNullable(keyWhitspaceReplacement);
    }


    /**
     * @return the keyPrefixOnNumeric
     */
    public String getKeyPrefixOnNumeric() {
        return keyPrefixOnNumeric.orElse(null);
    }


    /**
     * @param keyPrefixOnNumeric the keyPrefixOnNumeric to set
     */
    public void setKeyPrefixOnNumeric(String keyPrefixOnNumeric) {
        this.keyPrefixOnNumeric = Optional.ofNullable(keyPrefixOnNumeric);
    }


    /**
     * @return the illegalElementNameCharReplacement
     */
    public String getIllegalElementNameCharReplacement() {
        return illegalElementNameCharReplacement.orElse(null);
    }


    /**
     * @param illegalElementNameCharReplacement the illegalElementNameCharReplacement to set
     */
    public void setIllegalElementNameCharReplacement(
            String illegalElementNameCharReplacement) {
        this.illegalElementNameCharReplacement = Optional.ofNullable(illegalElementNameCharReplacement);
    }


    /**
     * @return the nullValueReplacement
     */
    public String getNullValueReplacement() {
        return nullValueReplacement;
    }


    /**
     * @param nullValueReplacement the nullValueReplacement to set
     */
    public void setNullValueReplacement(String nullValueReplacement) {
        this.nullValueReplacement = nullValueReplacement;
    }


    /**
     * @return the encoding
     */
    public Charset getEncoding() {
        return encoding;
    }


    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    /****************************************************************************
     *
     * The following methods are currently unimplemnted...
     *
     ****************************************************************************/

    public void parse(String systemId) {
        throw new UnsupportedOperationException("Operation not supports by this reader.");
    }

    public boolean getFeature(String name) {
        return false;
    }

    public void setFeature(String name, boolean value) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setDTDHandler(DTDHandler arg0) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setEntityResolver(EntityResolver arg0) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler arg0) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public void setProperty(String name, Object value) {
    }
}

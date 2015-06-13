/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.xml;

import com.android.sdklib.SdkConstants;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * XPath factory with automatic support for the android name space.
 */
public class AndroidXPathFactory {
    /** Default prefix for android name space: 'android' */
    public final static String DEFAULT_NS_PREFIX = "android"; //$NON-NLS-1$

    private final static XPathFactory sFactory = XPathFactory.newInstance();

    /** Name space context for Android resource XML files. */
    private static class AndroidNamespaceContext implements NamespaceContext {
        private final static AndroidNamespaceContext sThis = new AndroidNamespaceContext(
                DEFAULT_NS_PREFIX);

        private String mAndroidPrefix;

        /**
         * Returns the default {@link AndroidNamespaceContext}.
         */
        private static AndroidNamespaceContext getDefault() {
            return sThis;
        }

        /**
         * Construct the context with the prefix associated with the android namespace.
         * @param androidPrefix the Prefix
         */
        public AndroidNamespaceContext(String androidPrefix) {
            mAndroidPrefix = androidPrefix;
        }

        public String getNamespaceURI(String prefix) {
            if (prefix != null) {
                if (prefix.equals(mAndroidPrefix)) {
                    return SdkConstants.NS_RESOURCES;
                }
            }

            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            // This isn't necessary for our use.
            assert false;
            return null;
        }

        public Iterator<?> getPrefixes(String namespaceURI) {
            // This isn't necessary for our use.
            assert false;
            return null;
        }
    }

    /**
     * Creates a new XPath object, specifying which prefix in the query is used for the
     * android namespace.
     * @param androidPrefix The namespace prefix.
     */
    public static XPath newXPath(String androidPrefix) {
        XPath xpath = sFactory.newXPath();
        xpath.setNamespaceContext(new AndroidNamespaceContext(androidPrefix));
        return xpath;
    }

    /**
     * Creates a new XPath object using the default prefix for the android namespace.
     * @see #DEFAULT_NS_PREFIX
     */
    public static XPath newXPath() {
        XPath xpath = sFactory.newXPath();
        xpath.setNamespaceContext(AndroidNamespaceContext.getDefault());
        return xpath;
    }
}

/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.netsuite.runtime.client;

/**
 * Responsible for sequential retrieving of result data objects.
 */
public interface ResultSet<T> {

    /**
     * Advance to next result.
     *
     * @return {@code} true if result is available, {@code false} otherwise
     * @throws NetSuiteException if an error occurs during retrieving of results
     */
    boolean next() throws NetSuiteException;

    /**
     * Get last read result.
     *
     * @return result object
     * @throws NetSuiteException if an error occurs during retrieving of results
     */
    T get() throws NetSuiteException;
}

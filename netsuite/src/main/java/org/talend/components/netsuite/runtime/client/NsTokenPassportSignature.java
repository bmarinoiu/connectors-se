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

import lombok.Data;

@Data
public class NsTokenPassportSignature {

    protected String value;

    protected Algorithm algorithm;

    public enum Algorithm {
        Hmac_SHA256("HmacSHA256"),
        Hmac_SHA1("HmacSHA1");

        private final String algorithm;

        private Algorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        String getAlgorithmString() {
            return this.algorithm;
        }
    }
}

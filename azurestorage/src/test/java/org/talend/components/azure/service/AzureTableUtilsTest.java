/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
package org.talend.components.azure.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.microsoft.azure.storage.OperationContext;

public class AzureTableUtilsTest {

    @Test
    public void testOpContextCreatedForFirstTime() {
        OperationContext context = AzureConnectionService.getTalendOperationContext();

        assertNotNull(context);
        assertFalse(context.getUserHeaders().isEmpty());
        assertNotNull(context.getUserHeaders().get(AzureConnectionService.USER_AGENT_KEY));
    }

    @Test
    public void testOpContextIsSingleTone() {
        OperationContext contextFirst = AzureConnectionService.getTalendOperationContext();

        assertEquals(contextFirst, AzureConnectionService.getTalendOperationContext());
    }
}
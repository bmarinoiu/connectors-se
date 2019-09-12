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
package org.talend.components.couchbase;

import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class TestData {

    private final String colId = "id";

    private final int colIntMin = Integer.MIN_VALUE;

    private final int colIntMax = Integer.MAX_VALUE;

    private final long colLongMin = Long.MIN_VALUE;

    private final long colLongMax = Long.MAX_VALUE;

    private final float colFloatMin = Float.MIN_VALUE;

    private final float colFloatMax = Float.MAX_VALUE;

    private final double colDoubleMin = Double.MIN_VALUE;

    private final double colDoubleMax = Double.MAX_VALUE;

    private final boolean colBoolean = Boolean.TRUE;

    private final ZonedDateTime colDateTime = ZonedDateTime.of(2018, 10, 30, 10, 30, 59, 0, ZoneId.of("UTC"));

    private final List<String> colList = new ArrayList<>(Arrays.asList("data1", "data2", "data3"));
}

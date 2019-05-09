// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.rest.source;

import org.talend.components.rest.configuration.RequestConfig;
import org.talend.components.rest.service.RestService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Version(1)
@Icon(value = Icon.IconType.CUSTOM, custom = "Http")
@Emitter(name = "Input")
@Documentation("")
public class RestEmitter implements Serializable {

    private final RequestConfig config;

    private final RestService client;

    private final Queue<Record> records = new LinkedList<>();

    public RestEmitter(@Option("configuration") final RequestConfig config, final RestService client) {
        this.config = config;
        this.client = client;
    }

    @PostConstruct
    public void init() {
        Record record = client.execute(config);
        records.add(record);
    }

    @Producer
    public Record next() {
        return records.poll();
    }

}
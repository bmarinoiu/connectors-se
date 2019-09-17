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
package org.talend.components.slack.service;

import lombok.extern.slf4j.Slf4j;
import org.talend.components.slack.connection.SlackConnection;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

@Slf4j
@Service
public class UIActionService extends SlackService {

    public static final String HEALTH_CHECK = "SLACK_HEALTH_CHECK";

    @HealthCheck(HEALTH_CHECK)
    public HealthCheckStatus doHealthCheck(@Option(SlackConnection.NAME) SlackConnection connection, final I18nMessage i18n) {
        initClients(connection);
        return new HealthCheckStatus(HealthCheckStatus.Status.OK, i18n.connectionSuccessful());
    }

}

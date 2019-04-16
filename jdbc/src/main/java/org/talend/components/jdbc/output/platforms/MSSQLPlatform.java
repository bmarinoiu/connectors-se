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
package org.talend.components.jdbc.output.platforms;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.jdbc.service.I18nMessage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * https://docs.microsoft.com/fr-fr/sql/t-sql/statements/create-table-transact-sql?view=sql-server-2017
 */
@Slf4j
public class MSSQLPlatform extends Platform {

    public static final String MSSQL = "mssql";

    public MSSQLPlatform(final I18nMessage i18n) {
        super(i18n);
    }

    @Override
    public String name() {
        return MSSQL;
    }

    @Override
    protected String delimiterToken() {
        return "\"";
    }

    @Override
    protected String buildQuery(final Table table) {
        // keep the string builder for readability
        final StringBuilder sql = new StringBuilder("CREATE TABLE");
        sql.append(" ");
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(identifier(table.getSchema())).append(".");
        }
        sql.append(identifier(table.getName()));
        sql.append("(");
        sql.append(createColumns(table.getColumns()));
        sql.append(createPKs(table.getName(),
                table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toList())));
        sql.append(")");
        // todo create index

        log.debug("### create table query ###");
        log.debug(sql.toString());
        return sql.toString();
    }

    @Override
    protected boolean isTableExistsCreationError(final Throwable e) {
        return e instanceof SQLException && "S0001".equalsIgnoreCase(((SQLException) e).getSQLState())
                && 2714 == ((SQLException) e).getErrorCode();
    }

    private String createColumns(final List<Column> columns) {
        return columns.stream().map(this::createColumn).collect(Collectors.joining(","));
    }

    private String createColumn(final Column column) {
        return identifier(column.getName())//
                + " " + toDBType(column)//
                + " " + isRequired(column)//
        ;
    }

    protected String isRequired(final Column column) {
        return column.isNullable() && !column.isPrimaryKey() ? "" : "NOT NULL";
    }

    private String toDBType(final Column column) {
        switch (column.getType()) {
        case STRING:
            // https://docs.microsoft.com/fr-fr/sql/relational-databases/tables/primary-and-foreign-key-constraints?view=sql-server-2017
            return column.getSize() <= -1 ? (column.isPrimaryKey() ? "VARCHAR(900)" : "VARCHAR(max)")
                    : "VARCHAR(" + column.getSize() + ")";
        case BOOLEAN:
            return "BIT";
        case DOUBLE:
        case FLOAT:
            return "DECIMAL";
        case LONG:
            return "BIGINT";
        case INT:
            return "INT";
        case BYTES:
            return "VARBINARY(max)";
        case DATETIME:
            return "datetime2";
        case RECORD:
        case ARRAY:
        default:
            throw new IllegalStateException(getI18n().errorUnsupportedType(column.getType().name(), column.getName()));
        }
    }

    @Override
    public void addDataSourceProperties(HikariDataSource dataSource) {
        super.addDataSourceProperties(dataSource);

        // https://docs.microsoft.com/en-us/sql/connect/jdbc/setting-the-connection-properties?view=sql-server-2017
        dataSource.addDataSourceProperty("applicationName", APPLICATION);
    }
}

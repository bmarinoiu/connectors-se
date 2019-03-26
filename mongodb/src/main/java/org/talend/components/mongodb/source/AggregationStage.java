package org.talend.components.mongodb.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@GridLayout({ @GridLayout.Row({ "stage" }) })
@Documentation("This is the mapping for input schema.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationStage implements Serializable {

    @Option
    @Documentation("")
    private String stage;

}

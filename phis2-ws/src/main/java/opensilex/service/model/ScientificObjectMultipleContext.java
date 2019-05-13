//******************************************************************************
//                                ScientificObjectMultipleContext.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 13 mai 2019
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Scientific object model with multiple concept.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectMultipleContext {
    //uri of the scientific object
    private String uri;
    //geometry of the scientific object
    private String geometry;
    //metadata of the scientific object by context
    private List<ScientificObjectByContext> metadataByContext = new ArrayList<>();

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public List<ScientificObjectByContext> getMetadataByContext() {
        return metadataByContext;
    }

    public void setMetadataByContext(List<ScientificObjectByContext> metadataByContext) {
        this.metadataByContext = metadataByContext;
    }
    
    public void addMetadataByContext(ScientificObjectByContext metadataByContextToAdd) {
        metadataByContext.add(metadataByContextToAdd);
    }
}

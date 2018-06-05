//**********************************************************************************************
//                                       ResultatDocumentProperty.java 
//
// Author(s): Arnaud CHARLEROY
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: October 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  December, 2017
// Subject: extends Result. Adapted to a document property list
//***********************************************************************************************
package phis2ws.service.view.brapi.results;

import java.util.ArrayList;
import phis2ws.service.view.brapi.Pagination;
import phis2ws.service.view.manager.Resultat;
import phis2ws.service.view.model.phis.DocumentProperty;

/**
 * Extends Result. Adapted to a document property list
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class ResultatDocumentProperty extends Resultat<DocumentProperty> {
    public ResultatDocumentProperty(ArrayList<DocumentProperty> list) {
        super(list);
    }

    public ResultatDocumentProperty(ArrayList<DocumentProperty> list, Pagination pagination, boolean paginate) {
        super(list, pagination, paginate);
    }
}

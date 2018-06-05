//**********************************************************************************************
//                                       ResponseFormDocumentMetadata.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: June 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  June, 2017
// Subject: extends ResultForm. Adapted to the document property view
//***********************************************************************************************
package phis2ws.service.view.brapi.form;

import java.util.ArrayList;
import phis2ws.service.view.brapi.Metadata;
import phis2ws.service.view.brapi.results.ResultatDocumentProperty;
import phis2ws.service.view.manager.ResultForm;
import phis2ws.service.view.model.phis.DocumentProperty;

public class ResponseFormDocumentProperty extends ResultForm<DocumentProperty> {
    /**
     * Initialize  metadata et result fields
     * @param pageSize number of results per page
     * @param currentPage current page
     * @param list result list
     * @param paginate 
     */
    public ResponseFormDocumentProperty(int pageSize, int currentPage, ArrayList<DocumentProperty> list, boolean paginate) {
        metadata = new Metadata(pageSize, currentPage, list.size());
        if (list.size() > 1) {
            result = new ResultatDocumentProperty(list, metadata.getPagination(), paginate); 
        } else {
            result = new ResultatDocumentProperty(list);
        }
    }
}

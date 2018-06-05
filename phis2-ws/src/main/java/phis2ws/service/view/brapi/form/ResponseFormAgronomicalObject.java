//**********************************************************************************************
//                               ResponseFormAgronomicalObject.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: august 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  January, 2017
// Subject: Allows the formating of the result of the queries about 
//                                                          agronomical objects
//***********************************************************************************************

package phis2ws.service.view.brapi.form;

import java.util.ArrayList;
import phis2ws.service.view.brapi.Metadata;
import phis2ws.service.view.brapi.results.ResultatAgronomicalObject;
import phis2ws.service.view.manager.ResultForm;
import phis2ws.service.view.model.phis.AgronomicalObject;

public class ResponseFormAgronomicalObject  extends ResultForm<AgronomicalObject>{
    
    /**
     * Initialise les champs metadata et result
     * @param pageSize nombre de résultats par page
     * @param currentPage page demandée
     * @param list liste des résultats
     * @param paginate 
     */
    public ResponseFormAgronomicalObject(int pageSize, int currentPage, ArrayList<AgronomicalObject> list, boolean paginate) {
        metadata = new Metadata(pageSize, currentPage, list.size());
        if (list.size() > 1) {
            result = new ResultatAgronomicalObject(list, metadata.getPagination(), paginate);
        } else {
            result = new ResultatAgronomicalObject(list);
        }
    }
}

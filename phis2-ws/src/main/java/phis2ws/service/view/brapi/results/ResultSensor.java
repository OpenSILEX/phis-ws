//******************************************************************************
//                                       ResultSensor.java
//
// Author(s): Morgane Vidal <morgane.vidal@inra.fr>
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: 14 mars 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  14 mars 2018
// Subject: extend from Resultat, adapted to the sensors object list
//******************************************************************************
package phis2ws.service.view.brapi.results;

import java.util.ArrayList;
import phis2ws.service.view.brapi.Pagination;
import phis2ws.service.view.manager.Resultat;
import phis2ws.service.view.model.phis.Sensor;

/**
 * A class which represents the result part in the response form, adapted to the sensors
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ResultSensor extends Resultat<Sensor> {
    /**
     * @param sensors the sensors of the result 
     */
    public ResultSensor(ArrayList<Sensor> sensors) {
        super(sensors);
    }
    
    /**
     * @param sensors
     * @param pagination
     * @param paginate 
     */
    public ResultSensor(ArrayList<Sensor> sensors, Pagination pagination, boolean paginate) {
        super(sensors, pagination, paginate);
    }
}

//**********************************************************************************************
//                                       UnitDTO.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: November, 17 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  November, 17 2017
// Subject: A class which contains methods to automatically check the attributes 
//          of a class, from rules defined by user.
//          Contains the list of the elements which might be send by the client 
//          to save the database
//***********************************************************************************************
package phis2ws.service.resources.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.view.model.phis.Unit;

public class UnitDTO extends InstanceDefinitionDTO {
    final static Logger LOGGER = LoggerFactory.getLogger(UnitDTO.class);
    
    public Unit createObjectFromDTO() {
        Unit trait = (Unit) super.createObjectFromDTO();
        return trait; 
    }
}

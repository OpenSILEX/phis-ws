//******************************************************************************
//                                       DocumentStatus.java
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: 6 Aug, 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.configuration;

//SILEX:todo
// Use this enum instead of the Documentation in the documentation swagger examples and in the Documents DAO
//\SILEX:todo

/**
 * The list of the authorized documents status
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>, Morgane Vidal <morgane.vidal@inra.fr>
 */
public enum DocumentStatus {
    LINKED {
        @Override
        public String toString(){
            return "linked";
        }
    },
    UNLINKED {
        @Override
        public String toString(){
            return "unlinked";
        }
    }
}

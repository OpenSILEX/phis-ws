//******************************************************************************
//                                       GrantTypes.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 6 Aug, 2018
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.configuration;

//SILEX:todo
// Use this enum instead of the Documentation in the documentation swagger examples
//\SILEX:todo

/**
 * The list of the authorized grant types formats
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>, Morgane Vidal <morgane.vidal@inra.fr>
 */
public enum GrantTypes {
    
    PASSWORD {
        @Override
        public String toString(){
            return "password";
        }
    },
    JWT {
        @Override
        public String toString(){
            return "jwt";
        }
    }
}

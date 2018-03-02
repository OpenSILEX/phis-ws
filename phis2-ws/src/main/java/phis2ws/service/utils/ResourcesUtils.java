//**********************************************************************************************
//                                       ResourcesUtils.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: may 2016
// Contact:arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  February 05,  2018 - all the dates methods has been
// moved to a date package (phis2ws.service.utils.dates)
// Subject: A class which regroup all function which are not specific or can be usable in all the webservice
//***********************************************************************************************
package phis2ws.service.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * List of functions which can be used in ressources
 * @author Morgane Vidal
 * @author Arnaud Charleroy
 */
public class ResourcesUtils {
    
    /**
     * generates a unique string of 32 caracters
     * @author Arnaud Charleroy
     * @see java.util.UUID randomUUID()
     * @return the UUID
     */
    public static String getUniqueID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
     /**
     * return the boolean value of the given string
     * @param bool String
     * @author Arnaud Charleroy
     * @return true if the string is equals to "true" or "t" (it is not case 
     *         sensitive)
     *         false if the string is not equals to the precedent strings
     *         
     */
    public static boolean getStringBooleanValue(String bool) {
        return (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("t"));
    }

    /**
     * Capitalize the first Letter of the string
     * @author Arnaud Charleroy
     * @param original the string for which we wants the first letter to be 
     * capitalized
     * @return the given string with the first lettre capitalized
     */
    public static String capitalizeFirstLetter(String original) {
        return (original == null || original.length() == 0) ? original : original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }

    /**
     * split a string using the given separator
     * @author Arnaud Charleroy
     * @param values
     * @param separator
     * @return the list of elements separated by the given separator in the string
     *         values
     */
    public static List<String> splitStringWithGivenPattern(String values, String separator) {
        List<String> listValues;
        try {
            listValues = Arrays.asList(values.split(separator));
        } catch (Exception e) {
            listValues = new ArrayList<>();
            listValues.add(values);
        }
        return listValues;
    }

    /**
     * extentY to plantHeight
     * width to plantWidth
     * @author Arnaud Charleroy
     * @param mongoUserVariable
     * @return
     */
    public static String formatMongoImageAnalysisVariableForDB(String mongoUserVariable) {
        String tmpMongoUserVariable = null;
        String variableFormatMongo = "";
        // elcom special case
        if (mongoUserVariable.contains("parallelBoudingBox")) {
            tmpMongoUserVariable = mongoUserVariable.replaceFirst("parallelBoudingBox", "");
            variableFormatMongo = "parallelBoudingBox_" + tmpMongoUserVariable.toLowerCase();
        } else if (mongoUserVariable.contains("nonParallelBoudingBox")) {
            tmpMongoUserVariable = mongoUserVariable.replaceFirst("nonParallelBoudingBox", "");
            variableFormatMongo = "nonParallelBoudingBox" + tmpMongoUserVariable.toLowerCase();
        } else {
            for (int i = 0; i < mongoUserVariable.length(); i++) {
                if (Character.isUpperCase(mongoUserVariable.charAt(i))) {
                    variableFormatMongo += "_";
                    variableFormatMongo += Character.toLowerCase(mongoUserVariable.charAt(i));
                } else {
                    variableFormatMongo += mongoUserVariable.charAt(i);
                }
            }
        }
        return variableFormatMongo;
    }

    /**
     * @author Arnaud Charleroy
     * @param uri
     * @return 
     */
    public static String getValueOfAnURI(String uri) {
        final String[] parts = uri.split("#");
        if ((uri.contains(">") && uri.contains("<"))) {
            return parts[1].substring(0, parts[1].length() - 1);
        } else {
            return parts[1];
        }
    }
    
    /**
     * Transforme une chaîne de caractère en DateTime avec un format entrée en
     * paramètre (librairie Joda)
     *
     * @param stringDate
     * @param pattern
     * @return
     */
    public static DateTime convertStringToDateTime(String stringDate, String pattern) {
        final org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
        try {
            return formatter.withOffsetParsed().parseDateTime(stringDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}

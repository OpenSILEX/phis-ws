//******************************************************************************
//                            URINamespaces.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 6 Aug, 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import phis2ws.service.PropertiesFileManager;

/**
 * Personal parameters for global usage.
 * G_URI :
 *      - M3P URI for further use so that we don"t need to repeat paths in each page
 * Usage :
 * ~~~~~~ URINamespaces uris = new URINamespaces("m3p");
 * ~~~~~~ String vocabulary = URINamespaces.getContextsProperty("pVocaPlateform"
 * @author Arnaud Charleroy <arnaud.charleroy@inra.fr>
 * @author Isabelle Nembrot <i.nembrot@laposte.net>
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class URINamespaces {

    private static final Map<String, String> CONTEXTS = new HashMap<>();
    private static final Map<String, String> NAMESPACES = new HashMap<>();
    private static final Map<String, String> OBJECTS = new HashMap<>();
    private static final Map<String, String> RELATIONS = new HashMap<>();
    private static final Map<String, String> W3C_NAMESPACES = new HashMap<>();
    private static String platform = null;
    
    // Add additionnal custom users namespaces to triplestore based namespace. e.g. platform => http://www.phenome-fppn.fr/platform/
    // These one override the namespaces found in rdf4j 
    /** @see allPaginateNamespacesProperties function for usage **/
    // For example if "platform" namespace is defined as http://www.phenome-fppn.fr/platform in rdf4j, this static function 
    // below will allow to override "plateform" in http://www.phenome-fppn.fr/ephesia 
    public static final Map<String, String> USER_SPECIFIC_NAMESPACES;
    static {
        Map<String, String> temporaryMap = new HashMap<>();
        //SILEX:info
        // Uncomment if you want to add other namespace
        // String plateform = PropertiesFileManager.getConfigFileProperty("sesame_rdf_config", "platform");
        // Can put multiple other namespaces
        // temporaryMap.put("platform", "http://www.phenome-fppn.fr/" + plateform);
        //\SILEX:info
        USER_SPECIFIC_NAMESPACES = Collections.unmodifiableMap(temporaryMap);
    }


    public URINamespaces() {
        platform = PropertiesFileManager.getConfigFileProperty("sesame_rdf_config", "platform");
        setContexts();
        setNamespaces();
        setW3CNamespaces();
        setRelations();
        setObjects();
    }

    public Map<String, String> getContexts() {
        return URINamespaces.CONTEXTS;
    }

    public String getContextsProperty(String prop) {
        String propValue = null;
        if (URINamespaces.CONTEXTS.containsKey(prop)) {
            propValue = URINamespaces.CONTEXTS.get(prop);
        }
        return propValue;
    }

    public String getObjectsProperty(String prop) {
        String propValue = null;
        if (URINamespaces.OBJECTS.containsKey(prop)) {
            propValue = URINamespaces.OBJECTS.get(prop);
        }
        return propValue;
    }

    public String getNamespaceProperty(String prop) {
        if (URINamespaces.NAMESPACES.containsKey(prop)) {
            return URINamespaces.NAMESPACES.get(prop);
        } else {
            return null;
        }
    }

    public boolean objectsPropertyContainsValue(String value) {
        return OBJECTS.containsValue(value);
    }

    public String getRelationsProperty(String prop) {
        String propValue = null;
        if (URINamespaces.RELATIONS.containsKey(prop)) {
            propValue = URINamespaces.RELATIONS.get(prop);
        }
        return propValue;
    }

    public String getW3CNamespacesProp(String prop) {
        String propValue = null;
        if (URINamespaces.W3C_NAMESPACES.containsKey(prop)) {
            propValue = URINamespaces.W3C_NAMESPACES.get(prop);
        }
        return propValue;
    }

    private void setContexts() {
        if (platform == null) {
            platform = "";
        }
        //px => prefix
        //c  => context
        //n  => namespace
        //p  => phenome

        //Plateforme et préfixes
        CONTEXTS.put("pxPhenome", "http://www.phenome-fppn.fr");
        CONTEXTS.put("pxDublinCore", "http://purl.org/dc/terms");
        CONTEXTS.put("pxPlatform", CONTEXTS.get("pxPhenome") + "/" + platform);
        CONTEXTS.put("pxGeoSPARQL", "http://www.opengis.net/ont/geosparql#");
        CONTEXTS.put("pxFoaf", "http://xmlns.com/foaf/0.1");
        CONTEXTS.put("pxOa", "http://www.w3.org/ns/oa");

        //Context(s) 
        CONTEXTS.put("pVoc2017", CONTEXTS.get("pxPhenome") + "/vocabulary/2017");
        CONTEXTS.put("documents", CONTEXTS.get("pxPlatform") + "/documents");
        CONTEXTS.put("agronomicalObjects", CONTEXTS.get("pxPlatform") + "/agronomicalObjects");
        CONTEXTS.put("variables", CONTEXTS.get("pxPlatform") + "/variables");
        CONTEXTS.put("vectors", CONTEXTS.get("pxPlatform") + "/vectors");
        CONTEXTS.put("sensors", CONTEXTS.get("pxPlatform") + "/sensors");
        CONTEXTS.put("annotations", CONTEXTS.get("pxPlatform") + "/set/annotation");
    }

    private void setNamespaces() {
        NAMESPACES.put("variables", CONTEXTS.get("pxPlatform") + "/id/variables");
        NAMESPACES.put("traits", CONTEXTS.get("pxPlatform") + "/id/traits");
        NAMESPACES.put("methods", CONTEXTS.get("pxPlatform") + "/id/methods");
        NAMESPACES.put("units", CONTEXTS.get("pxPlatform") + "/id/units");
    }

    private void setW3CNamespaces() {

//        W3C_NAMESPACES.put("time", "http://www.w3.org/2006/time");
    }

    private void setObjects() {
        //c , concept
        OBJECTS.put("cDocuments", CONTEXTS.get("pVoc2017") + "#Document");
        OBJECTS.put("cExperiment", CONTEXTS.get("pVoc2017") + "#Experiment");
        OBJECTS.put("cImage", CONTEXTS.get("pVoc2017") + "#Image");
        OBJECTS.put("cAgronomicalObject", CONTEXTS.get("pVoc2017") + "#AgronomicalObject");
        OBJECTS.put("cPlot", CONTEXTS.get("pVoc2017") + "#Plot");
        OBJECTS.put("cFields", CONTEXTS.get("pVoc2017") + "#Fields");
        OBJECTS.put("cCultivatedLand", CONTEXTS.get("pVoc2017") + "#CultivatedLand");
        OBJECTS.put("cVariety", CONTEXTS.get("pVoc2017") + "#Variety");
        OBJECTS.put("cGenotype", CONTEXTS.get("pVoc2017") + "#Genotype");
        OBJECTS.put("cSpecies", CONTEXTS.get("pVoc2017") + "#Species");
        OBJECTS.put("cVariable", CONTEXTS.get("pVoc2017") + "#Variable");
        OBJECTS.put("cTrait", CONTEXTS.get("pVoc2017") + "#Trait");
        OBJECTS.put("cMethod", CONTEXTS.get("pVoc2017") + "#Method");
        OBJECTS.put("cUnit", CONTEXTS.get("pVoc2017") + "#Unit");
        OBJECTS.put("cVector", CONTEXTS.get("pVoc2017") + "#Vector");
        OBJECTS.put("cSensingDevice", CONTEXTS.get("pVoc2017") + "#SensingDevice");
        OBJECTS.put("cRestriction", "owl:Restriction");
        OBJECTS.put("cAgent", CONTEXTS.get("pxFoaf") + "/Agent");
        OBJECTS.put("cPerson", CONTEXTS.get("pxFoaf") + "/Person");
        OBJECTS.put("cMotivation", CONTEXTS.get("pxOa") + "#Motivation");
        OBJECTS.put("cAnnotation", CONTEXTS.get("pxOa") + "#Annotation");
        OBJECTS.put("cInfrastructure", CONTEXTS.get("pVoc2017") + "#Infrastructure");
    }

    private void setRelations() {
        //r , relation
        //d , draft
        //v , validated draft

        RELATIONS.put("rHasDocument", CONTEXTS.get("pVoc2017") + "#hasDocument");
        RELATIONS.put("rConcern", CONTEXTS.get("pVoc2017") + "#concern");
        RELATIONS.put("rIsPartOf", CONTEXTS.get("pVoc2017") + "#isPartOf");
        RELATIONS.put("rHasPlot", CONTEXTS.get("pVoc2017") + "#hasPlot");
        RELATIONS.put("rFromGenotype", CONTEXTS.get("pVoc2017") + "#fromGenotype");
        RELATIONS.put("rFromVariety", CONTEXTS.get("pVoc2017") + "#fromVariety");
        RELATIONS.put("rFromSpecies", CONTEXTS.get("pVoc2017") + "#fromSpecies");
        RELATIONS.put("rExperimentModalities", CONTEXTS.get("pVoc2017") + "#hasExperimentModalities");
        RELATIONS.put("rHasRepetition", CONTEXTS.get("pVoc2017") + "#hasRepetition");
        RELATIONS.put("rHasAlias", CONTEXTS.get("pVoc2017") + "#hasAlias");
        RELATIONS.put("rStatus", CONTEXTS.get("pVoc2017") + "#status");
        RELATIONS.put("rHasContact", CONTEXTS.get("pVoc2017") + "#hasContact");
        RELATIONS.put("rHasTrait", CONTEXTS.get("pVoc2017") + "#hasTrait");
        RELATIONS.put("rHasMethod", CONTEXTS.get("pVoc2017") + "#hasMethod");
        RELATIONS.put("rHasUnit", CONTEXTS.get("pVoc2017") + "#hasUnit");
        RELATIONS.put("rHasBrand", CONTEXTS.get("pVoc2017") + "#hasBrand");
        RELATIONS.put("rMeasuredVariable", CONTEXTS.get("pVoc2017") + "#measuredVariable");
        RELATIONS.put("rInServiceDate", CONTEXTS.get("pVoc2017") + "#inServiceDate");
        RELATIONS.put("rDateOfPurchase", CONTEXTS.get("pVoc2017") + "#dateOfPurchase");
        RELATIONS.put("rDateOfLastCalibration", CONTEXTS.get("pVoc2017") + "#dateOfLastCalibration");
        RELATIONS.put("rDeviceProperty", CONTEXTS.get("pVoc2017") + "#deviceProperty");
        RELATIONS.put("rPersonInCharge", CONTEXTS.get("pVoc2017") + "#personInCharge");
        RELATIONS.put("rSerialNumber", CONTEXTS.get("pVoc2017") + "#serialNumber");

        //Relations skos
        RELATIONS.put("rExactMatch", "http://www.w3.org/2008/05/skos#exactMatch");
        RELATIONS.put("rCloseMatch", "http://www.w3.org/2008/05/skos#closeMatch");
        RELATIONS.put("rNarrower", "http://www.w3.org/2008/05/skos#narrower");
        RELATIONS.put("rBroader", "http://www.w3.org/2008/05/skos#broader");

        //Relations rdfs and rdf
        RELATIONS.put("subClassOf", "rdfs:subClassOf");
        RELATIONS.put("subClassOf*", "rdfs:subClassOf*");
        RELATIONS.put("subPropertyOf*", "rdfs:subPropertyOf*");
        RELATIONS.put("label", "rdfs:label");
        RELATIONS.put("comment", "rdfs:comment");
        RELATIONS.put("type", "rdf:type");
        RELATIONS.put("domain", "rdfs:domain");
        RELATIONS.put("rest", "rdf:rest");
        RELATIONS.put("first", "rdf:first");

        //relations owl
        RELATIONS.put("unionOf", "owl:unionOf");
        RELATIONS.put("onProperty", "owl:onProperty");
        RELATIONS.put("cardinality", "http://www.w3.org/2002/07/owl#cardinality");
        RELATIONS.put("minCardinality", "http://www.w3.org/2002/07/owl#minCardinality");
        RELATIONS.put("maxCardinality", "http://www.w3.org/2002/07/owl#maxCardinality");
        RELATIONS.put("qualifiedCardinality", "http://www.w3.org/2002/07/owl#qualifiedCardinality");

        //Relations oa
        RELATIONS.put("rTarget", CONTEXTS.get("pxOa") + "#hasTarget");
        RELATIONS.put("rBodyValue", CONTEXTS.get("pxOa") + "#bodyValue");
        RELATIONS.put("rMotivatedBy", CONTEXTS.get("pxOa") + "#motivatedBy");

        //Relations dcterms
        RELATIONS.put("rCreator", CONTEXTS.get("pxDublinCore") + "/creator");
        RELATIONS.put("rCreated", CONTEXTS.get("pxDublinCore") + "/created");

    }
}
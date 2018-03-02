//**********************************************************************************************
//                                       URINamespaces.java from uris.php
//
// Author(s): Isabelle NEMBROT, Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:i.nembrot@laposte.net, arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@supagro.inra.fr, pascal.neveu@supagro.inra.fr
// Last modification date:  June, 2017 (adaptation to PHIS field)
// Subject: personal parameters for global usage
// G_URI :
// - M3P URI for further use so that we don"t need to repeat paths in each page
// Usage :
// ~~~~~~ URINamespaces uris = new URINamespaces("m3p");
// ~~~~~~ String vocabulary = URINamespaces.getContextsProperty("pVocaPlateform")
//***********************************************************************************************

package phis2ws.service.configuration;

import java.util.HashMap;
import java.util.Map;
import phis2ws.service.PropertiesFileManager;


/**
 * Permet de creer des URI de concepts, objets et relation entre objets et proprietes en fonction pour une platform
 * @author A. Charleroy
 */
public class URINamespaces {

    private static final Map<String, String> CONTEXTS = new HashMap<>();
    private static final Map<String, String> NAMESPACES = new HashMap<>();
    private static final Map<String, String> OBJECTS = new HashMap<>();
    private static final Map<String, String> RELATIONS = new HashMap<>();
    private static final Map<String, String> W3C_NAMESPACES = new HashMap<>();
    private static String platform = null;

    public URINamespaces() {
        platform = PropertiesFileManager.getConfigFileProperty("sesame_rdf_config", "platform");;
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
        
        
        //Context(s) 
        CONTEXTS.put("pVoc2017", CONTEXTS.get("pxPhenome") + "/vocabulary/2017");
        CONTEXTS.put("documents", CONTEXTS.get("pxPlatform") + "/documents");
        CONTEXTS.put("agronomicalObjects", CONTEXTS.get("pxPlatform") + "/agronomicalObjects");
        CONTEXTS.put("variables", CONTEXTS.get("pxPlatform") + "/variables");
        
        
               
        
        //Contextes de phenomeApi version serre
//        CONTEXTS.put("pxPhenome", "http://www.phenome-fppn.fr");
//        CONTEXTS.put("pExperiment", "http://www.phenome-fppn.fr/m3p/experiment");
//        CONTEXTS.put("pVocaPlateform", "http://www.phenome-fppn.fr/vocabulary/2015");
//        CONTEXTS.put("annotation", "http://www.mistea.supagro.inra.fr/ontologies/2014/v1/annotation");
//        CONTEXTS.put("annotSemantic", "http://www.mistea.supagro.inra.fr/ontologies/2014/v1/semanticAnnotation");
//        CONTEXTS.put("pEvent", "http://www.phenome-fppn.fr/vocabulary/"+ platform + "/2015/event");
//        CONTEXTS.put("eventRepo", "http://www.phenome-fppn.fr/" + platform + "/event");
//        CONTEXTS.put("annotationRepo", "http://www.phenome-fppn.fr/" + platform + "/annotation");
//        CONTEXTS.put("documents", "http://www.mistea.supagro.inra.fr/ontologies/2014/v1/documents");
//        CONTEXTS.put("documentsRepo", "http://www.phenome-fppn.fr/" + platform + "/document");
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
    }

    private void setRelations() {
        //r , relation
        //d , draft
        //v , validated draft
        
        RELATIONS.put("rHasDocument", CONTEXTS.get("pVoc2017") + "#hasDocument");
        RELATIONS.put("rConcern", CONTEXTS.get("pVoc2017") + "#concern");
        RELATIONS.put("rHasPlot", CONTEXTS.get("pVoc2017") + "#hasPlot");
        RELATIONS.put("rFromGenotype", CONTEXTS.get("pVoc2017") + "#fromGenotype");
        RELATIONS.put("rFromVariety", CONTEXTS.get("pVoc2017") + "#fromVariety");
        RELATIONS.put("rExperimentModalities", CONTEXTS.get("pVoc2017") + "#hasExperimentModalities");
        RELATIONS.put("rHasRepetition", CONTEXTS.get("pVoc2017") + "#hasRepetition");
        RELATIONS.put("rHasAlias", CONTEXTS.get("pVoc2017") + "#hasAlias");
        RELATIONS.put("rStatus", CONTEXTS.get("pVoc2017") + "#status");
        RELATIONS.put("rHasTrait", CONTEXTS.get("pVoc2017") + "#hasTrait");
        RELATIONS.put("rHasMethod", CONTEXTS.get("pVoc2017") + "#hasMethod");
        RELATIONS.put("rHasUnit", CONTEXTS.get("pVoc2017") + "#hasUnit");
        
        
        //Relations skos
        RELATIONS.put("rExactMatch", "http://www.w3.org/2008/05/skos#exactMatch");
        RELATIONS.put("rCloseMatch", "http://www.w3.org/2008/05/skos#closeMatch");
        RELATIONS.put("rNarrower", "http://www.w3.org/2008/05/skos#narrower");
        RELATIONS.put("rBroader", "http://www.w3.org/2008/05/skos#broader");
        
        //Relations rdfs
        RELATIONS.put("subClassOf","rdfs:subClassOf");
        RELATIONS.put("subClassOf*","rdfs:subClassOf");
    }
}

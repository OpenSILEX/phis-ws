//**********************************************************************************************
//                                       ConfigurationFilesMetadataDTO.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: March 2017
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  March, 2017
// Subject: Represents the submitted JSON for the file metadata
//***********************************************************************************************
package phis2ws.service.resources.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;

@ApiModel
public class ConfigurationFilesMetadataDTO extends AbstractVerifiedClass {
    private String provider;
    private String clientPath;
    private String plateform;
    private String fromIP;
    private String device;
    private String filename;
    private String serverFilename;
    private String extension;
    private String checksum;
    
    @Override
    public Map rules() {
        Map<String,Boolean> rules = new HashMap<>();
        rules.put("provider", Boolean.TRUE);
        rules.put("clientPath", Boolean.FALSE);
        rules.put("plateform", Boolean.TRUE);
        rules.put("fromIP", Boolean.TRUE);
        rules.put("device", Boolean.FALSE);
        rules.put("filename", Boolean.TRUE);
        rules.put("extension", Boolean.TRUE);
        rules.put("checksum", Boolean.TRUE);
        rules.put("serverFilename", Boolean.FALSE);
        return rules;
    }

    @ApiModelProperty(example = "test/dzdz/dzdz")
    public String getClientPath() {
        return clientPath;
    }

    public void setClientPath(String clientPath) {
        this.clientPath = clientPath;
    }
    
    public String getServerFilename() {
        return serverFilename;
    }

    public void setServerFilename(String serverFilename) {
        this.serverFilename = serverFilename;
    }
    
    @ApiModelProperty(example = "106fa487baa1728083747de1c6df73e9")
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    
     @ApiModelProperty(example = "m3p")
    public String getPlateform() {
        return plateform;
    }

    public void setPlateform(String plateform) {
        this.plateform = plateform;
    }
    @ApiModelProperty(example = "147.99.7.11")
    public String getFromIP() {
        return fromIP;
    }

    public void setFromIP(String fromIP) {
        this.fromIP = fromIP;
    }

    @ApiModelProperty(example = "Tablet computer")
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
    
    
    @ApiModelProperty(example = "mistea")
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @ApiModelProperty(example = "test numero 50005")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    @ApiModelProperty(example = "jpg")
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    @Override
    public Object createObjectFromDTO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

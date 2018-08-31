//**********************************************************************************************
//                                       ImageMetadata.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: December, 8 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  December, 8 2017
// Subject: Represents the submitted JSON for the images
//***********************************************************************************************
package phis2ws.service.resources.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.resources.validation.interfaces.Required;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.resources.validation.interfaces.URL;
import phis2ws.service.view.model.phis.ImageMetadata;

/**
 * corresponds to the submitted JSON for the images metadata
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ImageMetadataDTO extends AbstractVerifiedClass {
    
    private String rdfType;
    private List<ConcernItemDTO> concern;
    private ShootingConfigurationDTO configuration;
    private FileInformationDTO fileInfo;

    @Override
    public ImageMetadata createObjectFromDTO() {
       ImageMetadata imageMetadata = new ImageMetadata();
       imageMetadata.setRdfType(rdfType);
       for (ConcernItemDTO concernItemDTO : concern) {
           imageMetadata.addConcernedItem(concernItemDTO.createObjectFromDTO());
       }
       
       imageMetadata.setConfiguration(configuration.createObjectFromDTO());
       imageMetadata.setFileInformations(fileInfo.createObjectFromDTO());
       
       return imageMetadata;
    }
    
    @URL
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_IMAGE_TYPE)
    public String getRdfType() {
        return rdfType;
    }

    public void setRdfType(String rdfType) {
        this.rdfType = rdfType;
    }
    
    @Valid
    public List<ConcernItemDTO> getConcern() {
        return concern;
    }

    public void setConcern(List<ConcernItemDTO> concern) {
        this.concern = concern;
    }

    @Valid
    public ShootingConfigurationDTO getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ShootingConfigurationDTO configuration) {
        this.configuration = configuration;
    }

    @Valid
    public FileInformationDTO getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInformationDTO fileInfo) {
        this.fileInfo = fileInfo;
    }
}

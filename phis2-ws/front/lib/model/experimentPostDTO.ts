/**
 * 
 * 
 *
 * 
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import { ContactPostgreSQL } from './contactPostgreSQL';


export interface ExperimentPostDTO { 
    startDate?: string;
    endDate?: string;
    field?: string;
    campaign?: string;
    place?: string;
    alias?: string;
    comment?: string;
    keywords?: string;
    objective?: string;
    cropSpecies?: string;
    projectsUris?: Array<string>;
    groupsUris?: Array<string>;
    contacts?: Array<ContactPostgreSQL>;
}

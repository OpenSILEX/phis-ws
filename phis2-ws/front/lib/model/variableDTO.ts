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
import { OntologyReference } from './ontologyReference';


export interface VariableDTO { 
    uri?: string;
    label?: string;
    comment?: string;
    ontologiesReferences?: Array<OntologyReference>;
    trait?: string;
    method?: string;
    unit?: string;
}

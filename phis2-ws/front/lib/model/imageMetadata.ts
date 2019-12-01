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
import { ConcernedItem } from './concernedItem';
import { FileInformations } from './fileInformations';
import { ShootingConfiguration } from './shootingConfiguration';


export interface ImageMetadata { 
    uri?: string;
    rdfType?: string;
    concernedItems?: Array<ConcernedItem>;
    configuration?: ShootingConfiguration;
    fileInformations?: FileInformations;
}

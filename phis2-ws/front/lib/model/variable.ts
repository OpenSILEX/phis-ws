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
import { Method } from './method';
import { OntologyReference } from './ontologyReference';
import { Property } from './property';
import { Trait } from './trait';
import { Unit } from './unit';


export interface Variable { 
    uri?: string;
    label?: string;
    comment?: string;
    ontologiesReferences?: Array<OntologyReference>;
    properties?: Array<Property>;
    trait?: Trait;
    method?: Method;
    unit?: Unit;
}

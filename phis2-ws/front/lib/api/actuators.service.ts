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
/* tslint:disable:no-unused-variable member-ordering */

import { Observable } from "rxjs/Observable";
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import IHttpClient from "../IHttpClient";
import { inject, injectable } from "inversify";
import { IAPIConfiguration } from "../IAPIConfiguration";
import { Headers } from "../Headers";
import HttpResponse from "../HttpResponse";

import { ActuatorDTO } from '../model/actuatorDTO';
import { ActuatorDetailDTO } from '../model/actuatorDetailDTO';
import { ActuatorPostDTO } from '../model/actuatorPostDTO';
import { ResponseFormPOST } from '../model/responseFormPOST';

import { COLLECTION_FORMATS }  from '../variables';



@injectable()
export class ActuatorsService {
    private basePath: string = 'https://localhost';

    constructor(@inject("IApiHttpClient") private httpClient: IHttpClient,
        @inject("IAPIConfiguration") private APIConfiguration: IAPIConfiguration ) {
        if(this.APIConfiguration.basePath)
            this.basePath = this.APIConfiguration.basePath;
    }
    /**
     * Get an actuator
     * Retrieve an actuator. Need URL encoded actuator URI
     * @param uri An actuator URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getActuatorDetails(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<ActuatorDetailDTO>>;
    public getActuatorDetails(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<ActuatorDetailDTO>>>;
    public getActuatorDetails(uri: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getActuatorDetails.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getActuatorDetails.');
        }

        let queryParameters: string[] = [];
        if (pageSize !== undefined) {
            queryParameters.push("pageSize="+encodeURIComponent(String(pageSize)));
        }
        if (page !== undefined) {
            queryParameters.push("page="+encodeURIComponent(String(page)));
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<ActuatorDetailDTO>>> = this.httpClient.get(`${this.basePath}/actuators/${encodeURIComponent(String(uri))}?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<ActuatorDetailDTO>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all actuators corresponding to the search params given
     * Retrieve all actuators authorized for the user corresponding to the searched params given
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     * @param uri Search by uri
     * @param rdfType Search by type uri
     * @param label Search by label
     * @param brand Search by brand
     * @param serialNumber Search by serial number
     * @param model Search by model
     * @param inServiceDate Search by service date
     * @param dateOfPurchase Search by date of purchase
     * @param dateOfLastCalibration Search by date of last calibration
     * @param personInCharge Search by person in charge
     
     */
    public getActuatorsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, rdfType?: string, label?: string, brand?: string, serialNumber?: string, model?: string, inServiceDate?: string, dateOfPurchase?: string, dateOfLastCalibration?: string, personInCharge?: string, observe?: 'body', headers?: Headers): Observable<Array<ActuatorDTO>>;
    public getActuatorsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, rdfType?: string, label?: string, brand?: string, serialNumber?: string, model?: string, inServiceDate?: string, dateOfPurchase?: string, dateOfLastCalibration?: string, personInCharge?: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<ActuatorDTO>>>;
    public getActuatorsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, rdfType?: string, label?: string, brand?: string, serialNumber?: string, model?: string, inServiceDate?: string, dateOfPurchase?: string, dateOfLastCalibration?: string, personInCharge?: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getActuatorsBySearch.');
        }

        let queryParameters: string[] = [];
        if (pageSize !== undefined) {
            queryParameters.push("pageSize="+encodeURIComponent(String(pageSize)));
        }
        if (page !== undefined) {
            queryParameters.push("page="+encodeURIComponent(String(page)));
        }
        if (uri !== undefined) {
            queryParameters.push("uri="+encodeURIComponent(String(uri)));
        }
        if (rdfType !== undefined) {
            queryParameters.push("rdfType="+encodeURIComponent(String(rdfType)));
        }
        if (label !== undefined) {
            queryParameters.push("label="+encodeURIComponent(String(label)));
        }
        if (brand !== undefined) {
            queryParameters.push("brand="+encodeURIComponent(String(brand)));
        }
        if (serialNumber !== undefined) {
            queryParameters.push("serialNumber="+encodeURIComponent(String(serialNumber)));
        }
        if (model !== undefined) {
            queryParameters.push("model="+encodeURIComponent(String(model)));
        }
        if (inServiceDate !== undefined) {
            queryParameters.push("inServiceDate="+encodeURIComponent(String(inServiceDate)));
        }
        if (dateOfPurchase !== undefined) {
            queryParameters.push("dateOfPurchase="+encodeURIComponent(String(dateOfPurchase)));
        }
        if (dateOfLastCalibration !== undefined) {
            queryParameters.push("dateOfLastCalibration="+encodeURIComponent(String(dateOfLastCalibration)));
        }
        if (personInCharge !== undefined) {
            queryParameters.push("personInCharge="+encodeURIComponent(String(personInCharge)));
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<ActuatorDTO>>> = this.httpClient.get(`${this.basePath}/actuators?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<ActuatorDTO>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Post actuator(s)
     * Register actuator(s) in the database
     * @param Authorization Access token given
     * @param body JSON format to insert actuators
     
     */
    public post(Authorization: string, body?: Array<ActuatorPostDTO>, observe?: 'body', headers?: Headers): Observable<ResponseFormPOST>;
    public post(Authorization: string, body?: Array<ActuatorPostDTO>, observe?: 'response', headers?: Headers): Observable<HttpResponse<ResponseFormPOST>>;
    public post(Authorization: string, body?: Array<ActuatorPostDTO>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling post.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<ResponseFormPOST>> = this.httpClient.post(`${this.basePath}/actuators`, body , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ResponseFormPOST>(httpResponse.response));
        }
        return response;
    }
    /**
     * Put actuator(s)
     * Update actuator(s) in the database
     * @param Authorization Access token given
     * @param body JSON format to insert actuators
     
     */
    public put(Authorization: string, body?: Array<ActuatorDTO>, observe?: 'body', headers?: Headers): Observable<ResponseFormPOST>;
    public put(Authorization: string, body?: Array<ActuatorDTO>, observe?: 'response', headers?: Headers): Observable<HttpResponse<ResponseFormPOST>>;
    public put(Authorization: string, body?: Array<ActuatorDTO>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling put.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<ResponseFormPOST>> = this.httpClient.put(`${this.basePath}/actuators`, body , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ResponseFormPOST>(httpResponse.response));
        }
        return response;
    }
    /**
     * Update the measured variables of an actuator
     * 
     * @param uri An actuator URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param body List of variables uris
     
     */
    public putMeasuredVariables(uri: string, Authorization: string, body?: Array<string>, observe?: 'body', headers?: Headers): Observable<ResponseFormPOST>;
    public putMeasuredVariables(uri: string, Authorization: string, body?: Array<string>, observe?: 'response', headers?: Headers): Observable<HttpResponse<ResponseFormPOST>>;
    public putMeasuredVariables(uri: string, Authorization: string, body?: Array<string>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling putMeasuredVariables.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling putMeasuredVariables.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<ResponseFormPOST>> = this.httpClient.put(`${this.basePath}/actuators/${encodeURIComponent(String(uri))}/variables`, body , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ResponseFormPOST>(httpResponse.response));
        }
        return response;
    }
}

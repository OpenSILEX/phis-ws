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

import { RadiometricTargetDTO } from '../model/radiometricTargetDTO';
import { RadiometricTargetPostDTO } from '../model/radiometricTargetPostDTO';
import { RdfResourceDefinitionDTO } from '../model/rdfResourceDefinitionDTO';
import { ResponseFormPOST } from '../model/responseFormPOST';

import { COLLECTION_FORMATS }  from '../variables';



@injectable()
export class RadiometricTargetsService {
    private basePath: string = 'https://localhost';

    constructor(@inject("IApiHttpClient") private httpClient: IHttpClient,
        @inject("IAPIConfiguration") private APIConfiguration: IAPIConfiguration ) {
        if(this.APIConfiguration.basePath)
            this.basePath = this.APIConfiguration.basePath;
    }
    /**
     * Get all radiometric targets corresponding to the search params given
     * Retrieve all radiometric targets authorized for the user corresponding to the searched params given
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     * @param uri Search by uri
     * @param label Search by label
     
     */
    public getRadiometricTargetsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, label?: string, observe?: 'body', headers?: Headers): Observable<Array<RdfResourceDefinitionDTO>>;
    public getRadiometricTargetsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, label?: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<RdfResourceDefinitionDTO>>>;
    public getRadiometricTargetsBySearch(Authorization: string, pageSize?: number, page?: number, uri?: string, label?: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getRadiometricTargetsBySearch.');
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
        if (label !== undefined) {
            queryParameters.push("label="+encodeURIComponent(String(label)));
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<RdfResourceDefinitionDTO>>> = this.httpClient.get(`${this.basePath}/radiometricTargets?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<RdfResourceDefinitionDTO>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all radiometric target&#39;s details corresponding to the search uri
     * Retrieve all radiometric target&#39;s details authorized for the user corresponding to the searched uri
     * @param uri An infrastructure URI (Unique Resource Identifier)
     * @param Authorization Access token given
     
     */
    public getRadiometricTargetsDetails(uri: string, Authorization: string, observe?: 'body', headers?: Headers): Observable<Array<RdfResourceDefinitionDTO>>;
    public getRadiometricTargetsDetails(uri: string, Authorization: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<RdfResourceDefinitionDTO>>>;
    public getRadiometricTargetsDetails(uri: string, Authorization: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getRadiometricTargetsDetails.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getRadiometricTargetsDetails.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<RdfResourceDefinitionDTO>>> = this.httpClient.get(`${this.basePath}/radiometricTargets/${encodeURIComponent(String(uri))}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<RdfResourceDefinitionDTO>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Post radiometric(s) target(s) 
     * Register radiometric(s) target(s) in the database
     * @param Authorization Access token given
     * @param body JSON format of radiometric target data
     
     */
    public postRadiometricTargets(Authorization: string, body?: Array<RadiometricTargetPostDTO>, observe?: 'body', headers?: Headers): Observable<ResponseFormPOST>;
    public postRadiometricTargets(Authorization: string, body?: Array<RadiometricTargetPostDTO>, observe?: 'response', headers?: Headers): Observable<HttpResponse<ResponseFormPOST>>;
    public postRadiometricTargets(Authorization: string, body?: Array<RadiometricTargetPostDTO>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling postRadiometricTargets.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<ResponseFormPOST>> = this.httpClient.post(`${this.basePath}/radiometricTargets`, body , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ResponseFormPOST>(httpResponse.response));
        }
        return response;
    }
    /**
     * Update radiometric targets
     * 
     * @param Authorization Access token given
     * @param body JSON format of radiometric target data
     
     */
    public put4(Authorization: string, body?: Array<RadiometricTargetDTO>, observe?: 'body', headers?: Headers): Observable<ResponseFormPOST>;
    public put4(Authorization: string, body?: Array<RadiometricTargetDTO>, observe?: 'response', headers?: Headers): Observable<HttpResponse<ResponseFormPOST>>;
    public put4(Authorization: string, body?: Array<RadiometricTargetDTO>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling put4.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<ResponseFormPOST>> = this.httpClient.put(`${this.basePath}/radiometricTargets`, body , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ResponseFormPOST>(httpResponse.response));
        }
        return response;
    }
}

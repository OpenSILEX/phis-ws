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

import { Ask } from '../model/ask';
import { Uri } from '../model/uri';

import { COLLECTION_FORMATS }  from '../variables';



@injectable()
export class UriService {
    private basePath: string = 'https://localhost';

    constructor(@inject("IApiHttpClient") private httpClient: IHttpClient,
        @inject("IAPIConfiguration") private APIConfiguration: IAPIConfiguration ) {
        if(this.APIConfiguration.basePath)
            this.basePath = this.APIConfiguration.basePath;
    }
    /**
     * Get all the ancestor of an uri
     * Retrieve all Class parents of the uri
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getAncestors(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getAncestors(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getAncestors(uri: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getAncestors.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getAncestors.');
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

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/ancestors?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all the descendants of an uri
     * Retrieve all subclass and the subClass of subClass too
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getDescendants(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getDescendants(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getDescendants(uri: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getDescendants.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getDescendants.');
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

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/descendants?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all the instances of a concept
     * Retrieve all instances of subClass too, if deep &#x3D; true
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param deep true or false deppending if you want instances of concept progenity
     * @param language true or false deppending if you want instances of concept progenity
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getInstancesByConcept(uri: string, Authorization: string, deep?: boolean, language?: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getInstancesByConcept(uri: string, Authorization: string, deep?: boolean, language?: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getInstancesByConcept(uri: string, Authorization: string, deep?: boolean, language?: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getInstancesByConcept.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getInstancesByConcept.');
        }

        let queryParameters: string[] = [];
        if (deep !== undefined) {
            queryParameters.push("deep="+encodeURIComponent(String(deep)));
        }
        if (language !== undefined) {
            queryParameters.push("language="+encodeURIComponent(String(language)));
        }
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

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/instances?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all the siblings of an Uri
     * Retrieve all Class with same parent
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getSibblings(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getSibblings(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getSibblings(uri: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getSibblings.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getSibblings.');
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

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/siblings?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * get the type of an uri if it exist
     * 
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     
     */
    public getTypeIfUriExist(uri: string, Authorization: string, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getTypeIfUriExist(uri: string, Authorization: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getTypeIfUriExist(uri: string, Authorization: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getTypeIfUriExist.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getTypeIfUriExist.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/type`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * Get all uri informations
     * Retrieve all infos of the uri
     * @param uri Search by uri
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getUriMetadata(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getUriMetadata(uri: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getUriMetadata(uri: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling getUriMetadata.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getUriMetadata.');
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

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * get all uri with a given label
     * Retrieve all uri from the label given
     * @param label Search by label
     * @param Authorization Access token given
     * @param pageSize Number of elements per page (limited to 150000)
     * @param page Current page number
     
     */
    public getUrisByLabel(label: string, Authorization: string, pageSize?: number, page?: number, observe?: 'body', headers?: Headers): Observable<Array<Uri>>;
    public getUrisByLabel(label: string, Authorization: string, pageSize?: number, page?: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Uri>>>;
    public getUrisByLabel(label: string, Authorization: string, pageSize?: number, page?: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!label){
            throw new Error('Required parameter label was null or undefined when calling getUrisByLabel.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling getUrisByLabel.');
        }

        let queryParameters: string[] = [];
        if (pageSize !== undefined) {
            queryParameters.push("pageSize="+encodeURIComponent(String(pageSize)));
        }
        if (page !== undefined) {
            queryParameters.push("page="+encodeURIComponent(String(page)));
        }
        if (label !== undefined) {
            queryParameters.push("label="+encodeURIComponent(String(label)));
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<Uri>>> = this.httpClient.get(`${this.basePath}/uri?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Uri>>(httpResponse.response));
        }
        return response;
    }
    /**
     * check if an uri exists or not (in the triplestore)
     * Return a boolean
     * @param uri A concept URI (Unique Resource Identifier)
     * @param Authorization Access token given
     
     */
    public isUriExisting(uri: string, Authorization: string, observe?: 'body', headers?: Headers): Observable<Array<Ask>>;
    public isUriExisting(uri: string, Authorization: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Ask>>>;
    public isUriExisting(uri: string, Authorization: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!uri){
            throw new Error('Required parameter uri was null or undefined when calling isUriExisting.');
        }

        if (!Authorization){
            throw new Error('Required parameter Authorization was null or undefined when calling isUriExisting.');
        }

        if (Authorization) {
            headers['Authorization'] = String(Authorization);
        }

        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<Array<Ask>>> = this.httpClient.get(`${this.basePath}/uri/${encodeURIComponent(String(uri))}/exist`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Ask>>(httpResponse.response));
        }
        return response;
    }
}

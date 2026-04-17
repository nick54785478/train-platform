import { Injectable } from '@angular/core';
import { TemplateQueriedResource } from '../models/template-queried-resource.model';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { UploadTemplateResource } from '../models/template-upload-resource.model';
import { BaseResponse } from '../../../shared/models/base-response.model';

@Injectable({
  providedIn: 'root',
})
export class TemplateUploadService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 取得該範本的資訊
   * @param type
   * @returns
   */
  queryTemplate(type: string): Observable<TemplateQueriedResource> {
    const url = this.baseApiUrl + '/template';
    let params = new HttpParams().set('type', type);
    return this.http.get<TemplateQueriedResource>(url, { params });
  }

  /**
   * 進行上傳
   * @param formData
   * @returns
   */
  upload(formData: FormData): Observable<BaseResponse> {
    const url = this.baseApiUrl + '/template/upload';
    return this.http.post<BaseResponse>(url, formData);
  }
}

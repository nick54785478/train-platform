import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BaseResponse } from '../models/base-response.model';
import { Observable } from 'rxjs/internal/Observable';
import { UpdateCustomizedValueResource } from '../models/update-customized-value-resource.model';
import { CustomisationQueriedResource } from '../models/customisation-queried.model';

@Injectable({
  providedIn: 'root',
})
export class CustomisationService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 更新該使用者的個人設定
   * @param request
   * @return Observable<BaseResponse>
   */
  updateCustomizedValue(
    username: string,
    request: UpdateCustomizedValueResource
  ): Observable<BaseResponse> {
    const url = this.baseApiUrl + '/customisation/' + username;
    return this.http.put<BaseResponse>(url, request);
  }

  /**
   * 查詢表格個人化配置
   * @param username
   * @param type
   */
  queryTableColumnCustomisation(
    username: string,
    dataType: string,
    type: string
  ): Observable<CustomisationQueriedResource> {
    const url = this.baseApiUrl + '/customisation/' + username;
    let params = new HttpParams()
      .set('dataType', dataType ? dataType : '')
      .set('type', type ? type : '');
    return this.http.get<CustomisationQueriedResource>(url, { params });
  }
}

import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { CreateSetting } from '../models/create-setting-request.model';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { BaseResponse } from '../../../shared/models/base-response.model';
import { UpdateSetting } from '../models/update-setting-request.model';
import { CustomisationQueriedResource } from '../../../shared/models/customisation-queried.model';
import { SettingQueriedResource } from '../models/setting-queried-resource.model';
import { UpdateCustomizedValueResource } from '../../../shared/models/update-customized-value-resource.model';

@Injectable({
  providedIn: 'root',
})
export class SettingService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 新增一筆 Setting 資料
   * @param request
   * @return Observable<any>
   */
  create(request: CreateSetting): Observable<any> {
    const url = this.baseApiUrl + '/settings';
    return this.http.post(url, request);
  }

  /**
   * 更新一筆資料
   * @param request
   * @return Observable<any>
   */
  update(id: number, request: UpdateSetting): Observable<any> {
    const url = this.baseApiUrl + '/settings/' + id;
    return this.http.put(url, request);
  }

  /**
   * 查詢
   * @param dataType
   * @param type
   * @param name
   */
  query(
    dataType: string,
    type: string,
    name: string,
    activeFlag: string
  ): Observable<SettingQueriedResource[]> {
    const url = this.baseApiUrl + '/settings/query';
    let params = new HttpParams()
      .set('dataType', dataType ? dataType : '')
      .set('type', type ? type : '')
      .set('name', name ? name : '')
      .set('activeFlag', activeFlag ? activeFlag : '');
    return this.http.get<SettingQueriedResource[]>(url, { params });
  }

  /**
   * 刪除
   *
   * @param id
   */
  delete(id: number): Observable<BaseResponse> {
    const url = this.baseApiUrl + '/settings/' + id;
    return this.http.delete<BaseResponse>(url);
  }
}

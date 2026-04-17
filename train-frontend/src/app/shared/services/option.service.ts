import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Option } from '../models/option.model';
import { Observable } from 'rxjs/internal/Observable';
import { map } from 'rxjs/internal/operators/map';
import { environment } from '../../../environments/environment';
import { userInfo } from 'os';
import { UserInfoOption } from '../models/userinfo-option.model';
import { RoleInfoOption } from '../models/role-info-option.model';
import { GroupInfoOption } from '../models/group-info-option.model';
import { DataType } from '../../core/enums/data-type.enum';

@Injectable({
  providedIn: 'root',
})
export class OptionService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 取得 Data Type 配置資料
   * @return Observable<MenuItem[]
   */
  public getDataTypes(): Observable<Option[]> {
    return this.http.get<Option[]>('/data-type.json').pipe(
      map((response) => {
        return response;
      })
    );
  }

  /**
   * 取得 Setting Type 種類
   * @param dataType DataType
   * @return  Observable<Option[]>
   */
  public getSettingTypes(dataType: string): Observable<Option[]> {
    const url = this.baseApiUrl + '/options/query';
    let params = new HttpParams().set('dataType', dataType ? dataType : '');
    return this.http.get<Option[]>(url, { params }).pipe(
      map((response) => {
        return response;
      })
    );
  }

  /**
   * 取得 Setting 種類
   * @param dataType DataType
   * @return  Observable<Option[]>
   */
  public getSettingsByDataTypeAndType(type: string): Observable<Option[]> {
    const url = this.baseApiUrl + '/options/query/' + DataType.CUSTOMISATION;
    let params = new HttpParams().set('type', type ? type : '');
    return this.http.get<Option[]>(url, { params }).pipe(
      map((response) => {
        return response;
      })
    );
  }

  /**
   * 取得火車種類的下拉式選單資料
   *
   * @return List<OptionResource>
   */
  public getTrainKinds(): Observable<Option[]> {
    const url = this.baseApiUrl + '/options/trainKinds';
    return this.http.get<Option[]>(url).pipe(
      map((response) => {
        return response;
      })
    );
  }

  /**
   * 取得票別的下拉式選單資料
   *
   * @return List<OptionResource>
   */
  public getTicketTypes(): Observable<Option[]> {
    const url = this.baseApiUrl + '/options/ticketTypes';
    return this.http.get<Option[]>(url).pipe(
      map((response) => {
        return response;
      })
    );
  }

  /**
   * 取得車次號的下拉式選單資料
   *
   * @return List<OptionResource>
   */
  public getTrainNoList(): Observable<Option[]> {
    const url = this.baseApiUrl + '/options/trainNoList';
    return this.http.get<Option[]>(url).pipe(
      map((response) => {
        return response;
      })
    );
  }
}

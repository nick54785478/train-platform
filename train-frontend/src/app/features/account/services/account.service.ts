import { Injectable } from '@angular/core';
import { StorageService } from '../../../core/services/storage.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MoneyAccountQueriedResource } from '../models/money-account-queried-resource.model';
import { environment } from '../../../../environments/environment';
import { BookingQueriedResource } from '../models/booking-queried-resource.model';
import { BaseResponse } from '../../../shared/models/base-response.model';
import { DepositMoneyResource } from '../models/deposit-money-resource.model';
import { MoneyDepositedResource } from '../models/money-deposited-resource.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 取得儲值帳號資料
   * @param username
   * @returns
   */
  getAccountData(username: string): Observable<MoneyAccountQueriedResource> {
    const url = this.baseApiUrl + '/account';
    let params = new HttpParams().set('username', username ? username : '');
    return this.http.get<MoneyAccountQueriedResource>(url, { params });
  }

  /**
   * 查詢該使用者的訂票資訊
   *
   * @param username
   */
  getBooking(username: string): Observable<BookingQueriedResource> {
    const url = this.baseApiUrl + '/booking/' + username;
    return this.http.get<BookingQueriedResource>(url);
  }

  /**
   * 儲值
   *
   * @param requestData
   */
  deposit(
    requestData: DepositMoneyResource
  ): Observable<MoneyDepositedResource> {
    const url = this.baseApiUrl + '/account/deposit';
    return this.http.post<MoneyDepositedResource>(url, requestData);
  }
}

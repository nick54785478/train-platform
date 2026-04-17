import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { CreateMoneyAccountResource } from '../models/create-money-account-resource.model';
import { BaseResponse } from '../../../shared/models/base-response.model';
import { Observable } from 'rxjs/internal/Observable';
import { HttpClient } from '@angular/common/http';
import { MoneyAccountCreatedResource } from '../models/money-account-created-resource.model';

@Injectable({
  providedIn: 'root',
})
export class RegisterService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 進行註冊動作
   * @param request
   */
  create(request: CreateMoneyAccountResource): Observable<MoneyAccountCreatedResource> {
    const url = this.baseApiUrl + '/account/register';
    return this.http.post<MoneyAccountCreatedResource>(url, request);
  }
}

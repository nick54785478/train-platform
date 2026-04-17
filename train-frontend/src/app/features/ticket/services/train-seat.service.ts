import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { UnbookedSeatGottenResource } from '../models/unbooked-ticket-gotten-resource.model';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class TrainSeatService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 取得車票座位資訊
   * @param trainUuid
   * @param takeDate
   * @return 車票座位資訊
   */
  getSeatInfo(
    trainUuid: string,
    takeDate: string
  ): Observable<UnbookedSeatGottenResource> {
    const url = this.baseApiUrl + '/seats/unbooked';
    let params = new HttpParams()
      .set('trainUuid', trainUuid ? trainUuid : '')
      .set('takeDate', takeDate ? takeDate : '');
    return this.http.get<UnbookedSeatGottenResource>(url, { params });
  }
}

import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { EmailTemplateGottenData } from '../models/email-template-queried-resource.model';
import { SaveEmailTemplate } from '../models/save-email-template-resource.model';
import { EmailTemplateSavedResource } from '../models/save-email-template-resource.model copy';

@Injectable({
  providedIn: 'root',
})
export class EmailTemplateService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}

  /**
   * 取得信件範本資料 (By Template Key)
   * @param templateKey 唯一值
   */
  getEmailTemplateByKey(
    templateKey: string,
  ): Observable<EmailTemplateGottenData> {
    const url = this.baseApiUrl + '/email-templates/' + templateKey;
    return this.http.get<EmailTemplateGottenData>(url).pipe(
      map((response) => {
        return response;
      }),
    );
  }

  /**
   * 新增/修改一筆 EmailTemplate 資料
   * @param request
   * @return Observable<any>
   */
  saveEmailTemplate(request: SaveEmailTemplate): Observable<any> {
    const url = this.baseApiUrl + '/email-templates';
    return this.http.post(url, request);
  }
}

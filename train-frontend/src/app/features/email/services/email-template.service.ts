import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class EmailTemplateService {
  private readonly baseApiUrl = environment.apiEndpoint;

  constructor(private http: HttpClient) {}
}

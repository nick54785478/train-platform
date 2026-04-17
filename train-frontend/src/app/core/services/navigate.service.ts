import { Injectable } from '@angular/core';
import { Navigation, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class NavigateService {
  constructor(private router: Router) {}

  /**
   * 進行重導向
   * @param url
   */
  navigate(url: string) {
    this.router.navigateByUrl(url);
  }

  /**
   * 透過參數進行重導向
   * @param redirectUrl
   * @param queryParams
   */
  navigateWithQueryParam(redirectUrl: string, queryParams: string) {
    this.router.navigate([redirectUrl], {
      queryParams: JSON.parse(queryParams),
    });
  }

  /**
   * 透過 States 帶資料進行重導向
   * @param redirectUrl
   * @param data
   */
  navigateWithStates(redirectUrl: string, data: any) {
    this.router.navigateByUrl(redirectUrl, {
      state: { data: data },
    });
  }

  /**
   * 取得被傳遞的現有資料
   */
  getCurrentNavigation(): any {
    return this.router.getCurrentNavigation();
  }
}

import { Type } from '@angular/core';

export interface DialogConfig {
  /**
   * 開窗的內容要用哪個 Component 來顯示
   */
  component: Type<any>;

  /**
   * 要對這筆資料做什麼操作
   */
  dataAction: any;

  /**
   * 開窗後 Dialog 的標題
   */
  header: string;

  /**
   * 傳入的資料
   */
  data: any;

  /**
   * Dialog 的寬度，預設是 99vw
   */
  width?: string;

  /**
   * Dialog 的高度，預設是 98vh
   */
  height?: string;

  /**
   * Dialog 可不可以變更大小，預設是 false
   */
  resizable?: boolean;

  /**
   * Dialog 可不可以移動，預設是 false
   */
  draggable?: boolean;
}

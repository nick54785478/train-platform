import { WorkBook } from "xlsx";
import { Option } from "./option.model";

/**
 * 透過 SheetJS CE (XLSX) 轉換上傳檔案後的結果資料。
 */
export interface ExcelData {
  /**
   * 上傳檔案的名稱
   */
  fileName: string;

  /**
   * 上傳檔案的內容
   */
  workBook: WorkBook;

  /**
   * 上傳檔案裡的 sheet 轉換成的下拉選單清單
   */
  sheetNameOptions: Option[];

  /**
   * 原始上傳檔案
   */
  uploadFile: File;
}

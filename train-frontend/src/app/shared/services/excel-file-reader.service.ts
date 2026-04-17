import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { Option } from '../models/option.model';
import { ExcelData } from '../models/excel-data.model';

/**
 * 讀取 Excel 並透過 SheetJS CE (XLSX) 進行轉換的服務。
 */
@Injectable({
  providedIn: 'root',
})
export class ExcelFileReaderService {
  constructor() {}

  /**
   * 驗證檔案是否超過大小上限
   * @param file File
   * @param maxFileSize 檔案大小上限: 多少 MB
   * @returns 是否超過大小上限
   */
  fileSizeInvalid(file: File, maxFileSize: number): boolean {
    return file.size > maxFileSize * 1024 * 1024;
  }

  /**
   * 解析上傳的檔案
   * @param file File
   * @param maxRow 最多取幾列的資料
   * @returns ExcelData
   */
  parseFile(file: File, maxRow: number = 50): Promise<ExcelData> {
    return new Promise((resolve, reject) => {
      // 判斷是否為 csv 檔
      const isCSV = file.name.split('.').reverse()[0] == 'csv';

      const reader = new FileReader();
      reader.onloadstart = function (e) {
        // TODO 還需要做什麼?
      };

      reader.onload = function (e) {
        const fileContent = reader.result as ArrayBuffer;

        let xlsxData: any = fileContent;
        if (isCSV) {
          // 只有 csv 特別處理，只判斷 big5 且用 � 來判斷真的是不得已的
          // 有時間再來找比較正確的解法，先延續其他專案的判斷方式
          const big5String = new TextDecoder('big5').decode(
            new Uint8Array(fileContent)
          );
          console.log('big5String = ' + big5String);
          const isBig5 = big5String.indexOf('�') === -1;
          xlsxData = big5String;
        }

        try {
          // 把檔案透過 xlsx 轉出成 workbook 的 JSON 資料
          const workbook = XLSX.read(xlsxData, {
            type: 'binary',
            cellStyles: true,
            sheetRows: maxRow,
          });
          console.log('workbook = ' + JSON.stringify(workbook));

          // 設定 SheetName 下拉選單，從 SheetNames 裡面取，id 和 value 都存 sheetName 在 SheetNames 裡的 index
          const sheetNameOptions: Option[] = workbook.SheetNames.map(
            (name: string, index: number) => {
              return { id: index.toString(), value: name, label: name };
            }
          );

          resolve({
            uploadFile: file,
            fileName: file.name,
            workBook: workbook,
            sheetNameOptions: sheetNameOptions,
          });
        } catch (error) {
          reject(error);
        }
      };

      // TODO 這段可能會有問題，當發生錯誤的時候要回傳什麼
      reader.onerror = function (e) {
        console.error('An error occurred during Excel loading');
        reject('An error occurred during Excel loading');
      };

      if (isCSV) {
        // 將文件讀取為 ArrayBuffer
        reader.readAsArrayBuffer(file);
      } else {
        // 將文件讀取為二進位字元串
        reader.readAsBinaryString(file);
      }
    });
  }

  /**
   * 取得檔案名稱
   * @param contentDisposition Header
   * */
  getFileName(contentDisposition: any): string {
    console.log(contentDisposition);
    let filename = 'FileName';
    const regex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
    const matches = regex.exec(contentDisposition);
    if (matches != null && matches[1]) {
      filename = matches[1];
    }
    return filename;
  }
}

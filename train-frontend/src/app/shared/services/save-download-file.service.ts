import { Injectable } from '@angular/core';
import * as FileSaver from 'file-saver';

@Injectable({
  providedIn: 'root',
})
export class SaveDownloadFileService {
  constructor() {}

  // 儲存成xlsx
  saveBufferAsXlsx(buffer: any, fileName: string): void {
    let EXCEL_TYPE =
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE,
    });
    FileSaver.saveAs(data, fileName);
  }

  // 儲存成Zip
  saveBufferAsZip(buffer: any, fileName: string): void {
    let ZIP_TYPE = 'application/zip;charset=UTF-8';
    const data: Blob = new Blob([buffer], {
      type: ZIP_TYPE,
    });
    FileSaver.saveAs(data, fileName);
  }
}

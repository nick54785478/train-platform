import * as Jspreadsheet from 'jspreadsheet-ce';
import * as XLSX from 'xlsx';

export class JspreadsheetWrapper {
  jspreadsheet: Jspreadsheet.JspreadsheetInstance | undefined;

  constructor(jspreadsheetInstance: Jspreadsheet.JspreadsheetInstance) {
    this.jspreadsheet = jspreadsheetInstance;
  }

  /**
   * 把 xlxs 讀出上傳的 excel workbook 轉成 jspreadsheet 用到的 JSpreadsheetOptions
   *
   * 注意；
   * (1) Jspreadsheet CE 不能針對每個 cell 設定格式，所以日期格式顯示會與 excel 內看到的不同 ( Jspreadsheet Pro 可以 )
   * (2) workbook 沒有字體顏色資料
   *
   * @param workbook xlsx WorkBook
   * @param sheetName 要取哪個一個 sheet 的內容
   * @param maxRow 最多取幾列
   * @param maxColumn 最多取幾欄
   * @returns JSpreadsheetOptions
   */
  static convertWorkbookToJspreadsheetOptions(
    workbook: XLSX.WorkBook,
    sheetName: string,
    maxRow: number,
    maxColumn: number
  ): Jspreadsheet.JSpreadsheetOptions {
    let jspreadsheetOptions: Jspreadsheet.JSpreadsheetOptions = {};
    jspreadsheetOptions.editable = false;
    jspreadsheetOptions.minDimensions = [10, 10];
    jspreadsheetOptions.contextMenu = (instance, colIndex, rowIndex, event) => {
      const items = [];
      items.push({
        title: '',
      });
      return items;
    };

    // 判斷要轉換的 sheetName 是否存在於 workbook 內，如果不存在就直接回傳
    if (!workbook.SheetNames.includes(sheetName)) {
      return jspreadsheetOptions;
    }

    // 取得指定的工作表
    const worksheet: XLSX.WorkSheet = workbook.Sheets[sheetName];

    // 設定列高
    this.setRows(worksheet, jspreadsheetOptions, maxRow);

    // 設定欄寬
    this.setColumns(worksheet, jspreadsheetOptions, maxColumn);

    // 設定合併儲存格
    this.setMergeCells(worksheet, jspreadsheetOptions, maxRow);

    // 設定資料及樣式
    this.setDataAndStyle(worksheet, jspreadsheetOptions, maxRow, maxColumn);

    return jspreadsheetOptions;
  }

  /**
   * JSpreadsheetOptions mergeCells 設定
   *
   * @param worksheet 指定的工作表
   * @param jspreadsheetOptions JSpreadsheetOptions
   * @param maxRow 最多取幾列
   */
  private static setMergeCells(
    worksheet: XLSX.WorkSheet,
    jspreadsheetOptions: Jspreadsheet.JSpreadsheetOptions,
    maxRow: number
  ): void {
    if (worksheet['!merges']) {
      // 初始化合併儲存格物件
      jspreadsheetOptions.mergeCells = {};

      worksheet['!merges'].forEach((range: XLSX.Range) => {
        const startRow = range.s.r;
        const startColumn = range.s.c;
        const endRow = range.e.r;
        const endColumn = range.e.c;

        // 如果合併儲存格超過取資料的列數上限就不處理
        if (startRow >= maxRow || endRow >= maxRow) {
          return;
        }

        // 取出合併欄位最左上方的儲存格名稱，例如：A1
        const cellName = this.getCellName([startColumn, startRow]);
        // 這裡設定是從哪個儲存格開始，跨幾個 Column 和 跨幾個 Row
        jspreadsheetOptions.mergeCells![cellName] = [
          endColumn - startColumn + 1,
          endRow - startRow + 1,
        ];
      });
    }
  }

  /**
   * JSpreadsheetOptions data 及 style 設定
   *
   * @param worksheet 指定的工作表
   * @param jspreadsheetOptions JSpreadsheetOptions
   * @param maxRow 最多取幾列
   * @param maxColumn 最多取幾欄
   */
  private static setDataAndStyle(
    worksheet: XLSX.WorkSheet,
    jspreadsheetOptions: Jspreadsheet.JSpreadsheetOptions,
    maxRow: number,
    maxColumn: number
  ): void {
    if (worksheet['!ref']) {
      // 初始化資料物件
      jspreadsheetOptions.data = [];
      // 初始化 style
      jspreadsheetOptions.style = {};

      // 取得資料的範圍
      // 把 !ref 的值會像是 "A1:F3" 轉成 XLSX.Range
      const range: XLSX.Range = XLSX.utils.decode_range(worksheet['!ref']);
      // 從第一列開始依序讀取，最多只讀到 maxRow 列
      for (
        let rowIndex = range.s.r;
        rowIndex <= range.e.r && rowIndex < maxRow;
        rowIndex++
      ) {
        const rowData: Jspreadsheet.CellValue[] = [];
        for (
          let columnIndex = range.s.c;
          columnIndex <= range.e.c && columnIndex < maxColumn;
          columnIndex++
        ) {
          // 透過 index 轉換成 A1 格式的 cellName，例如：A1
          const cellName: string = XLSX.utils.encode_cell({
            r: rowIndex,
            c: columnIndex,
          });
          // 取得儲存格物件
          const cell: XLSX.CellObject = worksheet[cellName];
          if (!cell) {
            rowData.push('');
            continue;
          }

          // 如果有設定公式，要用公式塞值
          if (cell.f) {
            rowData.push('=' + cell.f);
          } else if (cell.w) {
            // cell.w 是儲存格顯示的值，通常會跟 cell.v 一樣，除非使用者自行修改了儲存格的格式或公式
            rowData.push(cell.w);
          } else if (cell.v) {
            // cell.v 是儲存格的值，因為 Jspreadsheet.CellValue 不能塞 Date，所以這裡直接把值 toString 再寫入
            rowData.push(cell.v.toString());
          } else {
            rowData.push('');
          }

          // 使用的套件只能取得儲存格的前景色，其他都還無法支援，所以只處理前景色
          jspreadsheetOptions.style[cellName] = '';
          if (cell.s && cell.s.fgColor) {
            jspreadsheetOptions.style[cellName] =
              'background-color: #' + cell.s.fgColor.rgb + ';';
          }

          if (cell.t === 'n') {
            jspreadsheetOptions.style[cellName] += ' text-align: right;';
          } else {
            jspreadsheetOptions.style[cellName] += ' text-align: left;';
          }
          jspreadsheetOptions.style[cellName] =
            jspreadsheetOptions.style[cellName].trim();
        }
        jspreadsheetOptions.data[rowIndex] = rowData;
      }
    }
  }

  /**
   * JSpreadsheetOptions rows 設定
   *
   * @param worksheet 指定的工作表
   * @param jspreadsheetOptions JSpreadsheetOptions
   * @param maxRow 最多取幾列
   */
  private static setRows(
    worksheet: XLSX.WorkSheet,
    jspreadsheetOptions: Jspreadsheet.JSpreadsheetOptions,
    maxRow: number
  ): void {
    if (worksheet['!ref'] && worksheet['!rows']) {
      // 初始化 Row 物件
      jspreadsheetOptions.rows = {};

      // 取得資料的範圍
      // 把 !ref 的值會像是 "A1:F3" 轉成 XLSX.Range
      const range: XLSX.Range = XLSX.utils.decode_range(worksheet['!ref']);
      const endRow = range.e.r;
      // 從第一列開始依序讀取，最多只讀到 maxRow 列
      for (let i = 0; i <= endRow && i < maxRow; i++) {
        const rowInfo = worksheet['!rows'][i];
        // 如果列的 rowInfo 不是 null 才做
        if (rowInfo && rowInfo.hpx) {
          const row: jspreadsheet.Row = {
            height: rowInfo.hpx + 'px',
          };
          jspreadsheetOptions.rows[i] = row;
        }
      }
    }
  }

  /**
   * JSpreadsheetOptions columns 設定
   *
   * @param worksheet 指定的工作表
   * @param jspreadsheetOptions JSpreadsheetOptions
   * @param maxColumn 最多取幾欄
   */
  private static setColumns(
    worksheet: XLSX.WorkSheet,
    jspreadsheetOptions: Jspreadsheet.JSpreadsheetOptions,
    maxColumn: number
  ): void {
    if (worksheet['!ref'] && worksheet['!cols']) {
      // 初始化 Row 物件
      jspreadsheetOptions.columns = [];

      // 取得資料的範圍
      // 把 !ref 的值會像是 "A1:F3" 轉成 XLSX.Range
      const range: XLSX.Range = XLSX.utils.decode_range(worksheet['!ref']);
      const endColumn = range.e.c;
      // 從第一欄開始依序讀取
      for (let i = 0; i <= endColumn && i < maxColumn; i++) {
        const colInfo = worksheet['!cols'][i];
        // 如果列的 colInfo 不是 null 才做，不用 wpx 是因為放到網頁上太寬，改用字符寬度去算
        if (colInfo && colInfo.wch) {
          jspreadsheetOptions.columns[i] = { width: colInfo.wch * 8 };
        }

        jspreadsheetOptions.columns[i] = {
          ...jspreadsheetOptions.columns[i],
          align: 'center',
        };
      }
    }
  }

  /**
   * 取得儲存格名稱 (EX: A5, C6...)
   * @param cellAddress 座標 ([columnIndex, rowIndex])
   * @returns 儲存格名稱 EX: A5, C6
   */
  static getCellName(
    cellAddress: [columnIndex: number, rowIndex: number]
  ): string {
    return Jspreadsheet.getColumnNameFromId(cellAddress);
  }

  /**
   * 取得儲存格座標 (EX: [0, 2])
   * @param cellName 儲存格名稱 (EX: A5, C6...)
   * @returns 儲存格座標 EX: [0, 2]
   */
  static getCellCoords(cellName: string): [number, number] {
    // 第二個參數 true 回傳的資料格式會是 [number, number]，false 會回傳 string: 'columnIndex - rowIndex'
    return Jspreadsheet.getIdFromColumnName(cellName, true) as [number, number];
  }
}

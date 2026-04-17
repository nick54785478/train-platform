export class TrainQueriedResource {
  /**
   * 火車 uuid
   */
  uuid?: string;
  /**
   * 車次
   */
  number!: number;
  /**
   * 車種
   */
  kind!: string;

  /**
   * 前端給予的 index ，用於 Inline Edit 操作
   */
  givenIndex!: number;

  /**
   * 停靠站
   */
  stops: StopQueriedResource[] = [];
}

export class StopQueriedResource {
  uuid!: string;
  /**
   * 停靠順序
   */
  seq!: number;
  /**
   * 站名
   */
  stopName!: string;
  /**
   * 停靠時間
   */
  stopTime!: string;
  /**
   * 是否刪除
   */
  deleteFlag!: string;
}

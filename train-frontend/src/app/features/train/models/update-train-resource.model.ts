export class UpdateTrainResource {
  uuid?: string;
  /**
   * 火車代號
   */
  trainNo?: number;

  /**
   * 火車種類
   */
  trainKind!: string;

  /**
   * 停靠站資訊
   */
  stops: UpdateStopResource[] = [];
}

export class UpdateStopResource {
  /**
   * uuid
   */
  uuid!: string;
  /**
   * 停站順序
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
}

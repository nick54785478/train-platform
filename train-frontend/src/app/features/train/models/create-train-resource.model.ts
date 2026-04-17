export class CreateTrainResource {
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
  stops: CreateStopResource[] = [];
}

export class CreateStopResource {
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

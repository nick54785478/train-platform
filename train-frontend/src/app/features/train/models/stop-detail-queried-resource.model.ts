export class StopDetailQueriedResource {
  /**
   * 火車 uuid
   */
  seq?: string;
  /**
   * 起站
   */
  fromStop!: string;
  /**
   * 迄站
   */
  toStop!: string;
  /**
   * 起站發車時間
   */
  arriveStartStopTime!: string;
  /**
   * 抵達迄站時間
   */
  fromStopTime!: string;
  /**
   * 票價
   */
  price!: string;
}

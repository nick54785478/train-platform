export class TrainInfoSelectedResource {
  trainUuid!: string; // 火車 UUID

  ticketUuid!: string; // 車票 UUID

  trainKind!: string; // 車種

  trainNo!: number; // 火車號次

  price!: string; // 價格

  seatNo!: string; // 座號

  takeDate!: string; // 乘車日期

  payByAccount?: string; // 付款方式是否透過 帳號扣款

  fromStop!: string; // 起站

  toStop!: string; // 迄站

  fromStopTime!: string; // 發車時間

  toStopTime!: string; // 抵達時間
}

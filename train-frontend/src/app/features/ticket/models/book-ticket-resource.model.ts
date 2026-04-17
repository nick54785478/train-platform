export class BookTicketResource {
  trainUuid!: string; // 火車 UUID

  ticketUuid!: string; // 車票 UUID

  trainNo!: number; // 火車號次

  price!: string; // 價格

  seatNo?: string; // 座號

  takeDate?: string; // 乘車日期

  payMethod?: string; // 付款方式是否透過 帳號扣款
}

export class TicketDetailResource {
  number?: number;

  kind?: string;

  from!: string; // 使用者帳號

  startTime!: string;

  to!: string;

  arriveTime!: string;

  takeDate!: string;

  carNo!: string;

  seatNo!: string;

  booked!: string;

  activeFlag!: string;
}

export class BookingQueriedResource {
  username!: string;

  bookedDatas: BookQueriedResource[] = [];
}

export class BookQueriedResource {
  code?: string;

  message?: string;

  number?: number; // uuid

  kind?: string;

  from!: string; // 使用者帳號

  startTime!: string;

  to!: string;

  arriveTime!: string;

  takeDate!: string;

  seatNo!: string;

  booked!: string;

  activeFlag!: string;
}

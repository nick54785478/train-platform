import { BaseResponse } from '../../../shared/models/base-response.model';
export class TicketQueriedResource {
  trainNo!: string; // 訂票 UUID

  fromStop!: string;

  toStop!: string;

  price!: string;

  code!: string;

  message!: string;
}

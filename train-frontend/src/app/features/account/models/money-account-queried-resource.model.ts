export class MoneyAccountQueriedResource {
  code?: string;

  message?: string;

  uuid?: string; // uuid

  name?: string;

  username!: string; // 使用者帳號

  email!: string;

  balance!: number; // 餘額
}

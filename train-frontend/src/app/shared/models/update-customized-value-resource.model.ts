import { Option } from './option.model';

export class UpdateCustomizedValueResource {
  username?: string;
  dataType!: string;
  type!: string;
  valueList!: string[]; // 值
}

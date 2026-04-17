import { Option } from './option.model';

export class CustomisationQueriedResource {
  username?: string; // 名稱
  value!: Option[]; // 值
}

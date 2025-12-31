export interface GlobalResponse<T> {
  message: string;
  status: any;
  code: string;
  error: string;
  errors: Array<string>;
  data: T;
  timestamp: string;
  page: Paging
}

export interface Paging {
  page: number;
  size: number;
  isFirst: boolean;
  isLast: boolean;
  totalElements: number;
  totalPages: number;
}

export enum MessageType {
  Success = 'Success',
  Error = 'Error',
  Warn = 'Warn',
  Other = 'Other'
}

export interface NotificationMessage {
  message: string;
  type: MessageType;
  timestamp: string;
}

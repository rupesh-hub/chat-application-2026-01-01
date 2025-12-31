export enum MessageType {
  Error = 'Error',
  Warn = 'Warn',
  Success = 'Success',
  Other = 'Other',
}

// Notification message type
export type NotificationMessage = {
  message: string;
  type: MessageType;
  timestamp: string;
}

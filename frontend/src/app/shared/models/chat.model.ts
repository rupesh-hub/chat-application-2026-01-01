export interface Conversation {
  id: string
  participantId: string
  participantName: string
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
  online: boolean
}

export interface Message {
  id: string
  conversationId: string
  senderId: string
  senderName: string
  content: string
  timestamp: string
  read: boolean
}

export interface WebSocketMessage {
  type: "SEND_MESSAGE" | "TYPING" | "USER_CONNECTED" | "USER_DISCONNECTED" | "READ_RECEIPT"
  conversationId?: string
  senderId?: string
  senderName?: string
  content?: string
  timestamp?: string
  userId?: string
  status?: string
}

export interface Notification {
  conversationId: string
  participantId: string
  message: string
  count: number
}

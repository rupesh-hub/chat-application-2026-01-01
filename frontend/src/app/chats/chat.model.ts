import {UserResponse} from '../users/user.model';

export interface ConversationResponse {
  id: number;
  name: string;
  avatar: string;
  type: "private" | "group";
  participant: UserResponse;
  lastMessage?: MessageResponse;
  messages?: MessageResponse[];
  createdAt: any;
  updatedAt: any;
  unreadCount: number;
}

export interface MessageResponse {
  id: number
  conversationId: number
  sender: UserResponse
  senderId: string
  content: string
  type: "text" | "image" | "file"
  isSeen: boolean
  seenBy: number[]
  createdAt: string
}

export interface MessageRequest {
  conversationId: string;
  message: string;
}


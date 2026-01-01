import {Injectable, OnDestroy} from "@angular/core";
import {BehaviorSubject, Subject} from "rxjs";
import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {API} from "../../constants";
import {ConversationResponse, MessageResponse} from "../../chats/chat.model";
import {StatusNotification} from '../models/user.model';

@Injectable({providedIn: "root"})
export class WebSocketService implements OnDestroy {
  public activeConversationId: number | null = null;
  private client?: Client;

  private connectionStatusSubject = new BehaviorSubject<"connecting" | "connected" | "disconnected">("disconnected");
  connectionStatus$ = this.connectionStatusSubject.asObservable();

  private privateMessageSubject = new Subject<MessageResponse>();
  privateMessages$ = this.privateMessageSubject.asObservable();

  private sentAckSubject = new Subject<MessageResponse>();
  messageSentAck$ = this.sentAckSubject.asObservable();

  private messageReadSubject = new Subject<{ conversationId: string, readerId: string }>();
  messageRead$ = this.messageReadSubject.asObservable();

  private unreadCountsSubject = new BehaviorSubject<Record<string, number>>({});
  unreadCounts$ = this.unreadCountsSubject.asObservable();

  private statusSubject = new Subject<StatusNotification>();
  public userStatus$ = this.statusSubject.asObservable();

  constructor() {
  }

  connect(token: string) {
    if (this.client?.active) return;
    this.connectionStatusSubject.next("connecting");

    this.client = new Client({
      webSocketFactory: () => new SockJS(API.WEBSOCKET_URL),
      connectHeaders: {Authorization: `Bearer ${token}`},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      this.connectionStatusSubject.next("connected");

      /*Participant status*/
      this.client!.subscribe('/user/queue/status', message => {
        const payload = JSON.parse(message.body);
        this.statusSubject.next(payload);
      });

      this.client!.publish({destination: "/app/get-partner-status", body: null});

      // 1. Listen for messages from others
      this.client!.subscribe("/user/queue/private-messages", msg => {
        const message: MessageResponse = JSON.parse(msg.body);
        this.privateMessageSubject.next(message);
        if (this.activeConversationId !== message.conversationId) {
          this.incrementCount(message.conversationId);
        }
      });

      // 2. Listen for server ACK when YOU send a message
      this.client!.subscribe("/user/queue/message-sent", msg => {
        const message: MessageResponse = JSON.parse(msg.body);
        this.sentAckSubject.next(message);
      });

      /*unread message count*/
      this.client!.subscribe("/user/queue/unread-count", msg => {
        const data = JSON.parse(msg.body);
        const count = (this.activeConversationId?.toString() === data.conversationId.toString()) ? 0 : data.unreadCount;
        this.updateBadgeMap(data.conversationId, count);
      });


      this.client!.subscribe("/user/queue/messages-read", msg => {
        this.messageReadSubject.next(JSON.parse(msg.body));
      });
    };

    this.client.activate();
  }

  public setInitialUnreadCounts(conversations: ConversationResponse[]): void {
    const counts: Record<string, number> = {};
    conversations.forEach(conv => {
      counts[conv.id.toString()] = (this.activeConversationId === conv.id) ? 0 : (conv.unreadCount || 0);
    });
    this.unreadCountsSubject.next(counts);
  }

  private incrementCount(conversationId: number | string) {
    const current = this.unreadCountsSubject.getValue();
    const idStr = conversationId.toString();
    const newCount = (current[idStr] || 0) + 1;
    this.unreadCountsSubject.next({...current, [idStr]: newCount});
  }

  public resetUnreadCount(conversationId: string | number): void {
    this.updateBadgeMap(conversationId, 0);
  }

  private updateBadgeMap(id: any, count: number) {
    const current = this.unreadCountsSubject.getValue();
    this.unreadCountsSubject.next({...current, [id.toString()]: count});
  }

  sendMessage(payload: { conversationId: number; content: string }) {
    if (this.client?.connected) {
      this.client.publish({destination: "/app/chat.sendMessage", body: JSON.stringify(payload)});
    }
  }

  sendReadReceipt(conversationId: number | string) {
    if (this.client?.connected) {
      this.client.publish({
        destination: "/app/chat.markRead",
        body: JSON.stringify({conversationId: conversationId.toString()})
      });
    }
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = undefined;
      this.connectionStatusSubject.next("disconnected");
    }
  }

  ngOnDestroy() {
    this.disconnect();
  }
}

import {inject, Injectable, OnDestroy} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of, Subject} from 'rxjs';
import {catchError, map, tap, takeUntil} from 'rxjs/operators';
import {MessageResponse} from './chat.model';
import {GlobalResponse} from '../core/core.model';
import {WebSocketService} from '../shared/services/websocket.service';
import {ChatsService} from './chats.service';

@Injectable({providedIn: 'root'})
export class MessageService implements OnDestroy {
  private http = inject(HttpClient);
  private wsService = inject(WebSocketService);
  private chatsService = inject(ChatsService);

  private messagesSubject = new BehaviorSubject<MessageResponse[]>([]);
  public messages$ = this.messagesSubject.asObservable();

  private activeConversationId: number | null = null;
  private destroy$ = new Subject<void>();

  constructor() {
    // Handling incoming messages
    this.wsService.privateMessages$.pipe(takeUntil(this.destroy$)).subscribe(m => this.handleIncomingMessage(m));

    // Handling our own sent messages
    this.wsService.messageSentAck$.pipe(takeUntil(this.destroy$)).subscribe(m => this.handleIncomingMessage(m));

    // Handling the "Seen" status (Double Checkmarks)
    this.wsService.messageRead$.pipe(takeUntil(this.destroy$)).subscribe(receipt => {
      if (this.activeConversationId?.toString() === receipt.conversationId.toString()) {
        this.markMessagesAsSeenLocally();
      }
    });
  }

  public setActiveConversation(id: number | null) {
    this.activeConversationId = id;
    this.wsService.activeConversationId = id; // Sync with WS Service
  }

  public handleIncomingMessage(message: MessageResponse): void {
    const currentMessages = this.messagesSubject.getValue();

    if (this.activeConversationId === message.conversationId) {
      // 1. Add message to the current window
      this.messagesSubject.next([...currentMessages, message]);

      // 2. 🔥 AUTOMATIC MARK AS READ
      // Since the user has this chat open, notify the backend immediately
      this.wsService.sendReadReceipt(message.conversationId);
      this.wsService.resetUnreadCount(message.conversationId);
    } else {
      // Background chat logic
      //this.chatsService.incrementUnreadCount(message.conversationId);
    }
    this.chatsService.updateLastMessage(message.conversationId, message.content);
  }

  public fetchMessages(conversationId: string): Observable<MessageResponse[]> {
    return this.http.get<GlobalResponse<MessageResponse[]>>(`${'/messages'}/conversation/${conversationId}`)
      .pipe(
        map(res => (res.data ?? []).reverse()),
        tap(messages => this.messagesSubject.next(messages)),
        catchError(() => { this.messagesSubject.next([]); return of([]); })
      );
  }

  public markMessagesAsSeenLocally(): void {
    const updated = this.messagesSubject.getValue().map(m => ({...m, isSeen: true}));
    this.messagesSubject.next(updated);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

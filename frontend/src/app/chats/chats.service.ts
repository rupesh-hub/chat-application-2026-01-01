import {BehaviorSubject, catchError, type Observable, of, throwError} from "rxjs";
import {inject, Injectable} from "@angular/core";
import type {ConversationResponse, MessageResponse} from "./chat.model";
import {HttpClient} from '@angular/common/http';
import {map, tap} from 'rxjs/operators';
import {GlobalResponse} from '../core/core.model';
import {WebSocketService} from '../shared/services/websocket.service';
import {StatusNotification} from '../shared/models/user.model';

@Injectable({providedIn: "root"})
export class ChatsService {
  private conversationsSubject = new BehaviorSubject<ConversationResponse[]>([]);
  public conversations$ = this.conversationsSubject.asObservable();

  private selectedConversationSubject = new BehaviorSubject<number | null>(null);
  public selectedConversation$ = this.selectedConversationSubject.asObservable();

  private http: HttpClient = inject(HttpClient);
  private webSocketService: WebSocketService = inject(WebSocketService);
  private API_PATH = "/conversations";

  constructor() {
    this.loadConversations();
  }

  public loadConversations(): void {
    this.http.get<GlobalResponse<ConversationResponse[]>>(`${this.API_PATH}`).pipe(
      map(response => response.data),
      tap(conversations => {
        this.conversationsSubject.next(conversations);
        this.webSocketService.setInitialUnreadCounts(conversations);
      }),
      catchError(error => {
        console.error('Conversations load error: ', error);
        return of([]);
      })
    ).subscribe();
  }

  // 🔥 CRITICAL FIX: Pushes the message into local state so UI updates for sender/receiver
  public updateLocalConversationState(message: MessageResponse): void {
    const conversations = this.conversationsSubject.getValue();
    const updated = conversations.map(conv => {
      if (conv.id === message.conversationId) {
        const messages = conv.messages || [];
        // Prevent duplicate if socket sends same message twice
        const exists = messages.some(m => m.id === message.id);
        return {
          ...conv,
          lastMessage: message,
          messages: exists ? messages : [...messages, message]
        };
      }
      return conv;
    });
    this.conversationsSubject.next(updated);
  }

  public getConversations(): Observable<ConversationResponse[]> {
    return this.conversations$;
  }

  public searchConversation(query: string): void {
    this.http.get<GlobalResponse<ConversationResponse[]>>(`${this.API_PATH}?query=${query}`).pipe(
      map(response => response.data),
      tap(conversations => {
        this.conversationsSubject.next(conversations);
        this.webSocketService.setInitialUnreadCounts(conversations);
      })
    ).subscribe();
  }

  public getOrCreateConversation(participant: string): Observable<ConversationResponse> {
    return this.http.post<GlobalResponse<ConversationResponse>>(`${this.API_PATH}/with/${participant}`, {})
      .pipe(
        map(response => response.data),
        tap(conversation => {
          this.loadConversations();
          this.selectConversation(conversation.id);
        }),
        catchError(error => throwError(() => error))
      );
  }

  public selectConversation(conversationId: number): void {
    this.selectedConversationSubject.next(conversationId);
    this.webSocketService.activeConversationId = conversationId;
    this.webSocketService.resetUnreadCount(conversationId);
  }

  public updateLastMessage(conversationId: number, lastMessage: any): void {
    const conversations = this.conversationsSubject.getValue();
    const updatedConversations = conversations.map(conv => {
      if (conv.id === conversationId) {
        return {...conv, lastMessage: lastMessage};
      }
      return conv;
    });
    this.conversationsSubject.next(updatedConversations);
  }

  getSelectedConversation(): Observable<number | null> {
    return this.selectedConversation$;
  }

  resetUnreadCount(id: number) {
    this.webSocketService.resetUnreadCount(id);
  }

  public updatePartnerStatus(notification: StatusNotification): void {
    const currentConversations = this.conversationsSubject.value;
    const updated: ConversationResponse[] = currentConversations.map((conv: ConversationResponse) => {
      // 1. Check if this conversation has the participant mentioned in the notification
      if (conv.participant && conv.participant.email === notification.userId) {
        // 2. Return a deep copy with the updated status
        return {
          ...conv,
          participant: {
            ...conv.participant,
            status: notification.status.toLowerCase() // Ensure it matches 'online' | 'offline'
          }
        } as ConversationResponse; // Explicitly cast to satisfy the compiler
      }
      // 3. Return original if no change
      return conv;
    });

    this.conversationsSubject.next(updated);
  }

}

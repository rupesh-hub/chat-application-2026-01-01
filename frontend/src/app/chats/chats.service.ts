import {BehaviorSubject, catchError, combineLatest, Observable, of, throwError} from "rxjs";
import {inject, Injectable} from "@angular/core";
import type {ConversationResponse, MessageResponse} from "./chat.model";
import {HttpClient} from '@angular/common/http';
import {map, tap} from 'rxjs/operators';
import {GlobalResponse} from '../core/core.model';
import {WebSocketService} from '../shared/services/websocket.service';
import {StatusNotification} from '../shared/models/user.model';

@Injectable({providedIn: "root"})
export class ChatsService {
  private rawConversationsSubject = new BehaviorSubject<ConversationResponse[]>([]);
  private partnerStatusMap = new BehaviorSubject<Map<string, string>>(new Map());

  // Use map logic with explicit type casting for the status union
  public conversations$: Observable<ConversationResponse[]> = combineLatest([
    this.rawConversationsSubject,
    this.partnerStatusMap
  ]).pipe(
    map(([conversations, statusMap]) => {
      return conversations.map(conv => {
        const liveStatus = statusMap.get(conv.participant?.email);
        if (liveStatus && conv.participant) {
          return {
            ...conv,
            participant: {
              ...conv.participant,
              // Type cast 'string' to the specific union type allowed in UserResponse
              status: liveStatus.toLowerCase() as "online" | "offline" | "typing"
            }
          };
        }
        return conv;
      });
    })
  );

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
        this.rawConversationsSubject.next(conversations);
        this.webSocketService.setInitialUnreadCounts(conversations);
      }),
      catchError(error => {
        console.error('Conversations load error: ', error);
        return of([]);
      })
    ).subscribe();
  }

  public updatePartnerStatus(notification: StatusNotification): void {
    const currentMap = this.partnerStatusMap.value;
    currentMap.set(notification.userId, notification.status);
    this.partnerStatusMap.next(new Map(currentMap));
  }

  public updateLocalConversationState(message: MessageResponse): void {
    const conversations = this.rawConversationsSubject.getValue();
    const updated = conversations.map(conv => {
      if (conv.id === message.conversationId) {
        const messages = conv.messages || [];
        const exists = messages.some(m => m.id === message.id);
        return {
          ...conv,
          lastMessage: message,
          messages: exists ? messages : [...messages, message]
        };
      }
      return conv;
    });
    this.rawConversationsSubject.next(updated);
  }

  public updateLastMessage(conversationId: number, lastMessage: any): void {
    const conversations = this.rawConversationsSubject.getValue();
    const updatedConversations = conversations.map(conv => {
      if (conv.id === conversationId) {
        return {...conv, lastMessage: lastMessage};
      }
      return conv;
    });
    this.rawConversationsSubject.next(updatedConversations);
  }

  public getConversations(): Observable<ConversationResponse[]> {
    return this.conversations$;
  }

  public searchConversation(query: string): void {
    this.http.get<GlobalResponse<ConversationResponse[]>>(`${this.API_PATH}?query=${query}`).pipe(
      map(response => response.data),
      tap(conversations => {
        this.rawConversationsSubject.next(conversations);
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

  public getSelectedConversation(): Observable<number | null> {
    return this.selectedConversation$;
  }

  public resetUnreadCount(id: number): void {
    this.webSocketService.resetUnreadCount(id);
  }
}

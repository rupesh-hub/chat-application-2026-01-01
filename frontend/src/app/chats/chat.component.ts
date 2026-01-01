import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  type OnDestroy,
  type OnInit,
  ViewChild
} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ChatsService} from "./chats.service";
import {ConversationResponse, MessageResponse} from "./chat.model";
import {ConversationUsersComponent} from "./conversation-users.component";
import {combineLatest, debounceTime, distinctUntilChanged, type Observable, Subject, takeUntil} from "rxjs";
import {UserResponse} from "../users/user.model";
import {UsersService} from "../users/users.service";
import {MessageService} from './message.service';
import {AuthService} from '../core/auth/auth.service';
import {WebSocketService} from '../shared/services/websocket.service';
import {map} from 'rxjs/operators';
import {StatusNotification} from '../shared/models/user.model';
import {NotificationService} from '../shared/services/notification.service';
import {HttpErrorResponse} from '@angular/common/http';
import {MessageType} from '../shared/component/notification.model';
import {NotificationComponent} from '../shared/component/notification.component';

@Component({
  selector: "chat-chat",
  standalone: true,
  imports: [CommonModule, FormsModule, ConversationUsersComponent, NotificationComponent],
  templateUrl: "./chat.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [
    `
      .whatsapp-bg {
        background-color: #e5ddd5;
        background-image: url("https://www.transparenttextures.com/patterns/cubes.png");
        background-repeat: repeat;
      }

      .custom-scrollbar::-webkit-scrollbar {
        width: 5px;
      }

      .custom-scrollbar::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.1);
        border-radius: 10px;
      }

      .status-indicator {
        transition: all 0.3s ease-in-out;
      }
    `
  ]
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  protected websocketService = inject(WebSocketService);
  private messageService = inject(MessageService);
  private chatsService = inject(ChatsService);
  private usersService = inject(UsersService);
  protected authService = inject(AuthService);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  protected conversations$!: Observable<ConversationResponse[]>;
  protected users$!: Observable<UserResponse[]>;
  protected selectedConversationId$!: Observable<number | null>;
  protected messages$ = this.messageService.messages$;
  protected unreadCounts$ = this.websocketService.unreadCounts$;
  protected authenticatedUser$ = this.authService.authenticatedUser$;

  // This stream makes the Header and Sidebar status LIVE
  protected activeConversation$!: Observable<ConversationResponse | null>;

  protected selectedConversationId: number | null = null;
  protected newMessage = "";
  protected query = "";
  protected userQuery = "";
  protected isNewConversationOpen = false;
  protected isSettingsOpen = false;
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();
  protected _notificationService: NotificationService = inject(NotificationService);

  ngOnInit(): void {
    this.conversations$ = this.chatsService.getConversations();
    this.selectedConversationId$ = this.chatsService.getSelectedConversation();

    // Combine list and selection into a single reactive stream
    this.activeConversation$ = combineLatest([
      this.conversations$,
      this.selectedConversationId$
    ]).pipe(
      map(([conversations, id]) => {
        return conversations.find(c => c.id === id) ?? null;
      })
    );

    this.selectedConversationId$.pipe(takeUntil(this.destroy$)).subscribe((id) => {
      this.selectedConversationId = id;
      this.messageService.setActiveConversation(id);
      if (id) {
        this.messageService.fetchMessages(id.toString()).subscribe();
        this.websocketService.sendReadReceipt(id);
        this.websocketService.resetUnreadCount(id);
      }
    });

    // Subscriptions for real-time socket events
    this.websocketService.userStatus$.pipe(takeUntil(this.destroy$)).subscribe((notification: StatusNotification) => {
      this.chatsService.updatePartnerStatus(notification);
    });

    this.websocketService.messageSentAck$.pipe(takeUntil(this.destroy$)).subscribe(msg => {
      this.chatsService.updateLocalConversationState(msg);
    });

    this.websocketService.privateMessages$.pipe(takeUntil(this.destroy$)).subscribe(msg => {
      this.chatsService.updateLocalConversationState(msg);
    });

    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(q => this.chatsService.searchConversation(q));
  }

  onMessage(): void {
    if (!this.selectedConversationId || !this.newMessage.trim()) return;
    this.websocketService.sendMessage({
      conversationId: this.selectedConversationId,
      content: this.newMessage
    });
    this.newMessage = "";
  }

  onLogout(): void {
    this.isSettingsOpen = false;
    this.authService.logout();
  }

  onConversationSelected(conversation: ConversationResponse): void {
    this.chatsService.selectConversation(conversation.id);
  }

  searchUsers(): void {
    this.users$ = this.usersService.searchUsers(this.userQuery);
  }

  search(): void {
    this.searchSubject.next(this.query);
  }

  openNewConversation(): void {
    this.isNewConversationOpen = true;
  }

  closeNewConversation(): void {
    this.isNewConversationOpen = false;
    this.userQuery = "";
  }

  startConversation(participant: string): void {
    this.chatsService.getOrCreateConversation(participant).subscribe(
      {
        error: (error: HttpErrorResponse) => {
          this._notificationService.publish({
            message: error.error?.message || 'Something went wrong. Please try again.',
            timestamp: new Date().toString(),
            type: MessageType.Error
          });
        }
      }
    );
    this.closeNewConversation();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    if (this.scrollContainer && this.scrollContainer.nativeElement) {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.messageService.setActiveConversation(null);
  }

  trackByMessageId(_: number, message: MessageResponse) {
    return message.id;
  }

  trackByUserId(_: number, user: UserResponse) {
    return user.id;
  }
}

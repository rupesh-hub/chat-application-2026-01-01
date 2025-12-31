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
import {debounceTime, distinctUntilChanged, type Observable, Subject, takeUntil} from "rxjs";
import {UserResponse} from "../users/user.model";
import {UsersService} from "../users/users.service";
import {MessageService} from './message.service';
import {AuthService} from '../core/auth/auth.service';
import {WebSocketService} from '../shared/services/websocket.service';

@Component({
  selector: "chat-chat",
  standalone: true,
  imports: [CommonModule, FormsModule, ConversationUsersComponent],
  templateUrl: "./chat.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
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

  protected selectedConversationId: number | null = null;
  protected newMessage = "";
  protected query = "";
  protected userQuery = "";
  protected isNewConversationOpen = false;
  protected isSettingsOpen = false; // New Settings Flag
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.conversations$ = this.chatsService.getConversations();
    this.selectedConversationId$ = this.chatsService.getSelectedConversation();

    this.selectedConversationId$.pipe(takeUntil(this.destroy$)).subscribe((id) => {
      this.selectedConversationId = id;
      this.messageService.setActiveConversation(id);
      if (id) {
        this.messageService.fetchMessages(id.toString()).subscribe();
        this.websocketService.sendReadReceipt(id);
        this.websocketService.resetUnreadCount(id);
      }
    });

    // Handle real-time updates for Sender UI
    this.websocketService.messageSentAck$.pipe(takeUntil(this.destroy$)).subscribe(msg => {
      this.chatsService.updateLocalConversationState(msg);
    });

    // Handle incoming messages from others
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

  getSelectedConversation(conversations: ConversationResponse[]): ConversationResponse | null {
    return conversations.find((c) => c.id === this.selectedConversationId) ?? null;
  }

  getOtherParticipant(conversation: ConversationResponse): UserResponse | undefined {
    return conversation.participant;
  }

  searchUsers(): void {
    this.users$ = this.usersService.searchUsers(this.userQuery);
  }

  search(): void {
    this.searchSubject.next(this.query);
  }

  openNewConversation(): void { this.isNewConversationOpen = true; }
  closeNewConversation(): void { this.isNewConversationOpen = false; this.userQuery = ""; }

  startConversation(participant: string): void {
    this.chatsService.getOrCreateConversation(participant).subscribe();
    this.closeNewConversation();
  }

  ngAfterViewChecked(): void { this.scrollToBottom(); }

  private scrollToBottom(): void {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.messageService.setActiveConversation(null);
  }

  trackByMessageId(_: number, message: MessageResponse) { return message.id; }
  trackByUserId(_: number, user: UserResponse) { return user.id; }
}

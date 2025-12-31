import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from "@angular/core"
import {CommonModule} from "@angular/common"
import {ConversationResponse} from "./chat.model"
import {Observable} from "rxjs"
import {UserResponse} from "../users/user.model"

@Component({
  selector: "chat-conversation-users",
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex-1 overflow-y-auto space-y-2 px-3 py-2">
      <ng-container *ngIf="conversations$ | async as conversations">
        <ng-container *ngIf="unreadCounts$ | async as counts">
          <div
            class="group flex items-center gap-3 px-4 py-3 rounded-xl cursor-pointer transition-all duration-200"
            *ngFor="let conversation of conversations; trackBy: trackById"
            [ngClass]="{
              'bg-gradient-to-r from-indigo-50 to-indigo-100/50 border-l-4 border-indigo-600 shadow-sm': selected === conversation.id,
              'hover:bg-slate-100': selected !== conversation.id,
              'bg-indigo-50': (counts[conversation.id] > 0) && selected !== conversation.id
            }"
            (click)="selectConversation(conversation)">

            <div class="relative shrink-0">
              <img
                class="w-11 h-11 rounded-xl object-cover border-2 border-slate-200 group-hover:border-indigo-300 transition-colors"
                [src]="getAvatar(conversation) ?? ''"
                [alt]="conversation.name">

              <span
                *ngIf="getOtherParticipant(conversation)?.status === 'online'"
                class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 bg-green-500 border-2 border-white rounded-full shadow-sm">
              </span>
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-baseline">
                <p class="text-sm font-bold truncate tracking-tight group-hover:text-indigo-700 transition-colors">
                  {{ conversation.name }}
                </p>
              </div>

              <p
                class="truncate text-xs"
                [ngClass]="{
                    'font-bold text-indigo-900 opacity-100': (counts[conversation.id] > 0),
                    'font-normal text-slate-500 opacity-75': !(counts[conversation.id] > 0)
                  }"
              >
                {{ getLastMessageContent(conversation.lastMessage) }}
              </p>
            </div>

            @if (counts[conversation.id] > 0) {
              <span
                class="text-[11px] font-bold h-5 w-5 bg-red-600 text-white flex justify-center items-center rounded-full">
                {{ counts[conversation.id] }}
              </span>
            }

          </div>
        </ng-container>
      </ng-container>
    </div>
  `,
})
export class ConversationUsersComponent {
  @Input("conversations") conversations$!: Observable<ConversationResponse[]>
  @Input() selectedConversationId: number | null = null
  @Output() conversationSelected = new EventEmitter<ConversationResponse>();
  @Input("unreadMessageCount") unreadCounts$!: Observable<any>;

  get selected(): number | null {
    return this.selectedConversationId
  }

  getLastMessageContent(lastMessage: any): string {
    if (!lastMessage) return 'No messages yet';
    return typeof lastMessage === 'string' ? lastMessage : lastMessage.content;
  }

  getOtherParticipant(conversation: ConversationResponse): UserResponse | undefined {
    return conversation.participant;
  }

  getAvatar(conversation: ConversationResponse): string | null {
    return conversation.avatar || this.getOtherParticipant(conversation)?.profile || null;
  }

  selectConversation(conversation: ConversationResponse): void {
    this.conversationSelected.emit(conversation)
  }

  trackById(_: number, conversation: ConversationResponse): number {
    return conversation.id
  }
}

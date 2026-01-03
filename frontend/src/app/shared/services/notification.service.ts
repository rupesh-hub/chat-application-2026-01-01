import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {NotificationMessage} from '../component/notification.model';

@Injectable({providedIn: 'root'})
export class NotificationService {
  private messageSubject = new Subject<NotificationMessage | null>();
  public message$ = this.messageSubject.asObservable();
  private timeoutId: any;

  public publish(message: NotificationMessage): void {
    this.messageSubject.next(message);

    if (this.timeoutId) clearTimeout(this.timeoutId);

    this.timeoutId = setTimeout(() => {
      this.clearMessage();
    }, 5000);
  }

  public clearMessage(): void {
    this.messageSubject.next(null);
  }
}

import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {NotificationMessage} from '../component/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private messageSubject: BehaviorSubject<NotificationMessage> = new BehaviorSubject<NotificationMessage>(null);
  public message$: Observable<NotificationMessage> = this.messageSubject.asObservable();

  private timeoutId: any; // To store the timeout ID for clearing the message

  /**
   * Publish a new message to subscribers
   * @param message The message to be sent to the subscribers
   */
  public publish(message: NotificationMessage): void {
    this.messageSubject.next(message);
    if (this.timeoutId) clearTimeout(this.timeoutId);
    this.timeoutId = setTimeout(() => {
      this.clearMessage();
    }, 5000);
  }

  /**
   * Clears the current message
   */
  private clearMessage(): void {
    this.messageSubject.next(null);
  }
}

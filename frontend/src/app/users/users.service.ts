import {inject, Injectable} from "@angular/core"
import {catchError, type Observable, of} from "rxjs"
import type {UserResponse} from "./user.model"
import {HttpClient} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {GlobalResponse} from '../core/core.model';

@Injectable({
  providedIn: "root",
})
export class UsersService {
  private http: HttpClient = inject(HttpClient);

  public searchUsers(query: string): Observable<UserResponse[]> {
    if (!query.trim()) return of([]);

    const q = query.toLowerCase();
    const path = `/users?query=${q}`;

    return this.http.get<GlobalResponse<UserResponse[]>>(path).pipe(
      map((response: GlobalResponse<UserResponse[]>) => response.data),
      catchError(error => {
        console.error('Search users failed', error);
        return of([]); // 👈 must return an Observable
      })
    );
  }

}

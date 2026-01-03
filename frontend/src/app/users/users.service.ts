import {inject, Injectable} from "@angular/core";
import {HttpClient} from '@angular/common/http';
import {catchError, type Observable, of, throwError} from "rxjs";
import {map} from 'rxjs/operators';
import {UserResponse, UserUpdateRequest} from "./user.model";
import {GlobalResponse} from '../core/core.model';

@Injectable({
  providedIn: "root",
})
export class UsersService {
  private http: HttpClient = inject(HttpClient);
  private BASE_PATH: string = '/users';

  public searchUsers(query: string): Observable<UserResponse[]> {
    if (!query.trim()) return of([]);
    const q = query.toLowerCase();
    const path = `${this.BASE_PATH}?query=${q}`;

    return this.http.get<GlobalResponse<UserResponse[]>>(path).pipe(
      map((response: GlobalResponse<UserResponse[]>) => response.data),
      catchError(error => {
        console.error('Search users failed', error);
        return of([]);
      })
    );
  }

  public changePassword = (request: any): Observable<any> => {
    return this.http.post<GlobalResponse<any>>(`${this.BASE_PATH}/change-password`, request)
      .pipe(
        map((response: GlobalResponse<any>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

  public uploadProfilePicture(formData: FormData): Observable<any> {
    return this.http.put<GlobalResponse<any>>(`${this.BASE_PATH}/change-profile`, formData)
      .pipe(
        map((response: GlobalResponse<any>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

  public profile = (): Observable<UserResponse> => {
    return this.http.get<GlobalResponse<any>>(`${this.BASE_PATH}/current`)
      .pipe(
        map((response: GlobalResponse<any>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

  public updateProfile(user: UserUpdateRequest): Observable<UserResponse> {
    return this.http.put<GlobalResponse<UserResponse>>(`${this.BASE_PATH}`, user)
      .pipe(
        map((response: GlobalResponse<UserResponse>) => response.data),
        catchError(error => throwError(() => error))
      );
  }
}

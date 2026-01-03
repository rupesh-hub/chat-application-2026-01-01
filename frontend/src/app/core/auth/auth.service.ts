import {inject, Injectable} from "@angular/core";
import {HttpClient, type HttpErrorResponse} from "@angular/common/http";
import {BehaviorSubject, catchError, map, type Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {TokenService} from "./token.service";
import type {AuthenticationRequest, AuthenticationResponse, GlobalResponse} from "./auth.model";
import {WebSocketService} from "../../shared/services/websocket.service";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private BASE_URL = "/authentication";
  private http: HttpClient = inject(HttpClient);
  private router: Router = inject(Router);
  private tokenService: TokenService = inject(TokenService);
  private webSocketService: WebSocketService = inject(WebSocketService);

  // Initialize with null first, then fill immediately in constructor
  private authenticatedUserSubject = new BehaviorSubject<AuthenticationResponse | null>(null);
  public authenticatedUser$ = this.authenticatedUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor() {
    this.initializeState();
  }

  private initializeState(): void {
    const token = localStorage.getItem("access_token");
    if (token && this.tokenService.isTokenValid(token)) {
      const user = this.decodeUserFromToken(token);
      if (user) {
        this.authenticatedUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        this.webSocketService.connect(token);
      }
    } else if (token) {
      this.logout();
    }
  }

  private decodeUserFromToken(token: string): AuthenticationResponse | null {
    const decoded = this.tokenService.decode(token);
    if (!decoded) return null;
    const user = decoded.user;
    return {
      access_token: token,
      email: user?.email || decoded?.sub,
      name: user?.name || "User",
      roles: user?.roles || decoded?.authorities,
      profile: user?.profile || "assets/images/default-avatar.png",
    };
  }

  public login(request: AuthenticationRequest): Observable<boolean> {
    return this.http.post<GlobalResponse<AuthenticationResponse>>(this.BASE_URL, request).pipe(
      map((response) => {
        const authResponse = response.data;
        localStorage.setItem("access_token", authResponse.access_token);

        this.isAuthenticatedSubject.next(true);
        this.authenticatedUserSubject.next(authResponse);
        this.webSocketService.connect(authResponse.access_token);

        this.router.navigate(["/chats"]);
        return true;
      }),
      catchError((error: HttpErrorResponse) => throwError(() => error?.error))
    );
  }

  public register(request: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/register`, request).pipe(
      map((response: GlobalResponse<any>) => {
        this.router.navigate(['/auth/confirm-email', response?.data]);
        return response;
      }),
      catchError((error: HttpErrorResponse) => throwError(() => error))
    );
  }

  public confirmEmail(token: string, email: string): Observable<string> {
    return this.http.get<GlobalResponse<string>>(`${this.BASE_URL}/confirm-email?token=${token}&email=${email}`).pipe(
      map((response: GlobalResponse<string>) => {
        this.router.navigate(["/auth/login"]);
        return response.data;
      }),
      catchError((error: HttpErrorResponse) => throwError(() => error))
    );
  }

  public existsByEmail(email: string): Observable<boolean> {
    return this.http.get<GlobalResponse<boolean>>(`${this.BASE_URL}/exists-by-email?email=${email}`).pipe(
      map((response) => response.data),
      catchError((error) => throwError(() => error))
    );
  }

  public logout(): void {
    localStorage.removeItem("access_token");
    this.isAuthenticatedSubject.next(false);
    this.authenticatedUserSubject.next(null);
    this.webSocketService.disconnect();
    this.router.navigate(["/auth/login"]);
  }

  public forgotPasswordRequest = (email): Observable<string> => {
    return this.http.get<GlobalResponse<string>>(`${this.BASE_URL}/forgot-password-request?email=${email}`)
      .pipe(
        map((response: GlobalResponse<string>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

  public resetPassword = (request: any): Observable<string> => {
    return this.http.post<GlobalResponse<string>>(`${this.BASE_URL}/reset-password`, request)
      .pipe(
        map((response: GlobalResponse<string>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

  public resendConfirmationToken = (email: string, token:string): Observable<string> => {
    return this.http.get<GlobalResponse<string>>(`${this.BASE_URL}/resend-confirmation-token?email=${email}&token=${token}`)
      .pipe(
        map((response: GlobalResponse<string>) => response.data),
        catchError((error) => throwError(() => error))
      );
  }

}

import {Injectable} from "@angular/core"

@Injectable({
  providedIn: "root",
})
export class TokenService {
  public isTokenValid(token: string): boolean {
    if (!token || typeof token !== "string") return false
    const parts = token.split(".")
    if (parts.length !== 3) return false
    const payload = parts[1]
    if (!payload) return false
    try {
      const decodedPayload = JSON.parse(atob(this.fixBase64Url(payload)))
      return !this.isTokenExpired(decodedPayload)
    } catch (e) {
      return false
    }
  }

  public decode(token: string): any | null {
    try {
      const payload = token.split(".")[1]
      return JSON.parse(atob(this.fixBase64Url(payload)))
    } catch (e) {
      return null
    }
  }

  private isTokenExpired(payload: any): boolean {
    if (!payload || !payload.exp) return true
    return (payload.exp * 1000) < Date.now()
  }

  private fixBase64Url(str: string): string {
    str = str.replace(/-/g, "+").replace(/_/g, "/")
    while (str.length % 4 !== 0) str += "="
    return str
  }
}

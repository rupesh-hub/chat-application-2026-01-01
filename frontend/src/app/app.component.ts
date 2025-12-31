import {Component} from "@angular/core"
import {RouterOutlet} from "@angular/router"

@Component({
  selector: "chat-root",
  imports: [RouterOutlet],
  standalone: true,
  template: `
    <router-outlet/>`,
})
export class AppComponent {
}

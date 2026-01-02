import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'chat-profile',
  imports: [CommonModule, FormsModule],
  standalone: true,
  templateUrl: './profile.component.html'
})
export class ProfileComponent {

  isEditing = false;

  user = {
    firstName: 'Alexandra',
    lastName: 'Sterling',
    email: 'alexandra.s@enterprise-corp.com', // Read-only
    profile: 'Senior System Architect',
    bio: 'Specializing in cloud infrastructure and scalable Angular architectures.',
    avatar: 'https://i.pravatar.cc/150?u=9'
  };

  saveChanges() {
    this.isEditing = false;
    // Handle API logic here
  }

}

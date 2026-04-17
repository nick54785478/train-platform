import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { CoreModule } from '../../../core/core.module';
import { Router } from '@angular/router';

@Component({
  selector: 'app-form-invalid',
  standalone: true,
  imports: [CommonModule, SharedModule, CoreModule],
  templateUrl: './form-invalid.component.html',
  styleUrl: './form-invalid.component.scss',
})
export class FormInvalidComponent {
  constructor(private router: Router) {}

  /**
   * 重導向回首頁
   */
  redirectHome() {
    this.router.navigate(['/home']);
  }
}

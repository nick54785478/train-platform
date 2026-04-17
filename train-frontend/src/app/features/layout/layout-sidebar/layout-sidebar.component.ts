import { Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { CommonModule } from '@angular/common';
import { MenuItem } from 'primeng/api';
import { LayoutService } from '../services/layout.service';
import { Subject } from 'rxjs/internal/Subject';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { Router } from '@angular/router';
import { lastValueFrom, map, of } from 'rxjs';
import { StorageService } from '../../../core/services/storage.service';

@Component({
  selector: 'app-layout-sidebar',
  standalone: true,
  imports: [CommonModule, SharedModule],
  templateUrl: './layout-sidebar.component.html',
  styleUrl: './layout-sidebar.component.scss',
  providers: [LayoutService, Router],
})
export class LayoutSidebarComponent implements OnInit, OnDestroy {
  private readonly _destroying$ = new Subject<void>();

  items: MenuItem[] = [];

  permissions: string[] = []; // 權限清單

  sidebarVisible: boolean = true; // 預設開啟

  constructor(
    public layoutService: LayoutService,
    private storageService: StorageService
  ) {}

  async ngOnInit() {
    // 初始化 側邊的超連結
    this.items = await lastValueFrom(
      this.layoutService.getPermissions().pipe(
        map((res) => {
          console.log(res);
          res.forEach((item) => {
            let id = item.id ? item.id : '';
            if (this.permissions.includes(id)) {
              item.visible = true;
            }
          });
          return res;
        }),
        takeUntil(this._destroying$)
      )
    );
  }

  ngOnDestroy() {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }
}

import { Routes } from '@angular/router';
import { TrainComponent } from './pages/train/train.component';
import { CreateTimetableComponent } from './pages/create-timetable/create-timetable.component';
import { TrainStopsComponent } from './pages/train/train-stops/train-stops.component';
import { TrainMaintenanceComponent } from './pages/train-maintenance/train-maintenance.component';
import { TrainUploadComponent } from './pages/train-upload/train-upload.component';

/**
 * 定義 Users 子路由配置的檔案
 */
export const routes: Routes = [
  {
    path: '',
    component: TrainComponent,
  },
  {
    path: 'create',
    component: CreateTimetableComponent,
  },
  {
    path: 'stops',
    component: TrainStopsComponent,
  },
  {
    path: 'maintenance',
    component: TrainMaintenanceComponent,
  },
  {
    path: 'upload',
    component: TrainUploadComponent,
  },
];

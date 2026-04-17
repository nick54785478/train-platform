import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainMaintenanceComponent } from './train-maintenance.component';

describe('TrainMaintenanceComponent', () => {
  let component: TrainMaintenanceComponent;
  let fixture: ComponentFixture<TrainMaintenanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainMaintenanceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainMaintenanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

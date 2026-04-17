import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainStopsComponent } from './train-stops.component';

describe('TrainStopsComponent', () => {
  let component: TrainStopsComponent;
  let fixture: ComponentFixture<TrainStopsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainStopsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainStopsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainSelectingComponent } from './train-selectng.component';

describe('TrainSelectingComponent', () => {
  let component: TrainSelectingComponent;
  let fixture: ComponentFixture<TrainSelectingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainSelectingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TrainSelectingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainDetailComponent } from './train-detail.component';

describe('TrainDetailComponent', () => {
  let component: TrainDetailComponent;
  let fixture: ComponentFixture<TrainDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

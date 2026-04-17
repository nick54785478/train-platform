import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainInfoFormComponent } from './train-info-form.component';

describe('TrainInfoFormComponent', () => {
  let component: TrainInfoFormComponent;
  let fixture: ComponentFixture<TrainInfoFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainInfoFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

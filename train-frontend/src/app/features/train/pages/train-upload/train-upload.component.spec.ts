import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainUploadComponent } from './train-upload.component';

describe('TrainUploadComponent', () => {
  let component: TrainUploadComponent;
  let fixture: ComponentFixture<TrainUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainUploadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

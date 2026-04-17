import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormInvalidComponent } from './form-invalid.component';

describe('FormInvalidComponent', () => {
  let component: FormInvalidComponent;
  let fixture: ComponentFixture<FormInvalidComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormInvalidComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormInvalidComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

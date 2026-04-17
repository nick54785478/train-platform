import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookedSuccessfullyComponent } from './booked-successfully.component';

describe('BookedSuccessfullyComponent', () => {
  let component: BookedSuccessfullyComponent;
  let fixture: ComponentFixture<BookedSuccessfullyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookedSuccessfullyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookedSuccessfullyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

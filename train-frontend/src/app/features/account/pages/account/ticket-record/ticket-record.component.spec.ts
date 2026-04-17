import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketRecordComponent } from './ticket-record.component';

describe('TicketRecordComponent', () => {
  let component: TicketRecordComponent;
  let fixture: ComponentFixture<TicketRecordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketRecordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

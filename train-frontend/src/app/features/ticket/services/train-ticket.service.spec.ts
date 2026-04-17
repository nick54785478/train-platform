import { TestBed } from '@angular/core/testing';

import { TrainTicketService } from './train-ticket.service';

describe('TrainTicketService', () => {
  let service: TrainTicketService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrainTicketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

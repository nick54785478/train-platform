import { TestBed } from '@angular/core/testing';

import { TrainSeatService } from './train-seat.service';

describe('TrainSeatService', () => {
  let service: TrainSeatService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrainSeatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

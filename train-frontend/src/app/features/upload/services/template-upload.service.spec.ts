import { TestBed } from '@angular/core/testing';

import { TemplateUploadService } from './template-upload.service';

describe('UploadTemplateService', () => {
  let service: TemplateUploadService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplateUploadService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

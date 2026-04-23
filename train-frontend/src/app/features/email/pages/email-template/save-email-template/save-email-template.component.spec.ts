import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SaveEmailTemplateComponent } from './save-email-template.component';

describe('SaveEmailTemplateComponent', () => {
  let component: SaveEmailTemplateComponent;
  let fixture: ComponentFixture<SaveEmailTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SaveEmailTemplateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SaveEmailTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

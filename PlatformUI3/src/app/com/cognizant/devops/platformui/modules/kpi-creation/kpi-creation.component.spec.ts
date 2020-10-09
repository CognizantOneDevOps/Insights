import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiCreationComponent } from './kpi-creation.component';

describe('KpiCreationComponent', () => {
  let component: KpiCreationComponent;
  let fixture: ComponentFixture<KpiCreationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KpiCreationComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

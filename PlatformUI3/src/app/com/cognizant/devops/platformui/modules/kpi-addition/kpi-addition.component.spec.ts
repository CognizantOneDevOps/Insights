import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiAdditionComponent } from './kpi-addition.component';

describe('KpiAdditionComponent', () => {
  let component: KpiAdditionComponent;
  let fixture: ComponentFixture<KpiAdditionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [KpiAdditionComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KpiAdditionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AutomlComponent } from './automl.component';

describe('AutomlComponent', () => {
  let component: AutomlComponent;
  let fixture: ComponentFixture<AutomlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AutomlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AutomlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

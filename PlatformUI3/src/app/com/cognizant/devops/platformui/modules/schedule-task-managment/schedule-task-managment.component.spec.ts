import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleTaskManagmentComponent } from './schedule-task-managment.component';

describe('ScheduleTaskManagmentComponent', () => {
  let component: ScheduleTaskManagmentComponent;
  let fixture: ComponentFixture<ScheduleTaskManagmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScheduleTaskManagmentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleTaskManagmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

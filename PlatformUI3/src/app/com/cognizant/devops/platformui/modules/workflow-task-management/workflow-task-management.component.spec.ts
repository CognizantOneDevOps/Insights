import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkflowTaskManagementComponent } from './workflow-task-management.component';

describe('WorkflowTaskManagementComponent', () => {
  let component: WorkflowTaskManagementComponent;
  let fixture: ComponentFixture<WorkflowTaskManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkflowTaskManagementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkflowTaskManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KafkaNeo4jListComponent } from './kafka-neo4j-list.component';

describe('KafkaNeo4jListComponent', () => {
  let component: KafkaNeo4jListComponent;
  let fixture: ComponentFixture<KafkaNeo4jListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KafkaNeo4jListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KafkaNeo4jListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

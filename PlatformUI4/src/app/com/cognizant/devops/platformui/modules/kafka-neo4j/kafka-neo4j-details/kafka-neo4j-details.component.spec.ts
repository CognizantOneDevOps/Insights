import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KafkaNeo4jDetailsComponent } from './kafka-neo4j-details.component';

describe('KafkaNeo4jDetailsComponent', () => {
  let component: KafkaNeo4jDetailsComponent;
  let fixture: ComponentFixture<KafkaNeo4jDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KafkaNeo4jDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KafkaNeo4jDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

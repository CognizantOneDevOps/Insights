import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KafkaNeo4jConfigurationComponent } from './kafka-neo4j-configuration.component';

describe('KafkaNeo4jConfigurationComponent', () => {
  let component: KafkaNeo4jConfigurationComponent;
  let fixture: ComponentFixture<KafkaNeo4jConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KafkaNeo4jConfigurationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KafkaNeo4jConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

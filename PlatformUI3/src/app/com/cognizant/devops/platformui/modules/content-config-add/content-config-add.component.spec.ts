import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ContentConfigAddition } from './content-config-add.component';

describe('ContentConfigAddition', () => {
  let component: ContentConfigAddition;
  let fixture: ComponentFixture<ContentConfigAddition>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ContentConfigAddition]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContentConfigAddition);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

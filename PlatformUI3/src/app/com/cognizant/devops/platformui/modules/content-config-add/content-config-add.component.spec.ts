import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ContentConfigAddition } from './content-config-add.component';

describe('ContentConfigAddition', () => {
  let component: ContentConfigAddition;
  let fixture: ComponentFixture<ContentConfigAddition>;

  beforeEach(waitForAsync(() => {
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

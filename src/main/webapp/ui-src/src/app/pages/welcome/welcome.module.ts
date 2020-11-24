import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WelcomeRoutingModule } from './welcome-routing.module';
import { NgZorroAntdModule } from 'ng-zorro-antd';
import { VgCoreModule } from 'videogular2/compiled/core';
import { VgControlsModule } from 'videogular2/compiled/controls';
import { VgOverlayPlayModule } from 'videogular2/compiled/overlay-play';
import { VgBufferingModule } from 'videogular2/compiled/buffering';
import { WelcomeComponent } from './welcome.component';
import { FileListComponent } from './file-list/file-list.component';
import { ContactsListComponent } from './contacts-list/contacts-list.component';

@NgModule({
  imports: [
    CommonModule,
    WelcomeRoutingModule,
    NgZorroAntdModule,
    VgCoreModule,
    VgControlsModule,
    VgOverlayPlayModule,
    VgBufferingModule
  ],
  declarations: [WelcomeComponent, FileListComponent, ContactsListComponent],
  exports: [WelcomeComponent]
})
export class WelcomeModule { }

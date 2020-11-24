import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WelcomeComponent } from './pages/welcome/welcome.component';
import { FileListComponent } from './pages/welcome/file-list/file-list.component';
import { ContactsListComponent } from './pages/welcome/contacts-list/contacts-list.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/welcome' },
  {
    path: 'welcome',
    component: WelcomeComponent,
    children: [
      {
        path: 'files',
        component: FileListComponent,
        pathMatch: 'full'
      },
      {
        path: 'contacts',
        component: ContactsListComponent,
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

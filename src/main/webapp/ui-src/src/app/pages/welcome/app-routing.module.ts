import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AddUserComponent} from "./../app/add-user/add-user.component";
import {UserListComponent} from "./../app/user-list/user-list.component";
import {EditUserComponent} from "./../app/edit-user/edit-user.component";



const routes: Routes = [
  { path: '', redirectTo: 'user', pathMatch: 'full' },
  { path: 'users', component: UserListComponent },
  { path: 'add', component: AddUserComponent },
  { path: 'update/:id', component: EditUserComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

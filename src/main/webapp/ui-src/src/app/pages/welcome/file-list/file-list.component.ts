import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { LocalStorageService } from '../../../services/local-storage.service';

@Component({
  selector: 'app-file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.css']
})
export class FileListComponent implements OnInit {


  fileList = [];
  lastModifiedStr = null;

  constructor(
    private http: HttpClient,
    private store: LocalStorageService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadFileList();
  }

  loadFileList() {
    this.http.get('api/file/collection').subscribe(ret => {
      if (ret['returnCode'] === 1) {
        this.fileList = ret['result'];
        if (this.fileList.length > 0) {
          this.lastModifiedStr = this.fileList[0].lastModifiedStr;
        }
      }
    });
  }

}

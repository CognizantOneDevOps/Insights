/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

import { Injectable } from '@angular/core';
import { DomSanitizer, BrowserModule, SafeUrl } from '@angular/platform-browser';
import { MatIconRegistry } from '@angular/material/icon';


export interface IImageHandlerService {
    getImagePath(imageKey: String): String;
    initializeImageIcons(): void;
    addPathIconRegistry(): void;
}

@Injectable()
export class ImageHandlerService implements IImageHandlerService {
    urlMapping = {};
    imageMap = new Map<String, String>();
    constructor(private iconRegistry: MatIconRegistry, private sanitizer: DomSanitizer) {

    }

    public initializeImageIcons() {
        this.addImage('defaultLogo', "icons/svg/landingPage/OneDevOps_InsightsLOGO.svg");
        this.addImage('InsightsLogo', "icons/svg/login/InsightsLogo.svg");
        this.addImage('CustomerLogo', "icons/svg/login/Customer_Logo.png");
        this.addImage('PoweredBy', "icons/svg/login/PoweredBy.svg");
        this.addImage('verticleLine', "icons/svg/login/Vertical_Line.svg");
        this.addImage('OrangeVerticalLine', "icons/svg/OrangeVerticalLine.svg");
        this.addImage('OrangeLine', "icons/svg/landingPage/OrangeLine.svg");
        this.addImage('user-icon', "icons/svg/login/user_icon.svg");
        this.addImage('user-icon-active', "icons/svg/login/user_icon_active.svg");
        this.addImage('password-icon', "icons/svg/login/password_icon.svg");
        this.addImage('password-icon-active', "icons/svg/login/password_icon_active.svg");
        this.addImage('favicon_icon', "icons/svg/IS.svg");
        this.addImage('success_status', "icons/svg/common/success_status.svg");
        this.addImage('inprogress', "icons/svg/common/inprogress.svg");
        this.addImage('failure_status', "icons/svg/common/failure_status.svg");
        this.addImage('error', "icons/svg/common/error.svg");
        this.addImage('alert', "icons/svg/common/alert.svg");
        this.addImage('clock', "icons/svg/common/clock.svg");
        this.addImage('healthcheck_show_details', "icons/svg/healthCheckPage/details.svg");
        this.addImage('healthcheck_back_to_top', "icons/svg/backToTop.svg");
        this.addImage('close_dialog', "icons/svg/common/close.svg");
        this.addImage('edit_icon', "icons/svg/actionIcons/Edit_icon_disabled.svg");
        this.addImage('start_icon', "icons/svg/actionIcons/Start_icon_Disabled.svg");
        this.addImage('stop', "icons/svg/agentManagement/stop.svg");
        this.addImage('successIconSrc', "icons/svg/ic_check_circle_24px.svg");
        this.addImage('ic_report_problem', "icons/svg/ic_report_problem_24px.svg");
        this.addImage('ic_delete_icon', "icons/svg/actionIcons/Delete_icon_disabled.svg");
        this.addImage('search_icon', "icons/svg/ic_search_24px.svg");
        this.addImage('addButton', "icons/svg/buttonIcon/Add.svg");
        this.addImage('startButton', "icons/svg/buttonIcon/sharp-play_circle.svg");
        this.addImage('stopButton', "icons/svg/buttonIcon/sharp-stop.svg");
        this.addImage('lineInButton', "icons/svg/buttonIcon/Line_menu.svg");
        this.addImage('warning', "./icons/svg/dialogBoxIcon/warning.svg");
        this.addImage('success1', "icons/others/success.png");
        this.addImage('success', "./icons/svg/dialogBoxIcon/success.svg");
        this.addImage('error', "./icons/svg/dialogBoxIcon/error.svg");
        this.addImage('alert', "icons/svg/confirmBox/alert.svg");
        this.addImage('menuImage', "icons/svg/homePage/menu_white_36.png");
        this.addImage('plus_icon', "icons/svg/auditReporting/Plus.svg");
        this.addImage('minus_icon', "icons/svg/reportMgmt/minus.svg");
        this.addImage('downArrow', "icons/svg/auditReporting/downArraw.svg");
        this.addImage('active_show_details', "icons/svg/auditReporting/showDetailAudit.svg");
        this.addImage('inactive_show_details', "icons/svg/auditReporting/ShowDetails-Inactive.svg");
        this.addImage('export_to_pdf_icon', "icons/svg/auditReporting/auditReportDownload.svg");
        this.addImage('view_pipe_line', "icons/svg/auditReporting/ViewPipeLine-GREEN.svg");
        this.addImage('ic_about_logo', "icons/svg/ic_about_logo.svg");
        this.addImage('ic_Insights_default_logo', "icons/svg/landingPage/Insights_Logo.png");
        this.addImage('redirect_icon', "icons/svg/userOnboarding/sharp-supervised_user_circle-24px.svg");
        this.addImage('unsubscribe_webhook', "icons/svg/webhook/WebhookUnsubscribe.svg");
        this.addImage('unsubscribe_webhookDeactive', "icons/svg/webhook/WebhookUnsubscribe-Deactivated.svg");
        this.addImage('webhook', "icons/svg/webhook/WebHook.svg");
        this.addImage('webhookDeactive', "icons/svg/webhook/WebHookDetactivate.svg");
        this.addImage('webhookCopyClipboard', "icons/svg/agentManagement/webhookCopyClipboard.svg");
        this.addImage('dashboard', "icons/svg/dashboard.svg");
        this.addImage('homeButton', "icons/svg/home.svg");
        this.addImage('predict', "./icons/svg/prediction/predict.svg");
        this.addImage('info', "icons/svg/info-24px.svg");
        this.addImage('dash_Menu', "icons/svg/homePage/svgmenuicons/dashboard.svg");
        this.addImage('dash_report_Menu', "icons/svg/homePage/svgmenuicons/dash_report.svg");
        this.addImage('audit_Menu', "icons/svg/homePage/svgmenuicons/audit.svg");
        this.addImage('config_Menu', "icons/svg/homePage/svgmenuicons/config.svg");
        this.addImage('data_dictionary_Menu', "icons/svg/homePage/svgmenuicons/data_dictionary.svg");
        this.addImage('health_Menu', "icons/svg/homePage/svgmenuicons/healthcheck.svg");
        this.addImage('report_mgmt_Menu', "icons/svg/homePage/svgmenuicons/report_mgmt.svg");
        this.addImage('playlist_Menu', "icons/svg/homePage/svgmenuicons/playlist.svg");
        this.addImage('userName', "./icons/svg/login/userName.svg");
        this.addImage('password', "./icons/svg/login/password.svg");
        this.addImage('list', "./icons/svg/landingpage/list.svg");
        this.addImage('thumb', "./icons/svg/landingpage/view-thumb.svg");
        this.addImage('searchIcon', "./icons/svg/landingpage/searchIcon.svg");
        this.addImage('dashboard_grp', "./icons/svg/landingpage/dashboard_grp.svg");
        this.addImage('star', "./icons/svg/landingpage/star.svg");
        this.addImage('time-reverse', "./icons/svg/landingpage/time-reverse.svg");
        this.addImage('add', "./icons/svg/reportMgmt/add.svg");
        this.addImage('edit', "./icons/svg/reportMgmt/edit.svg");
        this.addImage('trash', "./icons/svg/reportMgmt/trash.svg");
        this.addImage('download', "./icons/svg/reportMgmt/download.svg");
        this.addImage('retry', "./icons/svg/reportMgmt/refresh.svg");
        this.addImage('view', "./icons/svg/reportMgmt/view.svg");
        this.addImage('play', "./icons/svg/reportMgmt/play.svg");
        this.addImage('restart', "./icons/svg/reportMgmt/restart.svg");
        this.addImage('paperclip', "./icons/svg/reportMgmt/paperclip.svg")
        this.addImage('next-page', "./icons/svg/common/next_page.svg");
        this.addImage('prev-page', "./icons/svg/common/previous_page.svg");
        this.addImage('upload', "./icons/svg/agentManagement/upload.svg")
        this.addImage('cancel', "./icons/svg/agentManagement/cancel.svg")
        this.addImage('homeBck', "./icons/svg/reportMgmt/home.svg");
        this.addImage('exit', "./icons/svg/reportMgmt/exit.svg");
        this.addImage('cancelBlkUpld', "./icons/svg/bulkUpload/cancel.svg");
        this.addImage('addHook', "./icons/svg/webhook/addHook.svg");
        this.addImage('subscribeHook', "./icons/svg/webhook/subscribeHook.svg");
        this.addImage('unsubscribeHook', "./icons/svg/webhook/unsubscribeHook.svg");
        this.addImage('editHook', "./icons/svg/webhook/editHook.svg");
        this.addImage('saveHook', "./icons/svg/webhook/saveHook.svg");
        this.addImage('trashHook', "./icons/svg/webhook/trashHook.svg");
        this.addImage('linkHook', "./icons/svg/webhook/linkHook.svg");
        this.addImage('refreshHook', "./icons/svg/webhook/refreshHook.svg");
        this.addImage('arrow-down', "./icons/svg/agentManagement/arrow_down.svg");
        this.addImage('save', "./icons/svg/reportMgmt/save.svg");
        this.addImage('folder', "./icons/svg/landingpage/folder.svg");
        this.addImage('refresh', "./icons/svg/landingpage/time-reverse.svg");
        this.addImage('email_config', "./icons/svg/emailConfig/mail.svg");

        this.addImage('minus', "./icons/svg/reportMgmt/minus.svg");
        this.addImage('homeHook', "./icons/svg/webhook/homeHook.svg");
        this.addImage('search', "./icons/svg/milestoneConfig/search.svg");
        this.addImage('searchDashboard', "./icons/svg/search.svg");
        this.addImage('calendar', "./icons/svg/milestoneConfig/calendar.svg");
        this.addImage('help', "./icons/svg/bulkUpload/help.svg");
        this.addImage('exclamation', "./icons/svg/bulkUpload/Exclamation.svg");
        this.addImage('cross', "./icons/svg/bulkUpload/cross.svg");
        this.addImage('flag', "./icons/svg/correlationBuilder/flag.svg");
        this.addImage('correlation', "./icons/svg/correlationBuilder/correlationIcon.svg");
        this.addImage('delete_color', "./icons/svg/dialogBoxIcon/delete_color.svg");
        this.addImage('warn_color', "./icons/svg/dialogBoxIcon/warn_color.svg");
        this.addImage('chart', "./icons/svg/forecasting/chart.svg");
        this.addImage('successBlk', "./icons/svg/bulkUpload/success.svg");
        this.addImage('failureBlk', "./icons/svg/bulkUpload/failure.svg");
        this.addImage('up', "./icons/svg/dataArchival/UP.svg");
        this.addImage('down', "./icons/svg/dataArchival/Down.svg");
        this.addImage('backButton', "./icons/svg/common/backButton.svg");
        this.addImage('users', "./icons/svg/welcomePage/users.svg");
        this.addImage('admin', "./icons/svg/welcomePage/admin.svg");
        this.addImage('regenerate', "./icons/svg/reportMgmt/regenerate.svg");
    }

    public addPathIconRegistry() {
        this.imageMap.forEach((value: string, key: string) => {
            this.iconRegistry.addSvgIcon(key, this.sanitizer.bypassSecurityTrustResourceUrl(value));
        });
    }

    public addImage(name: string, imagePath: String) {
        if (!this.imageMap.has(name)) {
            this.imageMap.set(name, imagePath);
        } /* else {
            throw new Error('imagePath with same name already exists ' + name);
        } */
    }
    public getImagePath(imageKey: String) {
        if (!this.imageMap.has(imageKey)) {
            throw new Error("Url Mapping doesnt exist");
        }
        return this.imageMap.get(imageKey);
    }
}
